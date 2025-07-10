package com.java_template.common.workflow.ops;

import com.java_template.application.serializer.CriteriaRequestSerializer;
import com.java_template.common.workflow.CyodaCriterion;
import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.OperationSpecification;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ABOUTME: A CyodaCriterion that always returns false.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class AlwaysFalseCriterion extends AlwaysTrueCriterion implements CyodaCriterion {

    public AlwaysFalseCriterion(CriteriaRequestSerializer serializer) {
        super(serializer);
    }

    @Override
    public EntityCriteriaCalculationResponse check(CyodaEventContext<EntityCriteriaCalculationRequest> request) {
        EntityCriteriaCalculationResponse response = super.check(request);
        response.setMatches(true);
        return response;
    }

}
