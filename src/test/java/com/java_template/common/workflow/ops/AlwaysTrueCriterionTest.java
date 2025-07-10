package com.java_template.common.workflow.ops;

import com.java_template.common.workflow.CyodaEventContext;
import com.java_template.common.workflow.OperationSpecification;
import io.cloudevents.v1.proto.CloudEvent;
import org.cyoda.cloud.api.event.common.ModelSpec;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AlwaysTrueCriterionTest {

    @Test
    void testSupports() {
        // Given
        AlwaysTrueCriterion criterion = new AlwaysTrueCriterion();
        ModelSpec modeKey = new ModelSpec();
        modeKey.setName("model");
        modeKey.setVersion(1);
        OperationSpecification.Criterion opsSpec = new OperationSpecification.Criterion(
                modeKey,
                "AlwaysTrueCriterion",
                "state",
                "transition",
                "workflow"
        );

        // When
        boolean supports = criterion.supports(opsSpec);

        // Then
        assertTrue(supports);

        // Given
        opsSpec = new OperationSpecification.Criterion(
                modeKey,
                "xxx",
                "state",
                "transition",
                "workflow"
        );

        supports = criterion.supports(opsSpec);

        // Then
        assertFalse(supports);
    }


    @Test
    void testCheck() {
        // Given
        AlwaysTrueCriterion criterion = new AlwaysTrueCriterion();
        CyodaEventContext<EntityCriteriaCalculationRequest> context = getEventContext();

        // When
        EntityCriteriaCalculationResponse response = criterion.check(context);

        // Then
        assertTrue(response.getMatches());
        assertEquals("123", response.getRequestId());
        assertEquals("456", response.getEntityId());
        assertTrue(response.getSuccess());
    }

    @NotNull
    private static CyodaEventContext<EntityCriteriaCalculationRequest> getEventContext() {
        EntityCriteriaCalculationRequest request = new EntityCriteriaCalculationRequest();
        request.setRequestId("123");
        request.setEntityId("456");

        CyodaEventContext<EntityCriteriaCalculationRequest> context = new CyodaEventContext<>() {
            @Override
            public CloudEvent getCloudEvent() {
                return mock(CloudEvent.class);
            }

            @Override
            public @NotNull EntityCriteriaCalculationRequest getEvent() {
                return request;
            }
        };
        return context;
    }
}