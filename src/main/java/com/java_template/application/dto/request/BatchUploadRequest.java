package com.java_template.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for batch FpML file upload processing.
 * Supports uploading multiple FpML files for batch processing.
 */
@Data
public class BatchUploadRequest {

    /**
     * Batch identifier provided by the sender.
     */
    @NotBlank(message = "Batch ID is required")
    @Size(max = 100, message = "Batch ID must not exceed 100 characters")
    @JsonProperty("batchId")
    private String batchId;

    /**
     * LEI of the batch sender.
     */
    @NotBlank(message = "Sender LEI is required")
    @JsonProperty("senderLei")
    private String senderLei;

    /**
     * Optional correlation ID for tracking the batch.
     */
    @Size(max = 100, message = "Correlation ID must not exceed 100 characters")
    @JsonProperty("correlationId")
    private String correlationId;

    /**
     * Optional business date for the batch.
     */
    @JsonProperty("businessDate")
    private String businessDate;

    /**
     * Processing options for the batch.
     */
    @JsonProperty("processingOptions")
    private BatchProcessingOptions processingOptions;

    /**
     * The uploaded file containing FpML messages.
     * This field is populated from the multipart request.
     */
    @NotNull(message = "File is required")
    private MultipartFile file;

    /**
     * Batch processing options.
     */
    @Data
    public static class BatchProcessingOptions {
        
        /**
         * Whether to continue processing if individual messages fail.
         */
        @JsonProperty("continueOnError")
        private Boolean continueOnError = true;

        /**
         * Maximum number of messages to process from the batch.
         */
        @JsonProperty("maxMessages")
        private Integer maxMessages;

        /**
         * Whether to validate all messages before processing any.
         */
        @JsonProperty("validateFirst")
        private Boolean validateFirst = false;

        /**
         * Priority level for batch processing.
         */
        @JsonProperty("priority")
        private String priority = "NORMAL";

        /**
         * Whether to send notifications on completion.
         */
        @JsonProperty("notifyOnCompletion")
        private Boolean notifyOnCompletion = false;

        /**
         * Email address for completion notifications.
         */
        @JsonProperty("notificationEmail")
        private String notificationEmail;
    }
}
