package com.java_template.application.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.java_template.common.workflow.CyodaEntity;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityCriteriaCalculationResponse;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Criterion serializer interface that integrates with sealed response builders.
 * Provides type-safe response building with sealed interface hierarchy.
 */
public interface CriterionSerializer {

    /**
     * Extracts a typed entity from the request payload.
     */
    <T extends CyodaEntity> T extractEntity(EntityCriteriaCalculationRequest request, Class<T> clazz);

    /**
     * Extracts raw JSON payload from the request.
     */
    JsonNode extractPayload(EntityCriteriaCalculationRequest request);

    /**
     * Converts a CyodaEntity to JsonNode.
     * This method allows criteria to convert entities for validation purposes.
     */
    <T extends CyodaEntity> JsonNode entityToJsonNode(T entity);

    /**
     * Gets the serializer type identifier.
     */
    String getType();

    /**
     * Executes a custom function with the serializer and returns the result.
     * This allows for flexible operations without modifying the interface.
     */
    default <R> R executeFunction(EntityCriteriaCalculationRequest request,
                                  java.util.function.Function<CriterionSerializer, R> function) {
        return function.apply(this);
    }

    /**
     * Creates a response builder for the given request.
     * This builder provides a simplified API for both match and error responses.
     */
    ResponseBuilder.CriterionResponseBuilder responseBuilder(EntityCriteriaCalculationRequest request);

    /**
     * Starts a fluent criterion evaluation chain with the given request.
     * This allows for a more expressive and chainable API.
     */
    default FluentCriterion withRequest(EntityCriteriaCalculationRequest request) {
        return new FluentCriterionImpl(this, request);
    }

    /**
     * Fluent API for criterion operations.
     * Provides a chainable interface for evaluating criteria and building responses.
     */
    interface FluentCriterion {
        /**
         * Evaluates the criterion using the provided predicate on the JSON payload.
         * @param evaluator Predicate to evaluate the JSON payload
         * @return FluentCriterion for chaining
         */
        FluentCriterion evaluate(Predicate<JsonNode> evaluator);

        /**
         * Evaluates the criterion using the provided predicate on the extracted entity.
         * @param clazz Entity class to extract
         * @param evaluator Predicate to evaluate the entity
         * @return FluentCriterion for chaining
         */
        <T extends CyodaEntity> FluentCriterion evaluateEntity(Class<T> clazz, Predicate<T> evaluator);

        /**
         * Completes the evaluation chain and returns the appropriate response.
         * @return The criterion response
         */
        EntityCriteriaCalculationResponse complete();

        /**
         * Provides error handling for the evaluation chain.
         * @param errorHandler Function to handle errors and create error responses
         * @return The criterion response (match, non-match, or error)
         */
        EntityCriteriaCalculationResponse orElseFail(BiFunction<Throwable, JsonNode, ErrorInfo> errorHandler);
    }

    /**
     * Error information for response building.
     */
    class ErrorInfo {
        private final String code;
        private final String message;

        public ErrorInfo(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String code() {
            return code;
        }

        public String message() {
            return message;
        }
    }

    /**
     * Implementation of the FluentCriterion interface.
     */
    class FluentCriterionImpl implements FluentCriterion {
        private final CriterionSerializer serializer;
        private final EntityCriteriaCalculationRequest request;
        private JsonNode payload;
        private Throwable error;
        private Boolean matches;

        FluentCriterionImpl(CriterionSerializer serializer, EntityCriteriaCalculationRequest request) {
            this.serializer = serializer;
            this.request = request;
            try {
                this.payload = serializer.extractPayload(request);
            } catch (Exception e) {
                this.error = e;
            }
        }

        @Override
        public FluentCriterion evaluate(Predicate<JsonNode> evaluator) {
            if (error == null && matches == null) {
                try {
                    matches = evaluator.test(payload);
                } catch (Exception e) {
                    error = e;
                }
            }
            return this;
        }

        @Override
        public <T extends CyodaEntity> FluentCriterion evaluateEntity(Class<T> clazz, Predicate<T> evaluator) {
            if (error == null && matches == null) {
                try {
                    T entity = serializer.extractEntity(request, clazz);
                    matches = evaluator.test(entity);
                } catch (Exception e) {
                    error = e;
                }
            }
            return this;
        }

        @Override
        public EntityCriteriaCalculationResponse complete() {
            if (error != null) {
                return serializer.responseBuilder(request)
                        .withError("EVALUATION_ERROR", error.getMessage())
                        .build();
            }

            if (matches == null) {
                return serializer.responseBuilder(request)
                        .withError("EVALUATION_ERROR", "No evaluation was performed")
                        .build();
            }

            return matches ?
                serializer.responseBuilder(request).withMatch().build() :
                serializer.responseBuilder(request).withNonMatch().build();
        }

        @Override
        public EntityCriteriaCalculationResponse orElseFail(BiFunction<Throwable, JsonNode, ErrorInfo> errorHandler) {
            if (error != null) {
                ErrorInfo errorInfo = errorHandler.apply(error, payload);
                return serializer.responseBuilder(request)
                        .withError(errorInfo.code(), errorInfo.message())
                        .build();
            }

            if (matches == null) {
                ErrorInfo errorInfo = errorHandler.apply(
                    new IllegalStateException("No evaluation was performed"),
                    payload
                );
                return serializer.responseBuilder(request)
                        .withError(errorInfo.code(), errorInfo.message())
                        .build();
            }

            return matches ?
                serializer.responseBuilder(request).withMatch().build() :
                serializer.responseBuilder(request).withNonMatch().build();
        }
    }
}
