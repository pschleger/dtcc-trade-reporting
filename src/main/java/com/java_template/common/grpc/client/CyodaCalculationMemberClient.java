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
import org.cyoda.cloud.api.event.processing.CalculationMemberJoinEvent;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationResponse;
import org.cyoda.cloud.api.event.processing.EventAckResponse;
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
    private final List<EventHandlingStrategy> eventHandlingStrategies;
    private ScheduledExecutorService connectionMonitor;
    private volatile ConnectivityState lastKnownState = ConnectivityState.IDLE;

    private volatile long lastKeepAliveTimestamp;

    private static final List<String> TAGS = List.of(GRPC_PROCESSOR_TAG);
    private static final String SOURCE = "SimpleSample";
    private static final String CALC_REQ_EVENT_TYPE = "EntityProcessorCalculationRequest";
    private static final String CRIT_CALC_REQ_EVENT_TYPE = "EntityCriteriaCalculationRequest";
    private static final String GREET_EVENT_TYPE = "CalculationMemberGreetEvent";
    private static final String KEEP_ALIVE_EVENT_TYPE = "CalculationMemberKeepAliveEvent";
    private static final String EVENT_ACK_TYPE = "EventAckResponse";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int MAX_RETRIES = 5;
    volatile private int retryCount = 0;

    public CyodaCalculationMemberClient(ObjectMapper objectMapper,
                                        List<EventHandlingStrategy> eventHandlingStrategies,
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

        retryCount = 0; // Reset retry count after successful connection
        logger.info("Connected to gRPC event stream and awaiting events...");
        sendJoinEvent();
    }

    private void scheduleReconnect() {
        if (retryCount >= MAX_RETRIES) {
            logger.error("Maximum reconnection attempts ({}) reached. Giving up.", MAX_RETRIES);
            return;
        }

        int delaySeconds = (int) Math.pow(2, retryCount);
        logger.info("Attempting to reconnect to gRPC stream in {} seconds (retry attempt {}).", delaySeconds, retryCount + 1);
        scheduler.schedule(this::connectToStream, delaySeconds, TimeUnit.SECONDS);
        retryCount++;
    }

    private void sendJoinEvent() {
        CalculationMemberJoinEvent event = new CalculationMemberJoinEvent();
        event.setTags(TAGS);
        event.setId(UUID.randomUUID().toString());
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
                eventHandlingStrategies.stream().filter(strategy -> strategy.supports(cloudEvent.getType())).findFirst().ifPresentOrElse(strategy -> {
                    logger.debug("Using strategy '{}' for event type '{}'", strategy.getStrategyName(), cloudEvent.getType());

                    // Handle the event and send the response
                    strategy.handleEvent(cloudEvent).thenAccept(response -> {
                        if (response != null) {
                            sendEvent((BaseEvent) response);
                        }
                    }).exceptionally(throwable -> {
                        logger.error("Error in strategy '{}' for event type '{}'", strategy.getStrategyName(), cloudEvent.getType(), throwable);

                        // Create and send error response based on event type
                        Object errorResponse = createErrorResponse(cloudEvent, throwable.getMessage());
                        if (errorResponse != null) {
                            sendEvent((BaseEvent) errorResponse);
                        }

                        return null;
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
        logger.debug("[IN] Received event {}: \n{}", cloudEvent.getType(), cloudEvent.getTextData());
        switch (cloudEvent.getType()) {
            case EVENT_ACK_TYPE -> {
                Optional<EventAckResponse> response = parseCloudEvent(cloudEvent, EventAckResponse.class);
                response.ifPresent(eventAckResponse -> {
                    if (eventAckResponse.getSuccess()) {
                        logger.info("Received event ack for event ID: {}", eventAckResponse.getSourceEventId());
                    } else {
                        logger.error("Received failed event ack for event ID: {}", eventAckResponse.getSourceEventId());
                    }
                });
            }
            case GREET_EVENT_TYPE ->
                    logger.debug("[IN] Received event {}: \n{}", GREET_EVENT_TYPE, cloudEvent.getTextData());
            case KEEP_ALIVE_EVENT_TYPE -> {
                Optional<EventAckResponse> response = parseCloudEvent(cloudEvent, EventAckResponse.class);
                response.ifPresent(eventAckResponse -> {
                    eventAckResponse.setSourceEventId(eventAckResponse.getId());
                    lastKeepAliveTimestamp = System.currentTimeMillis();
                    sendEvent(eventAckResponse);
                });
            }
            default ->
                    logger.warn("[IN] Received UNHANDLED event type {}: \n{}",
                            cloudEvent.getType(), cloudEvent.getTextData());
        }
    }

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
            cloudEvent = CloudEvent.parseFrom(eventFormat.serialize(CloudEventBuilder.v1().withType(event.getClass().getSimpleName()).withSource(URI.create(SOURCE)).withId(UUID.randomUUID().toString()).withData(PojoCloudEventData.wrap(event, eventData -> {
                try {
                    return objectMapper.writeValueAsBytes(eventData);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error serializing event data", e);
                }
            })).build()));
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
     * Creates an appropriate error response based on the event type.
     * This centralizes error response creation logic in the calculation member client.
     *
     * @param cloudEvent   the original CloudEvent that caused the error
     * @param errorMessage the error message to include
     * @return the appropriate error response object, or null if event type is unknown
     */
    private Object createErrorResponse(CloudEvent cloudEvent, String errorMessage) {
        try {
            String eventType = cloudEvent.getType();

            if (CALC_REQ_EVENT_TYPE.equals(eventType)) {
                // Create EntityProcessorCalculationResponse error
                EntityProcessorCalculationResponse errorResponse = objectMapper.readValue(cloudEvent.getTextData(), EntityProcessorCalculationResponse.class);
                errorResponse.setSuccess(false);
                logger.debug("Created processor error response: {}", errorMessage);
                return errorResponse;

            } else if (CRIT_CALC_REQ_EVENT_TYPE.equals(eventType)) {
                // Create EntityCriteriaCalculationResponse error
                EntityCriteriaCalculationResponse errorResponse = objectMapper.readValue(cloudEvent.getTextData(), EntityCriteriaCalculationResponse.class);
                errorResponse.setSuccess(false);
                errorResponse.setMatches(false);
                logger.debug("Created criteria error response: {}", errorMessage);
                return errorResponse;

            } else {
                logger.warn("Unknown event type for error response creation: {}", eventType);
                return null;
            }

        } catch (Exception e) {
            logger.error("Failed to create error response for event type: {}", cloudEvent.getType(), e);
            return null;
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
                if (currentState != lastKnownState) {
                    logger.info("gRPC connection state changed: {} -> {}", lastKnownState, currentState);
                    lastKnownState = currentState;

                    switch (currentState) {
                        case READY:
                            logger.info("gRPC connection is READY - connected to {}:{}", GRPC_ADDRESS, GRPC_SERVER_PORT);
                            break;
                        case CONNECTING:
                            logger.info("gRPC connection is CONNECTING to {}:{}", GRPC_ADDRESS, GRPC_SERVER_PORT);
                            break;
                        case IDLE:
                            logger.info("gRPC connection is IDLE");
                            break;
                        case TRANSIENT_FAILURE:
                            logger.warn("gRPC connection is in TRANSIENT_FAILURE state - connection issues detected");
                            logger.warn("Attempting to connect to: {}:{}", GRPC_ADDRESS, GRPC_SERVER_PORT);
                            logger.warn("SSL trusted hosts: {}", Config.getTrustedHosts());
                            logger.warn("Channel authority: {}", managedChannel.authority());
                            break;
                        case SHUTDOWN:
                            logger.warn("gRPC connection is SHUTDOWN");
                            break;
                    }
                }

                // Check if stream observer is null (disconnected)
                if (cloudEventStreamObserver == null && currentState == ConnectivityState.READY) {
                    logger.warn("gRPC channel is READY but stream observer is null - connection may have been lost");
                }

                // Log periodic status for debugging
                if (logger.isDebugEnabled()) {
                    logger.debug("gRPC connection status: state={}, streamObserver={}", currentState, cloudEventStreamObserver != null ? "connected" : "disconnected");
                }

                // Log warning if keep alive isn't coming
                long timeSinceLastKeepAlive = System.currentTimeMillis() - lastKeepAliveTimestamp;
                if (timeSinceLastKeepAlive > Config.KEEP_ALIVE_WARNING_THRESHOLD) {
                    logger.warn("Last keep alive received {} seconds ago. Connection might be stale.", timeSinceLastKeepAlive / 1000);
                }

            } catch (Exception e) {
                logger.error("Error monitoring gRPC connection state", e);
            }
        }, 10, 30, TimeUnit.SECONDS); // Start after 10 seconds, then every 30 seconds

        logger.info("Started gRPC connection monitoring");
    }
}
