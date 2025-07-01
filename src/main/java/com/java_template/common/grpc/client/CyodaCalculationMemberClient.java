package com.java_template.common.grpc.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.common.auth.Authentication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.java_template.common.util.JsonUtils;
import com.java_template.common.workflow.WorkflowProcessor;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.protobuf.ProtobufFormat;
import io.cloudevents.v1.proto.CloudEvent;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.cyoda.cloud.api.event.*;
import org.cyoda.cloud.api.grpc.CloudEventsServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static com.java_template.common.config.Config.*;

@Component
public class CyodaCalculationMemberClient implements DisposableBean, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CyodaCalculationMemberClient.class);

    private final String token;
    private ManagedChannel managedChannel;
    private CloudEventsServiceGrpc.CloudEventsServiceStub cloudEventsServiceStub;
    private StreamObserver<CloudEvent> cloudEventStreamObserver;
    private EventFormat eventFormat;
    private final ObjectMapper objectMapper;
    private final WorkflowProcessor workflowProcessor;
    private final JsonUtils jsonUtils;

    private static final List<String> TAGS = List.of(GRPC_PROCESSOR_TAG);
    private static final String OWNER = "PLAY";
    private static final String SOURCE = "SimpleSample";
    private static final String JOIN_EVENT_TYPE = "CalculationMemberJoinEvent";
    private static final String CALC_RESP_EVENT_TYPE = "EntityProcessorCalculationResponse";
    private static final String CALC_REQ_EVENT_TYPE = "EntityProcessorCalculationRequest";
    private static final String CRIT_CALC_REQ_EVENT_TYPE = "EntityCriteriaCalculationRequest";
    private static final String GREET_EVENT_TYPE = "CalculationMemberGreetEvent";
    private static final String KEEP_ALIVE_EVENT_TYPE = "CalculationMemberKeepAliveEvent";
    private static final String EVENT_ACK_TYPE = "EventAckResponse";
    private static final int GRPC_SERVER_PORT = 443;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int MAX_RETRIES = 5;
    private int retryCount = 0;

    public CyodaCalculationMemberClient(ObjectMapper objectMapper, WorkflowProcessor workflowProcessor, Authentication authentication, JsonUtils jsonUtils) {
        this.objectMapper = objectMapper;
        this.workflowProcessor = workflowProcessor;
        this.token = authentication.getAccessToken();
        this.jsonUtils = jsonUtils;

        if (this.token == null) {
            throw new IllegalStateException("Token is not initialized");
        }
    }

    @Override
    public void afterPropertiesSet() {
        try {
            managedChannel = ManagedChannelBuilder.forAddress(GRPC_ADDRESS, GRPC_SERVER_PORT)
                    .useTransportSecurity()
                    .build();

            Metadata metadata = new Metadata();
            Metadata.Key<String> authKey = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(authKey, "Bearer " + token);

            ClientInterceptor authInterceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);

            cloudEventsServiceStub = CloudEventsServiceGrpc.newStub(managedChannel)
                    .withWaitForReady()
                    .withInterceptors(authInterceptor);

            eventFormat = EventFormatProvider.getInstance().resolveFormat(ProtobufFormat.PROTO_CONTENT_TYPE);
            if (eventFormat == null) {
                throw new IllegalStateException("Unable to resolve protobuf event format");
            }
            logger.info("gRPC client initialized successfully with server address: {} and port: {}", GRPC_ADDRESS, GRPC_SERVER_PORT);
        } catch (Exception e) {
            logger.error("Failed to initialize gRPC client", e);
            throw new RuntimeException("Failed to initialize gRPC client", e);
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
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
                logger.error("Error received from remote backend, scheduling reconnect...", t);
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
        event.setOwner(OWNER);
        event.setTags(TAGS);
        try {
            sendEvent(event);
        } catch (Exception e) {
            logger.error("Failed to send join event", e);
        }
    }

    private void handleCloudEvent(CloudEvent cloudEvent) {
        CompletableFuture.runAsync(() -> {
            try {
                switch (cloudEvent.getType()) {
                    case CALC_REQ_EVENT_TYPE:
                        logger.info("[IN] Received event {}: \n{}", CALC_REQ_EVENT_TYPE, cloudEvent.getTextData());
                        EntityProcessorCalculationRequest request = objectMapper.readValue(cloudEvent.getTextData(), EntityProcessorCalculationRequest.class);
                        EntityProcessorCalculationResponse response = objectMapper.readValue(cloudEvent.getTextData(), EntityProcessorCalculationResponse.class);
                        String processorName = request.getProcessorName();
                        ObjectNode payloadData = (ObjectNode) request.getPayload().getData();
                        logger.info("Processing {}: {}", CALC_REQ_EVENT_TYPE, processorName);
                        CompletableFuture<ObjectNode> futureResult = workflowProcessor.processEvent(processorName, payloadData);
                        futureResult.thenAccept(result -> {
                            try {
                                boolean success = result.path("success").asBoolean(true);
                                result.remove("success");
                                response.setSuccess(success);
                                response.getPayload().setData(jsonUtils.getJsonNode(result));
                                sendEvent(response);
                            } catch (InvalidProtocolBufferException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;
                    case CRIT_CALC_REQ_EVENT_TYPE:
                        logger.info("[IN] Received event {}: \n{}", CRIT_CALC_REQ_EVENT_TYPE, cloudEvent.getTextData());
                        EntityCriteriaCalculationRequest req = objectMapper.readValue(cloudEvent.getTextData(), EntityCriteriaCalculationRequest.class);
                        EntityCriteriaCalculationResponse res = objectMapper.readValue(cloudEvent.getTextData(), EntityCriteriaCalculationResponse.class);
                        String criteriaName = req.getCriteriaName();
                        ObjectNode payload = (ObjectNode) req.getPayload().getData();
                        logger.info("Processing {}: {}", CRIT_CALC_REQ_EVENT_TYPE, criteriaName);
                        CompletableFuture<ObjectNode> future = workflowProcessor.processEvent(criteriaName, payload);
                        future.thenAccept(result -> {
                            try {
                                boolean success = result.path("success").asBoolean(false);
                                result.remove("success");
                                res.setSuccess(success);
                                res.setMatches(success);
                                sendEvent(res);
                            } catch (InvalidProtocolBufferException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;
                    case EVENT_ACK_TYPE:
                        logger.debug("[IN] Received event {}: \n{}", EVENT_ACK_TYPE, cloudEvent.getTextData());
                        break;
                    case GREET_EVENT_TYPE:
                        logger.info("[IN] Received event {}: \n{}", GREET_EVENT_TYPE, cloudEvent.getTextData());
                        break;
                    case KEEP_ALIVE_EVENT_TYPE:
                        logger.info("[IN] Received event {}", KEEP_ALIVE_EVENT_TYPE);
                        EventAckResponse eventAckResponse = objectMapper.readValue(cloudEvent.getTextData(), EventAckResponse.class);
                        eventAckResponse.setSourceEventId(eventAckResponse.getId());
                        sendEvent(eventAckResponse);
                        break;
                    default:
                        logger.info("[IN] Received unhandled event type {}: \n{}", cloudEvent.getType(), cloudEvent.getTextData());
                }
            } catch (IOException e) {
                logger.error("Error processing event: {}", cloudEvent, e);
            }
        });
    }

    public void sendEvent(BaseEvent event) throws InvalidProtocolBufferException {
        CloudEvent cloudEvent = CloudEvent.parseFrom(
                eventFormat.serialize(
                        CloudEventBuilder.v1()
                                .withType(event.getClass().getSimpleName())
                                .withSource(URI.create(SOURCE))
                                .withId(UUID.randomUUID().toString())
                                .withData(PojoCloudEventData.wrap(event, eventData -> {
                                    try {
                                        return objectMapper.writeValueAsBytes(eventData);
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException("Error serializing event data", e);
                                    }
                                }))
                                .build()
                )
        );

        var observer = cloudEventStreamObserver;

        if (observer == null) {
            throw new IllegalStateException("Stream observer is not initialized");
        }

        if (event.getSuccess()) {
            logger.info("[OUT] Sending event {}, success: {}", cloudEvent.getType(), event.getSuccess());
        } else {
            logger.warn("[OUT] Sending event {}, success: {}", cloudEvent.getType(), event.getSuccess());
        }

        // StreamObserver is not thread-safe. Current implementation uses synchronized block.
        // For high-concurrency production environments, consider using event queue or stream pooling.
        synchronized (observer) {
            observer.onNext(cloudEvent);
        }
    }
}