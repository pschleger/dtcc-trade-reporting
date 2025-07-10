package com.java_template.common.workflow.ops;

import com.java_template.common.workflow.CyodaCriterion;
import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.OperationSpecification;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.springframework.stereotype.Component;

/**
 * ABOUTME: A CyodaCriterion that always returns true.
 */
@Component
public class AlwaysTrueCriterion implements CyodaCriterion {

    private final String className = this.getClass().getSimpleName();

    @Override
    public EntityCriteriaCalculationResponse check(CyodaEventContext<EntityCriteriaCalculationRequest> request) {
        EntityCriteriaCalculationResponse response = new EntityCriteriaCalculationResponse();

        EntityCriteriaCalculationRequest event = request.getEvent();

        response.setMatches(true);
        response.setEntityId(event.getEntityId());
        response.setRequestId(request.getEvent().getRequestId());
        response.setSuccess(true);
        return response;
    }

    @Override
    public boolean supports(OperationSpecification opsSpec) {
        return className.equals(opsSpec.operationName());
    }

}
