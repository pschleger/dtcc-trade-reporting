package com.java_template.common.workflow.ops;

import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.CyodaProcessor;
import com.java_template.common.workflow.OperationSpecification;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationResponse;
import org.springframework.stereotype.Component;

/**
 * ABOUTME: A processor that does nothing.
 */
@Component
public class NoopProcessor implements CyodaProcessor {
    private static final String BEAN_NAME = NoopProcessor.class.getSimpleName();

    @Override
    public EntityProcessorCalculationResponse process(CyodaEventContext<EntityProcessorCalculationRequest> context) {
        EntityProcessorCalculationResponse response = new EntityProcessorCalculationResponse();
        EntityProcessorCalculationRequest request = context.getEvent();
        response.setSuccess(true);
        response.setEntityId(request.getEntityId());
        response.setRequestId(request.getRequestId());
        response.setSuccess(true);
        return response;
    }

    @Override
    public boolean supports(OperationSpecification modelKey) {
        return BEAN_NAME.equals(modelKey.operationName());
    }

}
