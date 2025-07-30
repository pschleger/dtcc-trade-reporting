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
 * Response DTO for FpML trade confirmation processing results.
 * Contains the processing status and extracted trade information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeConfirmationResponse {

    /**
     * Unique message identifier from the request.
     */
    @JsonProperty("messageId")
    private String messageId;

    /**
     * Internal processing ID assigned by the system.
     */
    @JsonProperty("processingId")
    private String processingId;

    /**
     * Current processing status.
     */
    @JsonProperty("processingStatus")
    private String processingStatus;

    /**
     * Current validation status.
     */
    @JsonProperty("validationStatus")
    private String validationStatus;

    /**
     * Timestamp when the message was received.
     */
    @JsonProperty("receivedTimestamp")
    private Instant receivedTimestamp;

    /**
     * Timestamp when processing was completed.
     */
    @JsonProperty("processedTimestamp")
    private Instant processedTimestamp;

    /**
     * Validation results from FpML processing.
     */
    @JsonProperty("validationResults")
    private List<ValidationResult> validationResults;

    /**
     * Extracted trade information (if validation successful).
     */
    @JsonProperty("extractedTradeData")
    private ExtractedTradeData extractedTradeData;

    /**
     * Processing errors (if any occurred).
     */
    @JsonProperty("processingErrors")
    private List<ProcessingError> processingErrors;

    /**
     * Duplicate check results.
     */
    @JsonProperty("duplicateCheckResults")
    private DuplicateCheckResults duplicateCheckResults;

    /**
     * Links to related resources.
     */
    @JsonProperty("links")
    private ResponseLinks links;

    /**
     * Validation result structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationResult {
        
        @JsonProperty("validationType")
        private String validationType;

        @JsonProperty("severity")
        private String severity;

        @JsonProperty("errorCode")
        private String errorCode;

        @JsonProperty("errorMessage")
        private String errorMessage;

        @JsonProperty("fieldPath")
        private String fieldPath;

        @JsonProperty("expectedValue")
        private String expectedValue;

        @JsonProperty("actualValue")
        private String actualValue;
    }

    /**
     * Extracted trade data structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExtractedTradeData {
        
        @JsonProperty("tradeId")
        private String tradeId;

        @JsonProperty("uti")
        private String uti;

        @JsonProperty("usi")
        private String usi;

        @JsonProperty("tradeDate")
        private String tradeDate;

        @JsonProperty("effectiveDate")
        private String effectiveDate;

        @JsonProperty("maturityDate")
        private String maturityDate;

        @JsonProperty("notionalAmount")
        private String notionalAmount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("productType")
        private String productType;

        @JsonProperty("counterparties")
        private List<CounterpartyInfo> counterparties;
    }

    /**
     * Counterparty information structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CounterpartyInfo {
        
        @JsonProperty("lei")
        private String lei;

        @JsonProperty("role")
        private String role;

        @JsonProperty("tradingCapacity")
        private String tradingCapacity;
    }

    /**
     * Processing error structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProcessingError {
        
        @JsonProperty("errorCode")
        private String errorCode;

        @JsonProperty("errorMessage")
        private String errorMessage;

        @JsonProperty("timestamp")
        private Instant timestamp;

        @JsonProperty("retryable")
        private Boolean retryable;
    }

    /**
     * Duplicate check results structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DuplicateCheckResults {
        
        @JsonProperty("isDuplicate")
        private Boolean isDuplicate;

        @JsonProperty("originalMessageId")
        private String originalMessageId;

        @JsonProperty("duplicateCheckTimestamp")
        private Instant duplicateCheckTimestamp;

        @JsonProperty("duplicateCheckCriteria")
        private List<String> duplicateCheckCriteria;
    }

    /**
     * Response links structure.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseLinks {
        
        @JsonProperty("self")
        private String self;

        @JsonProperty("status")
        private String status;

        @JsonProperty("trade")
        private String trade;

        @JsonProperty("retry")
        private String retry;
    }
}
