package com.java_template.common.grpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.java_template.common.auth.Authentication;
import com.java_template.common.config.Config;
import com.java_template.common.util.SslUtils;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.protobuf.ProtobufFormat;
import io.cloudevents.v1.proto.CloudEvent;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.cyoda.cloud.api.event.common.BaseEvent;
import org.cyoda.cloud.api.event.common.CloudEventType;
import org.cyoda.cloud.api.event.processing.*;
import org.cyoda.cloud.api.grpc.CloudEventsServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.java_template.common.config.Config.*;

@Component
public class CyodaCalculationMemberClient implements DisposableBean, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Authentication authentication;
    private ManagedChannel managedChannel;
    private CloudEventsServiceGrpc.CloudEventsServiceStub cloudEventsServiceStub;
    private StreamObserver<CloudEvent> cloudEventStreamObserver;
    private EventFormat eventFormat;
    private final ObjectMapper objectMapper;
    private final List<EventHandlingStrategy<? extends BaseEvent>> eventHandlingStrategies;
    private ScheduledExecutorService connectionMonitor;

    private static final List<String> TAGS = List.of(GRPC_PROCESSOR_TAG);
    private static final String SOURCE = "urn:cyoda:calculation-member:" + Config.GRPC_PROCESSOR_TAG;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int MAX_RETRIES = 5;

    private final AtomicReference<MemberStatus> memberStatusRef = new AtomicReference<>(MemberStatus.starting());

    public CyodaCalculationMemberClient(ObjectMapper objectMapper,
                                        List<EventHandlingStrategy<? extends BaseEvent>> eventHandlingStrategies,
                                        Authentication authentication
    ) {
        this.objectMapper = objectMapper;
        this.authentication = authentication;
        this.eventHandlingStrategies = eventHandlingStrategies;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            logger.info("Initializing gRPC client with address: {}:{}", GRPC_ADDRESS, GRPC_SERVER_PORT);
            logger.debug("SSL trusted hosts: {}", Config.getTrustedHosts());
            logger.debug("SSL trust all: {}", Config.SSL_TRUST_ALL);

            managedChannel = SslUtils.createGrpcChannelBuilder(GRPC_ADDRESS, GRPC_SERVER_PORT).build();

            logger.debug("Created gRPC channel with authority: {}", managedChannel.authority());

            // Create a dynamic auth interceptor that gets fresh tokens
            ClientInterceptor authInterceptor = new ClientAuthorizationInterceptor(authentication);

            cloudEventsServiceStub = CloudEventsServiceGrpc.newStub(managedChannel).withWaitForReady().withInterceptors(authInterceptor);

            eventFormat = EventFormatProvider.getInstance().resolveFormat(ProtobufFormat.PROTO_CONTENT_TYPE);
            if (eventFormat == null) {
                throw new IllegalStateException("Unable to resolve protobuf event format for " + ProtobufFormat.PROTO_CONTENT_TYPE);
            }

            // Start connection monitoring
            startConnectionMonitoring();

            logger.info("gRPC client initialized successfully with server address: {} and port: {}", GRPC_ADDRESS, GRPC_SERVER_PORT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize gRPC client", e);
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();

        // Stop connection monitoring
        if (connectionMonitor != null && !connectionMonitor.isShutdown()) {
            connectionMonitor.shutdown();
            try {
                if (!connectionMonitor.awaitTermination(5, TimeUnit.SECONDS)) {
                    connectionMonitor.shutdownNow();
                }
            } catch (InterruptedException e) {
                connectionMonitor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (cloudEventStreamObserver != null) {
            cloudEventStreamObserver.onCompleted();
        }
        if (managedChannel != null) {
            try {
                managedChannel.shutdown().awaitTermination(100, TimeUnit.SECONDS);
                if (!managedChannel.isTerminated()) {
                    logger.warn("Forcing gRPC channel shutdown");
                    managedChannel.shutdownNow();
                }
                logger.info("gRPC channel shut down successfully");
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down gRPC channel", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @SuppressWarnings("unused")
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        connectToStream();
    }

    private void connectToStream() {
        cloudEventStreamObserver = cloudEventsServiceStub.startStreaming(new StreamObserver<>() {
            @Override
            public void onNext(CloudEvent cloudEvent) {
                handleCloudEvent(cloudEvent);
            }

            @Override
            public void onError(Throwable t) {
                logger.error("gRPC stream error - connection lost to remote backend. Error type: {}, message: {}",
                        t.getClass().getSimpleName(), t.getMessage(), t);
                scheduleReconnect();
            }

            @Override
            public void onCompleted() {
                logger.warn("Stream completed by remote backend, scheduling reconnect...");
                scheduleReconnect();
            }
        });

        // Reset retry count after successful connection
        memberStatusRef.updateAndGet(status -> status.withRetryCount(0));
        logger.info("Connected to gRPC event stream and awaiting events...");
        sendJoinEvent();
    }

    private void scheduleReconnect() {
        MemberStatus currentStatus = memberStatusRef.get();
        int currentRetries = currentStatus.retryCount();

        if (currentRetries >= MAX_RETRIES) {
            memberStatusRef.updateAndGet(MemberStatus::offline);
            logger.error("Maximum reconnection attempts ({}) reached. Member status updated to OFFLINE. Giving up.", MAX_RETRIES);
            return;
        }

        int delaySeconds = (int) Math.pow(2, currentRetries);
        MemberStatus updatedStatus = memberStatusRef.updateAndGet(MemberStatus::withIncrementedRetryCount);
        logger.info("Attempting to reconnect to gRPC stream in {} seconds (retry attempt {}) - current member status: {}",
                delaySeconds, updatedStatus.retryCount(), updatedStatus.state());
        scheduler.schedule(this::connectToStream, delaySeconds, TimeUnit.SECONDS);
    }

    private void sendJoinEvent() {
        CalculationMemberJoinEvent event = new CalculationMemberJoinEvent();
        event.setTags(TAGS);
        String joinEventId = UUID.randomUUID().toString();
        event.setId(joinEventId);

        // Update member status to JOINING and store the join event ID
        memberStatusRef.updateAndGet(status -> status.joiningWithEventId(joinEventId));
        logger.info("Member status updated to JOINING with join event ID: {}", joinEventId);

        try {
            sendEvent(event);
        } catch (Exception e) {
            logger.error("Failed to send join event. Will schedule a reconnect.", e);
            scheduleReconnect();
        }
    }

    private void handleCloudEvent(CloudEvent cloudEvent) {
        CompletableFuture.runAsync(() -> {
            try {
                // Find the appropriate strategy for this event type
                CloudEventType cloudEventType = CloudEventType.valueOf(cloudEvent.getType());
                eventHandlingStrategies.stream().filter(strategy -> strategy.supports(cloudEventType)).findFirst().ifPresentOrElse(strategy -> {
                    logger.debug("Using strategy '{}' for event type '{}'", strategy.getClass().getSimpleName(), cloudEventType);

                    // Handle the event and send the response.
                    // The contract on handleEvent is that it does not throw Exceptions,
                    // but handles them internally and returns an error response.
                    strategy.handleEvent(cloudEvent).thenAccept(response -> {
                        if (response != null) {
                            sendEvent(response);
                        }
                    });
                }, () -> {
                    // Handle other event types that don't need strategies
                    handleOtherEvents(cloudEvent);
                });
            } catch (Exception e) {
                logger.error("Error processing event: {}", cloudEvent, e);
            }
        });
    }

    /**
     * Handles event types that don't require specific strategies.
     */
    private void handleOtherEvents(CloudEvent cloudEvent) {
        CloudEventType cloudEventType = CloudEventType.valueOf(cloudEvent.getType());
        logger.debug("[IN] Received event {}: \n{}", cloudEventType, cloudEvent.getTextData());
        switch (cloudEventType) {
            case CloudEventType.EVENT_ACK_RESPONSE -> {
                Optional<EventAckResponse> response = parseCloudEvent(cloudEvent, EventAckResponse.class);
                response.ifPresent(eventAckResponse -> {
                    String sourceEventId = eventAckResponse.getSourceEventId();
                    boolean success = eventAckResponse.getSuccess();

                    // Check if this is an acknowledgment for our join event
                    MemberStatus currentStatus = memberStatusRef.get();
                    if (sourceEventId != null && sourceEventId.equals(currentStatus.lastJoinEventId())) {
                        if (success) {
                            memberStatusRef.updateAndGet(MemberStatus::joined);
                            logger.info("Member status updated to JOINED - successfully joined calculation network (event ID: {})", sourceEventId);
                        } else {
                            memberStatusRef.updateAndGet(MemberStatus::rejected);
                            logger.warn("Member status updated to REJECTED - join request was rejected (event ID: {})", sourceEventId);
                            // Schedule another join attempt
                            scheduleReconnect();
                        }
                    } else {
                        // Handle other event acknowledgments
                        if (success) {
                            logger.info("Received event ack for event ID: {}", sourceEventId);
                        } else {
                            throw new IllegalStateException("Received failed event ack for event ID: " + sourceEventId);
                        }
                    }
                });
            }
            case CloudEventType.CALCULATION_MEMBER_GREET_EVENT ->
                    logger.debug("[IN] Received event {}: \n{}", cloudEventType, cloudEvent.getTextData());
            case CloudEventType.CALCULATION_MEMBER_KEEP_ALIVE_EVENT -> {
                Optional<EventAckResponse> response = parseCloudEvent(cloudEvent, EventAckResponse.class);
                response.ifPresent(eventAckResponse -> {
                    eventAckResponse.setSourceEventId(eventAckResponse.getId());
                    memberStatusRef.updateAndGet(status -> status.withKeepAliveTimestamp(System.currentTimeMillis()));
                    sendEvent(eventAckResponse);
                });
            }
            default -> logger.warn("[IN] Received UNHANDLED event type {}: \n{}",
                    cloudEventType, cloudEvent.getTextData());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends BaseEvent> Optional<T> parseCloudEvent(CloudEvent cloudEvent, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(cloudEvent.getTextData(), clazz));
        } catch (JsonProcessingException e) {
            logger.error("Error parsing cloud event. This shouldn't happen unless the systems are misaligned {}",
                    cloudEvent, e);
            return Optional.empty();
        }
    }

    public void sendEvent(BaseEvent event) {
        CloudEvent cloudEvent;
        try {
            cloudEvent = CloudEvent.parseFrom(
                    eventFormat.serialize(
                            CloudEventBuilder.v1()
                                    .withType(event.getClass().getSimpleName())
                                    .withSource(URI.create(SOURCE))
                                    .withId(UUID.randomUUID().toString())
                                    .withData(
                                            PojoCloudEventData.wrap(event, eventData -> {
                                                try {
                                                    return objectMapper.writeValueAsBytes(eventData);
                                                } catch (JsonProcessingException e) {
                                                    throw new RuntimeException("Error serializing event data", e);
                                                }
                                            })
                                    )
                                    .build()
                    )
            );
        } catch (InvalidProtocolBufferException e) {
            logger.error("Failed to parse cloud event", e);
            // TODO: Define the strategy for handling serialization errors. For now we just log it.
            return;
        }

        var observer = cloudEventStreamObserver;

        if (observer == null) {
            throw new IllegalStateException("Stream observer is not initialized");
        }

        if (event.getSuccess()) {
            logger.debug("[OUT] Sending event {}, success: {}", cloudEvent.getType(), event.getSuccess());
        } else {
            logger.warn("[OUT] Sending event {}, success: {}", cloudEvent.getType(), event.getSuccess());
        }

        // StreamObserver is not thread-safe. Current implementation uses synchronized block.
        // For high-concurrency production environments, consider using event queue or stream pooling.
        synchronized (observer) {
            observer.onNext(cloudEvent);
        }
    }

    /**
     * Start monitoring the gRPC connection state
     */
    private void startConnectionMonitoring() {
        connectionMonitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "gRPC-Connection-Monitor");
            t.setDaemon(true);
            return t;
        });

        // Monitor connection state every 30 seconds
        connectionMonitor.scheduleWithFixedDelay(() -> {
            try {
                ConnectivityState currentState = managedChannel.getState(false);
                MemberStatus currentMemberStatus = memberStatusRef.get();
                ConnectivityState lastKnownState = currentMemberStatus.lastKnownState();

                if (currentState != lastKnownState) {
                    logger.info("gRPC connection state changed: {} -> {} (member status: {})",
                            lastKnownState, currentState, currentMemberStatus.state());
                    // Update the connectivity state atomically
                    memberStatusRef.updateAndGet(status -> status.withConnectivityState(currentState));

                    switch (currentState) {
                        case READY:
                            logger.info("gRPC connection is READY - connected to {}:{} (member status: {})",
                                    GRPC_ADDRESS, GRPC_SERVER_PORT, currentMemberStatus.state());
                            break;
                        case CONNECTING:
                            logger.info("gRPC connection is CONNECTING to {}:{} (member status: {})",
                                    GRPC_ADDRESS, GRPC_SERVER_PORT, currentMemberStatus.state());
                            break;
                        case IDLE:
                            logger.info("gRPC connection is IDLE (member status: {})", currentMemberStatus.state());
                            break;
                        case TRANSIENT_FAILURE:
                            logger.warn("gRPC connection is in TRANSIENT_FAILURE state - connection issues detected (member status: {})",
                                    currentMemberStatus.state());
                            logger.warn("Attempting to connect to: {}:{}", GRPC_ADDRESS, GRPC_SERVER_PORT);
                            logger.warn("SSL trusted hosts: {}", Config.getTrustedHosts());
                            logger.warn("Channel authority: {}", managedChannel.authority());
                            break;
                        case SHUTDOWN:
                            logger.warn("gRPC connection is SHUTDOWN (member status: {})", currentMemberStatus.state());
                            break;
                    }
                }

                // If member status is OFFLINE, initiate clean shutdown
                if (currentMemberStatus.state() == MemberStatus.MemberState.OFFLINE) {
                    logger.warn("Member status is OFFLINE - initiating clean shutdown of gRPC connections");
                    // Note: We don't call destroy() here to avoid recursive shutdown
                    if (cloudEventStreamObserver != null) {
                        try {
                            cloudEventStreamObserver.onCompleted();
                            cloudEventStreamObserver = null;
                        } catch (Exception e) {
                            logger.warn("Error completing stream observer during OFFLINE shutdown", e);
                        }
                    }
                    return; // Exit monitoring loop early
                }

                // Check if stream observer is null (disconnected)
                if (cloudEventStreamObserver == null && currentState == ConnectivityState.READY) {
                    logger.warn("gRPC channel is READY but stream observer is null - connection may have been lost (member status: {})",
                            currentMemberStatus.state());
                }

                // Log periodic status for debugging
                if (logger.isDebugEnabled()) {
                    logger.debug("gRPC connection status: state={}, streamObserver={}, memberStatus={}",
                            currentState, cloudEventStreamObserver != null ? "connected" : "disconnected", currentMemberStatus.state());
                }

                // Log warning if keep alive isn't coming
                long timeSinceLastKeepAlive = System.currentTimeMillis() - currentMemberStatus.lastKeepAliveTimestamp();
                if (timeSinceLastKeepAlive > Config.KEEP_ALIVE_WARNING_THRESHOLD) {
                    logger.warn("Last keep alive received {} seconds ago. Connection might be stale. (member status: {})",
                            timeSinceLastKeepAlive / 1000, currentMemberStatus.state());
                }

            } catch (Exception e) {
                logger.error("Error monitoring gRPC connection state", e);
            }
        }, 10, 30, TimeUnit.SECONDS); // Start after 10 seconds, then every 30 seconds

        logger.info("Started gRPC connection monitoring");
    }

    /**
     * Gets the current member status.
     *
     * @return the current MemberStatus containing state, join event ID, retry count, and keep alive timestamp
     */
    public MemberStatus getMemberStatus() {
        return memberStatusRef.get();
    }
}
