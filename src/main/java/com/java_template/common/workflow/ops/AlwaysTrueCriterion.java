package com.java_template.common.workflow.ops;

import com.java_template.application.serializer.CriteriaRequestSerializer;
import com.java_template.common.config.Config;
import com.java_template.common.workflow.CyodaCriterion;
import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.OperationSpecification;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ABOUTME: A CyodaCriterion that always returns true.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class AlwaysTrueCriterion implements CyodaCriterion {

    private final String className = this.getClass().getSimpleName();
    private final CriteriaRequestSerializer serializer;

    public AlwaysTrueCriterion(CriteriaRequestSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public EntityCriteriaCalculationResponse check(CyodaEventContext<EntityCriteriaCalculationRequest> request) {
        EntityCriteriaCalculationResponse response = serializer.createResponse(request.getEvent());
        response.setSuccess(true);
        return response;
    }

    @Override
    public boolean supports(OperationSpecification opsSpec) {
        return Config.INCLUDE_DEFAULT_OPERATIONS || className.equals(opsSpec.operationName());
    }

}
