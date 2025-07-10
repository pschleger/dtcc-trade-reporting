package com.java_template.common.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cyoda.cloud.api.event.common.statemachine.TransitionInfo;
import org.cyoda.cloud.api.event.common.statemachine.WorkflowInfo;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * ABOUTME: Sealed class hierarchy representing different types of operations.
 */
public sealed class OperationSpecification
    permits OperationSpecification.Entity, OperationSpecification.Criterion, OperationSpecification.Processor {

    private final @NotNull ModelKey modelKey;

    protected OperationSpecification(@NotNull ModelKey modelKey) {
        this.modelKey = modelKey;
    }

    public @NotNull ModelKey modelKey() {
        return modelKey;
    }

    /**
     * Represents a basic entity operation without workflow context.
     */
    public static final class Entity extends OperationSpecification {

        public Entity(@NotNull ModelKey modelKey) {
            super(modelKey);
        }
    }

    /**
     * Represents a criterion-based transition operation.
     */
    public static final class Criterion extends OperationSpecification {
        private final @NotNull String criterionName;
        private final @Nullable String transitionName;
        private final @Nullable String workflowName;

        public Criterion(@NotNull ModelKey modelKey,
                         @NotNull String criterionName,
                         @Nullable String transitionName,
                         @Nullable String workflowName) {
            super(modelKey);
            this.criterionName = criterionName;
            this.transitionName = transitionName;
            this.workflowName = workflowName;
        }

        public @NotNull String criterionName() {
            return criterionName;
        }

        public @Nullable String transitionName() {
            return transitionName;
        }

        public @Nullable String workflowName() {
            return workflowName;
        }

        /**
         * Gets the criterion name for this operation.
         * @return the criterion name
         */
        public String getCriterionName() {
            return criterionName();
        }
    }

    /**
     * Represents a processor-based transition operation.
     */
    public static final class Processor extends OperationSpecification {
        private final @NotNull String processorName;
        private final @Nullable String transitionName;
        private final @Nullable String workflowName;

        public Processor(@NotNull ModelKey modelKey,
                         @NotNull String processorName,
                         @Nullable String transitionName,
                         @Nullable String workflowName) {
            super(modelKey);
            this.processorName = processorName;
            this.transitionName = transitionName;
            this.workflowName = workflowName;
        }

        public @NotNull String processorName() {
            return processorName;
        }

        public @Nullable String transitionName() {
            return transitionName;
        }

        public @Nullable String workflowName() {
            return workflowName;
        }

        /**
         * Gets the processor name for this operation.
         * @return the processor name
         */
        public String getProcessorName() {
            return processorName();
        }
    }

    public static Processor create(EntityProcessorCalculationRequest request) {
        ModelKey modelKey = extractModelKey(request);

        // TODO This is just a suggestion of things to add. Need to agree what we really should pack up.
        String processorName = request.getProcessorName();
        String transitionName = Optional.ofNullable(request.getTransition()).map(TransitionInfo::getName).orElse(null);
        String workflowName = Optional.ofNullable(request.getWorkflow()).map(WorkflowInfo::getName).orElse(null);

        return new Processor(modelKey, processorName, transitionName, workflowName);
    }

    public static Criterion create(EntityCriteriaCalculationRequest request) {
        ModelKey modelKey = extractModelKey(request);

        // TODO This is just a suggestion of things to add. Need to agree what we really should pack up.
        String criteriaName = request.getCriteriaName();
        String transitionName = Optional.ofNullable(request.getTransition()).map(TransitionInfo::getName).orElse(null);
        String workflowName = Optional.ofNullable(request.getWorkflow()).map(WorkflowInfo::getName).orElse(null);

        return new Criterion(modelKey, criteriaName, transitionName, workflowName);
    }

    private static @NotNull ModelKey extractModelKey(EntityProcessorCalculationRequest request) {
        ObjectNode payload = (ObjectNode) request.getPayload().getData();
        return getModelKey(payload);
    }

    private static @NotNull ModelKey extractModelKey(EntityCriteriaCalculationRequest request) {
        ObjectNode payload = (ObjectNode) request.getPayload().getData();
        return getModelKey(payload);
    }

    @NotNull
    private static ModelKey getModelKey(ObjectNode payload) {
        if (!payload.has("metadata")) {
            throw new IllegalArgumentException("Payload does not contain metadata");
        }
        if (!payload.has("entityName")) {
            throw new IllegalArgumentException("Payload does not have entityName in metadata");
        }
        if (!payload.has("entityVersion")) {
            throw new IllegalArgumentException("Payload does not have entityVersion in metadata");
        }

        ObjectNode metadata = (ObjectNode) payload.get("metadata");
        String entityName = metadata.path("entityName").asText(null);
        String entityVersion = metadata.path("entityVersion").asText(null);

        return new ModelKey(entityName, entityVersion);
    }

}
