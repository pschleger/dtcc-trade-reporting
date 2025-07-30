package com.java_template.application.controller;

import com.java_template.application.dto.request.FpMLTradeConfirmationRequest;
import com.java_template.application.dto.response.ApiResponse;
import com.java_template.application.dto.response.TradeConfirmationResponse;
import com.java_template.application.service.TradeConfirmationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for FpML trade confirmation processing.
 * Provides endpoints for submitting and tracking trade confirmations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/trade-confirmations")
@RequiredArgsConstructor
@Tag(name = "Trade Confirmations", description = "FpML trade confirmation processing endpoints")
public class TradeConfirmationController {

    private final TradeConfirmationService tradeConfirmationService;

    /**
     * Submit an FpML trade confirmation for processing.
     *
     * @param request The FpML trade confirmation request
     * @param correlationId Optional correlation ID for tracking
     * @param bindingResult Validation results
     * @return Processing results
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Submit FpML trade confirmation",
            description = "Submit an FpML trade confirmation message for validation and processing"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Trade confirmation processed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public CompletableFuture<ResponseEntity<ApiResponse<TradeConfirmationResponse>>> submitTradeConfirmation(
            @Valid @RequestBody FpMLTradeConfirmationRequest request,
            @Parameter(description = "Correlation ID for request tracking")
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            BindingResult bindingResult) {

        // Generate correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;
        
        log.info("Received trade confirmation request - messageId: {}, correlationId: {}", 
                request.getMessageId(), correlationId);

        // Handle validation errors
        if (bindingResult.hasErrors()) {
            List<ApiResponse.ValidationError> validationErrors = bindingResult.getFieldErrors().stream()
                    .map(this::mapFieldError)
                    .collect(Collectors.toList());

            ApiResponse<TradeConfirmationResponse> errorResponse = ApiResponse.validationError(validationErrors, correlationId);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResponse));
        }

        // Process the trade confirmation
        return tradeConfirmationService.processTradeConfirmation(request, correlationId)
                .thenApply(result -> {
                    ApiResponse<TradeConfirmationResponse> response = ApiResponse.success(
                            result,
                            "Trade confirmation processed successfully",
                            finalCorrelationId
                    );
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error processing trade confirmation {}: {}", 
                            request.getMessageId(), throwable.getMessage(), throwable);
                    
                    ApiResponse<TradeConfirmationResponse> errorResponse = ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "INTERNAL_SERVER_ERROR",
                            "An error occurred while processing the trade confirmation: " + throwable.getMessage(),
                            finalCorrelationId
                    );
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                });
    }

    /**
     * Get the processing status of a trade confirmation.
     *
     * @param messageId The message ID to check
     * @param correlationId Optional correlation ID for tracking
     * @return Processing status
     */
    @GetMapping(value = "/{messageId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get trade confirmation status",
            description = "Retrieve the current processing status of a trade confirmation"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Trade confirmation not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> getTradeConfirmationStatus(
            @Parameter(description = "Message ID of the trade confirmation")
            @PathVariable String messageId,
            @Parameter(description = "Correlation ID for request tracking")
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        // Generate correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;
        
        log.info("Retrieving status for trade confirmation - messageId: {}, correlationId: {}", 
                messageId, correlationId);

        return tradeConfirmationService.getProcessingStatus(messageId)
                .thenApply(status -> {
                    Map<String, String> statusData = Map.of(
                            "messageId", messageId,
                            "status", status,
                            "timestamp", Instant.now().toString()
                    );
                    
                    ApiResponse<Map<String, String>> response = ApiResponse.success(
                            statusData,
                            "Status retrieved successfully",
                            finalCorrelationId
                    );
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving status for trade confirmation {}: {}", 
                            messageId, throwable.getMessage(), throwable);
                    
                    ApiResponse<Map<String, String>> errorResponse = ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "INTERNAL_SERVER_ERROR",
                            "An error occurred while retrieving the status: " + throwable.getMessage(),
                            finalCorrelationId
                    );
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                });
    }

    /**
     * Get trade confirmation details by message ID.
     *
     * @param messageId The message ID to retrieve
     * @param correlationId Optional correlation ID for tracking
     * @return Trade confirmation details
     */
    @GetMapping(value = "/{messageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get trade confirmation details",
            description = "Retrieve detailed information about a trade confirmation"
    )
    public CompletableFuture<ResponseEntity<ApiResponse<Object>>> getTradeConfirmation(
            @Parameter(description = "Message ID of the trade confirmation")
            @PathVariable String messageId,
            @Parameter(description = "Correlation ID for request tracking")
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {

        // Generate correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;
        
        log.info("Retrieving trade confirmation details - messageId: {}, correlationId: {}", 
                messageId, correlationId);

        return tradeConfirmationService.getTradeConfirmationByMessageId(messageId)
                .thenApply(entity -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            entity,
                            "Trade confirmation retrieved successfully",
                            finalCorrelationId
                    );
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving trade confirmation {}: {}", 
                            messageId, throwable.getMessage(), throwable);
                    
                    ApiResponse<Object> errorResponse = ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "INTERNAL_SERVER_ERROR",
                            "An error occurred while retrieving the trade confirmation: " + throwable.getMessage(),
                            finalCorrelationId
                    );
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                });
    }

    /**
     * Map a field error to a validation error DTO.
     */
    private ApiResponse.ValidationError mapFieldError(FieldError fieldError) {
        return ApiResponse.ValidationError.builder()
                .field(fieldError.getField())
                .code(fieldError.getCode())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }
}
