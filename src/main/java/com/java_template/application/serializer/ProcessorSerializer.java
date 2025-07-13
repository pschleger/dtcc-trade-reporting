package com.java_template.application.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.java_template.common.workflow.CyodaEntity;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationRequest;
import org.cyoda.cloud.api.event.processing.EntityProcessorCalculationResponse;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Processor serializer interface that integrates with sealed response builders.
 * Provides type-safe response building with sealed interface hierarchy.
 */
public interface ProcessorSerializer {

    /**
     * Extracts a typed entity from the request payload.
     */
    <T extends CyodaEntity> T extractEntity(EntityProcessorCalculationRequest request, Class<T> clazz);

    /**
     * Extracts raw JSON payload from the request.
     */
    JsonNode extractPayload(EntityProcessorCalculationRequest request);

    /**
     * Converts a CyodaEntity to JsonNode.
     * This method allows processors to convert entities for use with withEntity method.
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
    default <R> R executeFunction(EntityProcessorCalculationRequest request,
                                  Function<ProcessorSerializer, R> function) {
        return function.apply(this);
    }

    /**
     * Creates a response builder for the given request.
     * This builder provides a simplified API for both success and error responses.
     */
    ResponseBuilder.ProcessorResponseBuilder responseBuilder(EntityProcessorCalculationRequest request);

    /**
     * Starts a fluent processing chain with the given request.
     * This allows for a more expressive and chainable API.
     */
    default FluentProcessor withRequest(EntityProcessorCalculationRequest request) {
        return new FluentProcessorImpl(this, request);
    }

    /**
     * Fluent API for processor operations.
     * Provides a chainable interface for transforming data and building responses.
     */
    interface FluentProcessor {
        /**
         * Maps the extracted payload using the provided function.
         * @param mapper Function to transform the JSON payload
         * @return FluentProcessor for chaining
         */
        FluentProcessor map(Function<JsonNode, JsonNode> mapper);

        /**
         * Maps the extracted entity using the provided function.
         * @param clazz Entity class to extract
         * @param mapper Function to transform the entity
         * @return FluentProcessor for chaining
         */
        <T extends CyodaEntity> FluentProcessor mapEntity(Class<T> clazz, Function<T, JsonNode> mapper);

        /**
         * Completes the processing chain and returns a successful response.
         * @return The successful processor response
         */
        EntityProcessorCalculationResponse complete();

        /**
         * Provides error handling for the processing chain.
         * @param errorHandler Function to handle errors and create error responses
         * @return The processor response (success or error)
         */
        EntityProcessorCalculationResponse orElseFail(BiFunction<Throwable, JsonNode, ErrorInfo> errorHandler);
    }

    /**
         * Error information for response building.
         */
        record ErrorInfo(String code, String message) {
    }

    /**
     * Implementation of the FluentProcessor interface.
     */
    class FluentProcessorImpl implements FluentProcessor {
        private final ProcessorSerializer serializer;
        private final EntityProcessorCalculationRequest request;
        private JsonNode processedData;
        private Throwable error;

        FluentProcessorImpl(ProcessorSerializer serializer, EntityProcessorCalculationRequest request) {
            this.serializer = serializer;
            this.request = request;
            try {
                this.processedData = serializer.extractPayload(request);
            } catch (Exception e) {
                this.error = e;
            }
        }

        @Override
        public FluentProcessor map(Function<JsonNode, JsonNode> mapper) {
            if (error == null) {
                try {
                    processedData = mapper.apply(processedData);
                } catch (Exception e) {
                    error = e;
                }
            }
            return this;
        }

        @Override
        public <T extends CyodaEntity> FluentProcessor mapEntity(Class<T> clazz, Function<T, JsonNode> mapper) {
            if (error == null) {
                try {
                    T entity = serializer.extractEntity(request, clazz);
                    processedData = mapper.apply(entity);
                } catch (Exception e) {
                    error = e;
                }
            }
            return this;
        }

        @Override
        public EntityProcessorCalculationResponse complete() {
            if (error != null) {
                return serializer.responseBuilder(request)
                        .withError("PROCESSING_ERROR", error.getMessage())
                        .build();
            }
            return serializer.responseBuilder(request)
                    .withSuccess(processedData)
                    .build();
        }

        @Override
        public EntityProcessorCalculationResponse orElseFail(BiFunction<Throwable, JsonNode, ErrorInfo> errorHandler) {
            if (error != null) {
                ErrorInfo errorInfo = errorHandler.apply(error, processedData);
                return serializer.responseBuilder(request)
                        .withError(errorInfo.code(), errorInfo.message())
                        .build();
            }
            return serializer.responseBuilder(request)
                    .withSuccess(processedData)
                    .build();
        }
    }
}
