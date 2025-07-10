package com.java_template.common.workflow.ops;

import com.java_template.application.serializer.ProcessorRequestSerializer;
import com.java_template.common.config.Config;
import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.CyodaProcessor;
import com.java_template.common.workflow.OperationSpecification;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ABOUTME: A processor that does nothing.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class NoopProcessor implements CyodaProcessor {

    private final String className = this.getClass().getSimpleName();

    private final ProcessorRequestSerializer serializer;


    public NoopProcessor(ProcessorRequestSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public EntityProcessorCalculationResponse process(CyodaEventContext<EntityProcessorCalculationRequest> context) {
        EntityProcessorCalculationResponse response = serializer.createResponse(context.getEvent());
        response.setSuccess(true);
        return response;
    }

    @Override
    public boolean supports(OperationSpecification modelKey) {
        return Config.INCLUDE_DEFAULT_OPERATIONS || className.equals(modelKey.operationName());
    }

}
