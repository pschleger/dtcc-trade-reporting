package com.java_template.common.workflow.ops;

import com.java_template.common.workflow.CyodaCriterion;
import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.OperationSpecification;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.springframework.stereotype.Component;

/**
 * ABOUTME: A CyodaCriterion that always returns false.
 */
@Component
public class AlwaysFalseCriterion extends AlwaysTrueCriterion implements CyodaCriterion {

    @Override
    public EntityCriteriaCalculationResponse check(CyodaEventContext<EntityCriteriaCalculationRequest> request) {
        EntityCriteriaCalculationResponse response = super.check(request);
        response.setMatches(true);
        return response;
    }

}
