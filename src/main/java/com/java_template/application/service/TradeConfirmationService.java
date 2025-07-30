package com.java_template.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.java_template.application.dto.request.FpMLTradeConfirmationRequest;
import com.java_template.application.dto.response.TradeConfirmationResponse;
import com.java_template.common.service.EntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.java_template.common.config.Config.ENTITY_VERSION;

/**
 * Service for managing trade confirmation entities in Cyoda.
 * Handles the creation and persistence of TradeConfirmation entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeConfirmationService {

    private final EntityService entityService;
    private final FpMLProcessingService fpmlProcessingService;
    private final ObjectMapper objectMapper;

    private static final String TRADE_CONFIRMATION_MODEL = "TradeConfirmation";

    /**
     * Process and persist a trade confirmation message.
     * Creates a TradeConfirmation entity in Cyoda and triggers the workflow.
     *
     * @param request The FpML trade confirmation request
     * @param correlationId Correlation ID for tracking
     * @return Processing results
     */
    public CompletableFuture<TradeConfirmationResponse> processTradeConfirmation(
            FpMLTradeConfirmationRequest request, String correlationId) {
        
        log.info("Processing trade confirmation with messageId: {} and correlationId: {}", 
                request.getMessageId(), correlationId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Process FpML message
                TradeConfirmationResponse processingResult = fpmlProcessingService.processFpMLMessage(request);
                
                // Create TradeConfirmation entity
                ObjectNode tradeConfirmationEntity = createTradeConfirmationEntity(request, processingResult, correlationId);
                
                // Persist entity in Cyoda
                UUID entityId = entityService.addItem(TRADE_CONFIRMATION_MODEL, ENTITY_VERSION, tradeConfirmationEntity)
                        .join();
                
                log.info("Created TradeConfirmation entity with ID: {} for messageId: {}", 
                        entityId, request.getMessageId());
                
                // Update processing result with entity information
                processingResult.setProcessingId(entityId.toString());
                
                return processingResult;
                
            } catch (Exception e) {
                log.error("Error processing trade confirmation {}: {}", request.getMessageId(), e.getMessage(), e);
                
                return TradeConfirmationResponse.builder()
                        .messageId(request.getMessageId())
                        .processingId(UUID.randomUUID().toString())
                        .processingStatus("FAILED")
                        .validationStatus("SYSTEM_ERROR")
                        .receivedTimestamp(Instant.now())
                        .processedTimestamp(Instant.now())
                        .processingErrors(java.util.List.of(
                                TradeConfirmationResponse.ProcessingError.builder()
                                        .errorCode("SYSTEM_ERROR")
                                        .errorMessage("System error during processing: " + e.getMessage())
                                        .timestamp(Instant.now())
                                        .retryable(true)
                                        .build()
                        ))
                        .build();
            }
        });
    }

    /**
     * Create a TradeConfirmation entity from the request and processing results.
     */
    private ObjectNode createTradeConfirmationEntity(
            FpMLTradeConfirmationRequest request, 
            TradeConfirmationResponse processingResult,
            String correlationId) {
        
        try {
            ObjectNode entity = objectMapper.createObjectNode();
            
            // Basic message information
            entity.put("messageId", request.getMessageId());
            entity.put("messageType", request.getMessageType());
            entity.put("fpmlVersion", request.getFpmlVersion());
            entity.put("senderId", request.getSenderLei());
            entity.put("senderLei", request.getSenderLei());
            
            if (request.getReceiverLei() != null) {
                entity.put("receiverId", request.getReceiverLei());
                entity.put("receiverLei", request.getReceiverLei());
            }
            
            // Timestamps
            entity.put("receivedTimestamp", Instant.now().toString());
            entity.put("messageTimestamp", Instant.now().toString());
            
            // FpML content and metadata
            entity.put("fpmlContent", request.getFpmlContent());
            entity.put("messageSize", calculateMessageSize(request.getFpmlContent()));
            entity.put("messageHash", calculateMessageHash(request.getFpmlContent()));
            
            // Processing status
            entity.put("validationStatus", processingResult.getValidationStatus());
            entity.put("processingStatus", processingResult.getProcessingStatus());
            
            // Validation results
            if (processingResult.getValidationResults() != null) {
                entity.set("validationResults", objectMapper.valueToTree(processingResult.getValidationResults()));
            }
            
            // Processing results
            ObjectNode processingResults = objectMapper.createObjectNode();
            if (processingResult.getExtractedTradeData() != null) {
                TradeConfirmationResponse.ExtractedTradeData extractedData = processingResult.getExtractedTradeData();
                if (extractedData.getTradeId() != null) {
                    processingResults.put("extractedTradeId", extractedData.getTradeId());
                }
                if (extractedData.getUti() != null) {
                    processingResults.put("extractedUti", extractedData.getUti());
                }
                if (extractedData.getUsi() != null) {
                    processingResults.put("extractedUsi", extractedData.getUsi());
                }
            }
            
            if (processingResult.getProcessingErrors() != null && !processingResult.getProcessingErrors().isEmpty()) {
                processingResults.set("processingErrors", objectMapper.valueToTree(processingResult.getProcessingErrors()));
            }
            
            entity.set("processingResults", processingResults);
            
            // Correlation and tracking
            if (correlationId != null) {
                entity.put("correlationId", correlationId);
            }
            
            if (request.getParentMessageId() != null) {
                entity.put("parentMessageId", request.getParentMessageId());
            }
            
            // Duplicate check results
            if (processingResult.getDuplicateCheckResults() != null) {
                entity.set("duplicateCheckResults", objectMapper.valueToTree(processingResult.getDuplicateCheckResults()));
            }
            
            // Metadata for entity
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("entityType", TRADE_CONFIRMATION_MODEL);
            metadata.put("entityVersion", ENTITY_VERSION);
            metadata.put("createdTimestamp", Instant.now().toString());
            metadata.put("workflowState", "received");
            entity.set("metadata", metadata);
            
            return entity;
            
        } catch (Exception e) {
            log.error("Error creating TradeConfirmation entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create TradeConfirmation entity", e);
        }
    }

    /**
     * Calculate the size of the message in bytes.
     */
    private int calculateMessageSize(String base64Content) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            return decodedBytes.length;
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode base64 content for size calculation");
            return base64Content.length();
        }
    }

    /**
     * Calculate SHA-256 hash of the message content.
     */
    private String calculateMessageHash(String base64Content) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(decodedBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            log.error("Error calculating message hash: {}", e.getMessage(), e);
            return "hash_calculation_failed";
        }
    }

    /**
     * Retrieve a trade confirmation by message ID.
     */
    public CompletableFuture<JsonNode> getTradeConfirmationByMessageId(String messageId) {
        log.info("Retrieving trade confirmation with messageId: {}", messageId);
        
        // This would typically use a search condition to find by messageId
        // For now, returning a placeholder implementation
        return CompletableFuture.supplyAsync(() -> {
            try {
                // In a real implementation, you would search for the entity by messageId
                // using entityService.getItemsByCondition with appropriate search criteria
                return objectMapper.createObjectNode().put("messageId", messageId);
            } catch (Exception e) {
                log.error("Error retrieving trade confirmation {}: {}", messageId, e.getMessage(), e);
                throw new RuntimeException("Failed to retrieve trade confirmation", e);
            }
        });
    }

    /**
     * Get processing status for a trade confirmation.
     */
    public CompletableFuture<String> getProcessingStatus(String messageId) {
        return getTradeConfirmationByMessageId(messageId)
                .thenApply(entity -> {
                    if (entity.has("processingStatus")) {
                        return entity.get("processingStatus").asText();
                    }
                    return "UNKNOWN";
                });
    }
}
