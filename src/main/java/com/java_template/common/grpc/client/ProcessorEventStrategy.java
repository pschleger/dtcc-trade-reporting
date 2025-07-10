package com.java_template.common.grpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.common.workflow.*;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationResponse;
import io.cloudevents.v1.proto.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strategy for handling EntityProcessorCalculationRequest events.
 * This strategy parses the CloudEvent, finds the appropriate CyodaProcessor,
 * and delegates directly to the processor. The processor handles its own
 * request/response conversion using serializers as needed.
 */
@Component
public class ProcessorEventStrategy implements EventHandlingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorEventStrategy.class);
    private static final String CALC_REQ_EVENT_TYPE = "EntityProcessorCalculationRequest";

    private final OperationFactory operationFactory;
    private final CyodaContextFactory eventContextFactory;
    private final ObjectMapper objectMapper;

    public ProcessorEventStrategy(OperationFactory operationFactory, ObjectMapper objectMapper, CyodaContextFactory eventContextFactory) {
        this.operationFactory = operationFactory;
        this.eventContextFactory = eventContextFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<Object> handleEvent(CloudEvent cloudEvent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("[IN] Received event {}: \n{}", CALC_REQ_EVENT_TYPE, cloudEvent.getTextData());

                CyodaEventContext<EntityProcessorCalculationRequest> context = eventContextFactory.createCyodaEventContext(cloudEvent, EntityProcessorCalculationRequest.class);

                EntityProcessorCalculationRequest request = context.getEvent();
                OperationSpecification.Processor processorOperation = OperationSpecification.create(request);
                String processorName = processorOperation.getProcessorName();

                logger.info("Running processor {}: {}", CALC_REQ_EVENT_TYPE, processorName);

                // Get the processor that supports this OperationSpecification
                CyodaProcessor processor = operationFactory.getProcessorForModel(processorOperation);

                CompletableFuture<EntityProcessorCalculationResponse> futureResponse = processor.process(context);
                try {
                    return futureResponse.get(); // Wait for completion
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Processing was interrupted", e);
                } catch (Exception e) {
                    throw new RuntimeException("Error during processing", e);
                }

            } catch (JsonProcessingException e) {
                logger.error("JsonProcessingException when parsing CloudEvent into EntityProcessorCalculationRequest: {}", cloudEvent, e);

                try {
                    EntityProcessorCalculationResponse errorResponse = new EntityProcessorCalculationResponse();
                    // we cannot parse the event into something we know, so we have to marshal into a map and try to find the requestId key
                    String requestId = recoverRequestIdFromCloudEvent(cloudEvent);
                    if (requestId != null) {
                        errorResponse.setRequestId(requestId);
                        logger.info("Set requestId {} in error response", requestId);
                    } else {
                        logger.warn("Could not recover requestId for error response");
                    }
                    errorResponse.setSuccess(false);
                    return errorResponse;
                } catch (Exception ex) {
                    logger.error("Error creating error response", ex);
                    return null;
                }
            } catch (Exception e) {
                logger.error("Error handling processor event: {}", cloudEvent, e);
                return null;
            }
        });
    }

    @Override
    public boolean supports(String eventType) {
        return CALC_REQ_EVENT_TYPE.equals(eventType);
    }

    @Override
    public String getStrategyName() {
        return "ProcessorEventStrategy";
    }

    /**
     * ABOUTME: Attempts to recover the requestId from a CloudEvent when JSON parsing fails.
     * Uses string-based regex patterns to search for the requestId field in potentially corrupted JSON.
     *
     * @param cloudEvent the CloudEvent containing potentially corrupted JSON data
     * @return the requestId if found, null otherwise
     */
    private String recoverRequestIdFromCloudEvent(CloudEvent cloudEvent) {
        if (cloudEvent == null) {
            logger.warn("CloudEvent is null, cannot recover requestId");
            return null;
        }

        String textData = cloudEvent.getTextData();
        if (textData == null || textData.trim().isEmpty()) {
            logger.warn("CloudEvent text data is null or empty, cannot recover requestId");
            return null;
        }

        logger.debug("Attempting to recover requestId from CloudEvent text data: {}", textData);

        // Pattern to match "requestId" field with various quote styles and whitespace
        // Matches: "requestId": "value", 'requestId': 'value', "requestId":"value", etc.
        Pattern requestIdPattern = Pattern.compile(
            "[\"']?requestId[\"']?\\s*:\\s*[\"']([^\"'\\s,}]+)[\"']?",
            Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = requestIdPattern.matcher(textData);
        if (matcher.find()) {
            String requestId = matcher.group(1);
            logger.info("Successfully recovered requestId from corrupted JSON: {}", requestId);
            return requestId;
        }

        // Fallback: try to find any UUID-like pattern near "requestId" text
        // This handles cases where quotes might be corrupted but the value is still readable
        Pattern fallbackPattern = Pattern.compile(
            "requestId[^a-zA-Z0-9-]*([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})",
            Pattern.CASE_INSENSITIVE
        );

        Matcher fallbackMatcher = fallbackPattern.matcher(textData);
        if (fallbackMatcher.find()) {
            String requestId = fallbackMatcher.group(1);
            logger.info("Successfully recovered requestId using fallback UUID pattern: {}", requestId);
            return requestId;
        }

        // Final fallback: look for any string value after "requestId" that looks like an identifier
        Pattern generalPattern = Pattern.compile(
            "requestId[^a-zA-Z0-9-]*([a-zA-Z0-9][a-zA-Z0-9-_]{2,})",
            Pattern.CASE_INSENSITIVE
        );

        Matcher generalMatcher = generalPattern.matcher(textData);
        if (generalMatcher.find()) {
            String requestId = generalMatcher.group(1);
            logger.info("Successfully recovered requestId using general pattern: {}", requestId);
            return requestId;
        }

        logger.warn("Could not recover requestId from CloudEvent text data. No matching patterns found.");
        return null;
    }

}
