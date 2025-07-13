package com.java_template.common.serializer;

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
         * Extracts an entity and initiates entity-based processing flow.
         * @param clazz Entity class to extract
         * @return FluentEntityProcessor for entity-based chaining
         */
        <T extends CyodaEntity> FluentEntityProcessor<T> toEntity(Class<T> clazz);

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
     * Fluent API for entity-based processor operations.
     * Provides a chainable interface for transforming entity instances and building responses.
     * This interface supports entity flows where processing operates on entity instances
     * rather than JsonNode objects.
     */
    interface FluentEntityProcessor<T extends CyodaEntity> {
        /**
         * Maps the current entity using the provided function.
         * @param mapper Function to transform the entity
         * @return FluentEntityProcessor for chaining
         */
        FluentEntityProcessor<T> map(Function<T, T> mapper);

        /**
         * Validates the current entity using the provided predicate.
         * If validation fails, the processing chain will error.
         * @param validator Predicate to validate the entity
         * @return FluentEntityProcessor for chaining
         */
        FluentEntityProcessor<T> validate(Function<T, Boolean> validator);

        /**
         * Validates the current entity with a custom error message.
         * @param validator Predicate to validate the entity
         * @param errorMessage Custom error message if validation fails
         * @return FluentEntityProcessor for chaining
         */
        FluentEntityProcessor<T> validate(Function<T, Boolean> validator, String errorMessage);

        /**
         * Switches back to JsonNode processing by converting the current entity.
         * @param converter Function to convert entity to JsonNode
         * @return FluentProcessor for JsonNode-based chaining
         */
        FluentProcessor toJsonFlow(Function<T, JsonNode> converter);

        /**
         * Completes the entity processing chain and returns the response.
         * The entity is automatically converted to JsonNode using the serializer.
         * @return EntityProcessorCalculationResponse
         */
        EntityProcessorCalculationResponse complete();

        /**
         * Completes the entity processing chain with a custom converter.
         * @param converter Function to convert the final entity to JsonNode
         * @return EntityProcessorCalculationResponse
         */
        EntityProcessorCalculationResponse complete(Function<T, JsonNode> converter);

        /**
         * Provides error handling for the entity processing chain.
         * @param errorHandler Function to handle errors and create error responses
         * @return The processor response (success or error)
         */
        EntityProcessorCalculationResponse orElseFail(BiFunction<Throwable, T, ErrorInfo> errorHandler);
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

        FluentProcessorImpl(ProcessorSerializer serializer, EntityProcessorCalculationRequest request, JsonNode data) {
            this.serializer = serializer;
            this.request = request;
            this.processedData = data;
            this.error = null;
        }

        FluentProcessorImpl(ProcessorSerializer serializer, EntityProcessorCalculationRequest request, Throwable error) {
            this.serializer = serializer;
            this.request = request;
            this.processedData = null;
            this.error = error;
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
        public <T extends CyodaEntity> FluentEntityProcessor<T> toEntity(Class<T> clazz) {
            if (error == null) {
                try {
                    T entity = serializer.extractEntity(request, clazz);
                    return new FluentEntityProcessorImpl<>(serializer, request, entity);
                } catch (Exception e) {
                    return new FluentEntityProcessorImpl<>(serializer, request, e);
                }
            }
            return new FluentEntityProcessorImpl<>(serializer, request, error);
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

    /**
     * Implementation of the FluentEntityProcessor interface.
     * Handles entity-based processing flows where operations work on entity instances.
     */
    class FluentEntityProcessorImpl<T extends CyodaEntity> implements FluentEntityProcessor<T> {
        private final ProcessorSerializer serializer;
        private final EntityProcessorCalculationRequest request;
        private T processedEntity;
        private Throwable error;

        FluentEntityProcessorImpl(ProcessorSerializer serializer, EntityProcessorCalculationRequest request, T entity) {
            this.serializer = serializer;
            this.request = request;
            this.processedEntity = entity;
            this.error = null;
        }

        FluentEntityProcessorImpl(ProcessorSerializer serializer, EntityProcessorCalculationRequest request, Throwable error) {
            this.serializer = serializer;
            this.request = request;
            this.processedEntity = null;
            this.error = error;
        }

        @Override
        public FluentEntityProcessor<T> map(Function<T, T> mapper) {
            if (error == null && processedEntity != null) {
                try {
                    processedEntity = mapper.apply(processedEntity);
                } catch (Exception e) {
                    error = e;
                }
            }
            return this;
        }

        @Override
        public FluentEntityProcessor<T> validate(Function<T, Boolean> validator) {
            return validate(validator, "Entity validation failed");
        }

        @Override
        public FluentEntityProcessor<T> validate(Function<T, Boolean> validator, String errorMessage) {
            if (error == null && processedEntity != null) {
                try {
                    Boolean isValid = validator.apply(processedEntity);
                    if (isValid == null || !isValid) {
                        error = new IllegalArgumentException(errorMessage);
                    }
                } catch (Exception e) {
                    error = e;
                }
            }
            return this;
        }

        @Override
        public FluentProcessor toJsonFlow(Function<T, JsonNode> converter) {
            if (error == null && processedEntity != null) {
                try {
                    JsonNode jsonData = converter.apply(processedEntity);
                    return new FluentProcessorImpl(serializer, request, jsonData);
                } catch (Exception e) {
                    return new FluentProcessorImpl(serializer, request, e);
                }
            }
            return new FluentProcessorImpl(serializer, request, error);
        }

        @Override
        public EntityProcessorCalculationResponse complete() {
            if (error != null) {
                return serializer.responseBuilder(request)
                        .withError("PROCESSING_ERROR", error.getMessage())
                        .build();
            }
            if (processedEntity == null) {
                return serializer.responseBuilder(request)
                        .withError("PROCESSING_ERROR", "Entity is null")
                        .build();
            }
            try {
                JsonNode entityJson = serializer.entityToJsonNode(processedEntity);
                return serializer.responseBuilder(request)
                        .withSuccess(entityJson)
                        .build();
            } catch (Exception e) {
                return serializer.responseBuilder(request)
                        .withError("CONVERSION_ERROR", e.getMessage())
                        .build();
            }
        }

        @Override
        public EntityProcessorCalculationResponse complete(Function<T, JsonNode> converter) {
            if (error != null) {
                return serializer.responseBuilder(request)
                        .withError("PROCESSING_ERROR", error.getMessage())
                        .build();
            }
            if (processedEntity == null) {
                return serializer.responseBuilder(request)
                        .withError("PROCESSING_ERROR", "Entity is null")
                        .build();
            }
            try {
                JsonNode entityJson = converter.apply(processedEntity);
                return serializer.responseBuilder(request)
                        .withSuccess(entityJson)
                        .build();
            } catch (Exception e) {
                return serializer.responseBuilder(request)
                        .withError("CONVERSION_ERROR", e.getMessage())
                        .build();
            }
        }

        @Override
        public EntityProcessorCalculationResponse orElseFail(BiFunction<Throwable, T, ErrorInfo> errorHandler) {
            if (error != null) {
                ErrorInfo errorInfo = errorHandler.apply(error, processedEntity);
                return serializer.responseBuilder(request)
                        .withError(errorInfo.code(), errorInfo.message())
                        .build();
            }
            try {
                JsonNode entityJson = serializer.entityToJsonNode(processedEntity);
                return serializer.responseBuilder(request)
                        .withSuccess(entityJson)
                        .build();
            } catch (Exception e) {
                return serializer.responseBuilder(request)
                        .withError("CONVERSION_ERROR", e.getMessage())
                        .build();
            }
        }
    }
}
