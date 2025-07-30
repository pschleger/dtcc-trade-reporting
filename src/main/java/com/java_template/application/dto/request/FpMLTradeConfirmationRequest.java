package com.java_template.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for FpML trade confirmation submission.
 * Represents the incoming request structure for processing FpML XML messages.
 */
@Data
public class FpMLTradeConfirmationRequest {

    /**
     * Unique message identifier provided by the sender.
     */
    @NotBlank(message = "Message ID is required")
    @Size(max = 100, message = "Message ID must not exceed 100 characters")
    @JsonProperty("messageId")
    private String messageId;

    /**
     * Type of FpML message being submitted.
     */
    @NotBlank(message = "Message type is required")
    @JsonProperty("messageType")
    private String messageType;

    /**
     * FpML version used in the message.
     */
    @NotBlank(message = "FpML version is required")
    @Pattern(regexp = "^[0-9]+\\.[0-9]+$", message = "FpML version must be in format X.Y")
    @JsonProperty("fpmlVersion")
    private String fpmlVersion;

    /**
     * LEI of the message sender.
     */
    @NotBlank(message = "Sender LEI is required")
    @Pattern(regexp = "^[A-Z0-9]{18}[0-9]{2}$", message = "Sender LEI must be a valid 20-character LEI code")
    @JsonProperty("senderLei")
    private String senderLei;

    /**
     * LEI of the message receiver.
     */
    @Pattern(regexp = "^[A-Z0-9]{18}[0-9]{2}$", message = "Receiver LEI must be a valid 20-character LEI code")
    @JsonProperty("receiverLei")
    private String receiverLei;

    /**
     * Raw FpML XML content (base64 encoded).
     */
    @NotBlank(message = "FpML content is required")
    @JsonProperty("fpmlContent")
    private String fpmlContent;

    /**
     * Optional correlation ID for tracking related messages.
     */
    @Size(max = 100, message = "Correlation ID must not exceed 100 characters")
    @JsonProperty("correlationId")
    private String correlationId;

    /**
     * Optional parent message ID for amendments or cancellations.
     */
    @Size(max = 100, message = "Parent message ID must not exceed 100 characters")
    @JsonProperty("parentMessageId")
    private String parentMessageId;

    /**
     * Optional business date override.
     */
    @JsonProperty("businessDate")
    private String businessDate;

    /**
     * Optional processing flags for special handling.
     */
    @JsonProperty("processingFlags")
    private ProcessingFlags processingFlags;

    /**
     * Processing flags for special handling instructions.
     */
    @Data
    public static class ProcessingFlags {
        
        /**
         * Skip duplicate checking if true.
         */
        @JsonProperty("skipDuplicateCheck")
        private Boolean skipDuplicateCheck = false;

        /**
         * Force processing even if validation warnings exist.
         */
        @JsonProperty("forceProcessing")
        private Boolean forceProcessing = false;

        /**
         * Enable strict validation mode.
         */
        @JsonProperty("strictValidation")
        private Boolean strictValidation = false;

        /**
         * Priority level for processing (HIGH, NORMAL, LOW).
         */
        @JsonProperty("priority")
        private String priority = "NORMAL";
    }
}
