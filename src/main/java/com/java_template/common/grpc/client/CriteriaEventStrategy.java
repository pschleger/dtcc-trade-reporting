package com.java_template.common.grpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.common.workflow.*;
import io.cloudevents.v1.proto.CloudEvent;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Strategy for handling EntityCriteriaCalculationRequest events.
 * This strategy parses the CloudEvent, finds the appropriate CyodaCriterion,
 * and delegates directly to the criteria checker. The criteria checker handles its own
 * request/response conversion using serializers as needed.
 */
@Component
public class CriteriaEventStrategy implements EventHandlingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CriteriaEventStrategy.class);
    private static final String CRIT_CALC_REQ_EVENT_TYPE = "EntityCriteriaCalculationRequest";
    public static final String CRITERIA_EVENT_STRATEGY = "CriteriaEventStrategy";

    private final OperationFactory operationFactory;
    private final ObjectMapper objectMapper;
    private final CyodaContextFactory eventContextFactory;

    public CriteriaEventStrategy(OperationFactory operationFactory, ObjectMapper objectMapper, CyodaContextFactory eventContextFactory) {
        this.operationFactory = operationFactory;
        this.objectMapper = objectMapper;
        this.eventContextFactory = eventContextFactory;
    }

    @Override
    public CompletableFuture<Object> handleEvent(CloudEvent cloudEvent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("[IN] Received event {}: \n{}", CRIT_CALC_REQ_EVENT_TYPE, cloudEvent.getTextData());

                CyodaEventContext<EntityCriteriaCalculationRequest> context = eventContextFactory.createCyodaEventContext(cloudEvent, EntityCriteriaCalculationRequest.class);

                EntityCriteriaCalculationRequest request = context.getEvent();
                OperationSpecification.Criterion criterionOperation = OperationSpecification.create(request);
                String criterionName = criterionOperation.getCriterionName();

                logger.info("running Criterion {}: {}", CRIT_CALC_REQ_EVENT_TYPE, criterionName);

                // Get a criterion checker that supports this OperationSpecification
                CyodaCriterion cyodaCriterion = operationFactory.getCriteriaForModel(criterionOperation);

                // Delegate directly to criteria checker - it handles its own serialization
                CompletableFuture<EntityCriteriaCalculationResponse> futureResponse = cyodaCriterion.check(request);
                try {
                    return futureResponse.get(); // Wait for completion
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Criteria checking was interrupted", e);
                } catch (Exception e) {
                    throw new RuntimeException("Error during criteria checking", e);
                }

            } catch (IOException e) {
                logger.error("Error handling criteria event: {}", cloudEvent, e);

                // Create error response
                try {
                    EntityCriteriaCalculationResponse errorResponse = objectMapper.readValue(cloudEvent.getTextData(), EntityCriteriaCalculationResponse.class);
                    errorResponse.setSuccess(false);
                    errorResponse.setMatches(false);
                    return errorResponse;
                } catch (Exception ex) {
                    logger.error("Error creating error response", ex);
                    return null;
                }
            }
        });
    }

    @Override
    public boolean supports(String eventType) {
        return CRIT_CALC_REQ_EVENT_TYPE.equals(eventType);
    }

    @Override
    public String getStrategyName() {
        return CRITERIA_EVENT_STRATEGY;
    }

}
