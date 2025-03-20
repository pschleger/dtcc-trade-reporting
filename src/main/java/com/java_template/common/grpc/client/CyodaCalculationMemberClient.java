package com.java_template.common.grpc.client;

import com.java_template.common.auth.Authentication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.java_template.common.util.JsonUtils;
import com.java_template.entity.WorkflowProcessor;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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

    private static final List<String> TAGS = List.of(GRPC_PROCESSOR_TAG);
    private static final String OWNER = "PLAY";
    private static final String SPEC_VERSION = "1.0";
    private static final String SOURCE = "SimpleSample";
    private static final String JOIN_EVENT_TYPE = "CalculationMemberJoinEvent";
    private static final String CALC_RESP_EVENT_TYPE = "EntityProcessorCalculationResponse";
    private static final String CALC_REQ_EVENT_TYPE = "EntityProcessorCalculationRequest";
    private static final String GREET_EVENT_TYPE = "CalculationMemberGreetEvent";
    private static final String KEEP_ALIVE_EVENT_TYPE = "CalculationMemberKeepAliveEvent";
    private static final String EVENT_ACK_TYPE = "EventAckResponse";
    private static final String EVENT_ID_FORMAT = "{uuid}";
    private static final int GRPC_SERVER_PORT = 443;

    public CyodaCalculationMemberClient(ObjectMapper objectMapper, WorkflowProcessor workflowProcessor, Authentication authentication) {
        this.objectMapper = objectMapper;
        this.workflowProcessor = workflowProcessor;
        this.token = authentication.getToken();

        if (this.token == null) {
            throw new IllegalStateException("Token is not initialized");
        }
    }

    public CloudEvent createCloudEvent(String eventId, String eventType, Map<String, Object> data) {
        try {
            String jsonData = JsonUtils.mapToJson(data);

            CloudEvent.Builder cloudEventBuilder = CloudEvent.newBuilder()
                    .setId(eventId)
                    .setSource(SOURCE)
                    .setSpecVersion(SPEC_VERSION)
                    .setType(eventType)
                    .setTextData(jsonData);

            return cloudEventBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error creating CloudEvent", e);
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
        try {
            cloudEventStreamObserver = cloudEventsServiceStub.startStreaming(new StreamObserver<>() {
                @Override
                public void onNext(CloudEvent cloudEvent) {
//                    logger.info(">> Got EVENT:\n" + cloudEvent);

                    handleCloudEvent(cloudEvent);
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("Error received from remote backend", t);
                }

                @Override
                public void onCompleted() {
                    logger.info("Stream completed by remote backend");
                }
            });
            logger.info("Started streaming events from gRPC server");
            CalculationMemberJoinEvent event = new CalculationMemberJoinEvent();
            event.setOwner(OWNER);
            event.setTags(TAGS);
            sendEvent(event);
        } catch (Exception e) {
            logger.error("Failed to start streaming events from gRPC server", e);
        }
    }

    private void handleCloudEvent(CloudEvent cloudEvent) {
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("<< Received event: \n{}", cloudEvent.getTextData());
                Object json = objectMapper.readValue(cloudEvent.getTextData(), Object.class);
                logger.info("<< Received event: \n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
                switch (cloudEvent.getType()) {
                    case CALC_REQ_EVENT_TYPE:
                        EntityProcessorCalculationRequest request = objectMapper.readValue(cloudEvent.getTextData(), EntityProcessorCalculationRequest.class);
                        EntityProcessorCalculationResponse response = objectMapper.readValue(cloudEvent.getTextData(), EntityProcessorCalculationResponse.class);
                        String processorName = request.getProcessorName();
                        logger.info("Processing {}: {}", CALC_REQ_EVENT_TYPE, processorName);
                        Map<String, Object> payloadData = JsonUtils.jsonToMap(request.getPayload().getData().toString());
                        CompletableFuture<Map<String, Object>> futureResult =  workflowProcessor.processEvent(processorName, payloadData);
                        futureResult.thenAccept(result -> {
                            try {
                                response.getPayload().setData(JsonUtils.getJsonNode(result));
                                sendEvent(response);
                            } catch (InvalidProtocolBufferException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;
                    case EVENT_ACK_TYPE:
                        logger.info("Received {}", EVENT_ACK_TYPE);
                        break;
                    case KEEP_ALIVE_EVENT_TYPE:
                        EventAckResponse eventAckResponse = objectMapper.readValue(cloudEvent.getTextData(), EventAckResponse.class);
                        eventAckResponse.setSourceEventId(eventAckResponse.getId());
                        sendEvent(eventAckResponse);
                        break;
                    default:
                        logger.info("Unhandled event type: {}", cloudEvent.getType());
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

//        logger.info("<< Sending event:\n" + event);
        logger.info("<< Sending event {}, success: {}\n", cloudEvent.getType(), event.getSuccess());

        // stream observer is not thread safe, for production usage this should be managed by some pooling for such cases
        synchronized (observer) {
            observer.onNext(cloudEvent);
        }
    }
}