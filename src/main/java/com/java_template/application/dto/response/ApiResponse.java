package com.java_template.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standardized API response wrapper for all external interface endpoints.
 * Provides consistent response structure with success/error handling.
 *
 * @param <T> The type of data being returned
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates whether the request was successful.
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * HTTP status code.
     */
    @JsonProperty("statusCode")
    private Integer statusCode;

    /**
     * Human-readable message describing the result.
     */
    @JsonProperty("message")
    private String message;

    /**
     * The actual response data (null for error responses).
     */
    @JsonProperty("data")
    private T data;

    /**
     * Error information (null for successful responses).
     */
    @JsonProperty("error")
    private ErrorInfo error;

    /**
     * Validation errors (present when request validation fails).
     */
    @JsonProperty("validationErrors")
    private List<ValidationError> validationErrors;

    /**
     * Correlation ID for request tracking.
     */
    @JsonProperty("correlationId")
    private String correlationId;

    /**
     * Timestamp when the response was generated.
     */
    @JsonProperty("timestamp")
    private Instant timestamp;

    /**
     * Processing time in milliseconds.
     */
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    /**
     * API version.
     */
    @JsonProperty("apiVersion")
    private String apiVersion;

    /**
     * Error information structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        
        /**
         * Standardized error code.
         */
        @JsonProperty("code")
        private String code;

        /**
         * Human-readable error message.
         */
        @JsonProperty("message")
        private String message;

        /**
         * Additional error details.
         */
        @JsonProperty("details")
        private String details;

        /**
         * Reference to help documentation.
         */
        @JsonProperty("helpUrl")
        private String helpUrl;
    }

    /**
     * Validation error structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationError {
        
        /**
         * Field name that failed validation.
         */
        @JsonProperty("field")
        private String field;

        /**
         * Validation error code.
         */
        @JsonProperty("code")
        private String code;

        /**
         * Human-readable validation error message.
         */
        @JsonProperty("message")
        private String message;

        /**
         * Rejected value.
         */
        @JsonProperty("rejectedValue")
        private Object rejectedValue;
    }

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data, String message, String correlationId) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .message(message)
                .data(data)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .apiVersion("v1")
                .build();
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(int statusCode, String errorCode, String errorMessage, String correlationId) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .message("Request failed")
                .error(ErrorInfo.builder()
                        .code(errorCode)
                        .message(errorMessage)
                        .build())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .apiVersion("v1")
                .build();
    }

    /**
     * Create a validation error response.
     */
    public static <T> ApiResponse<T> validationError(List<ValidationError> validationErrors, String correlationId) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(400)
                .message("Validation failed")
                .validationErrors(validationErrors)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .apiVersion("v1")
                .build();
    }
}
