package com.java_template.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.application.dto.request.FpMLTradeConfirmationRequest;
import com.java_template.application.dto.response.TradeConfirmationResponse;
import com.java_template.application.service.TradeConfirmationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TradeConfirmationController.
 */
@WebMvcTest(TradeConfirmationController.class)
@Import(com.java_template.common.config.SecurityConfig.class)
class TradeConfirmationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TradeConfirmationService tradeConfirmationService;

    private FpMLTradeConfirmationRequest validRequest;
    private TradeConfirmationResponse mockResponse;

    @BeforeEach
    void setUp() {
        // Create a valid test request with proper 20-character LEI codes
        validRequest = new FpMLTradeConfirmationRequest();
        validRequest.setMessageId("TEST-MSG-001");
        validRequest.setMessageType("TRADE_CONFIRMATION");
        validRequest.setFpmlVersion("5.12");
        validRequest.setSenderLei("1234567890ABCDEFGH12");      // 20 chars: 18 alphanumeric + 2 digits
        validRequest.setReceiverLei("ZYXWVUTSRQ9876543201");    // 20 chars: 18 alphanumeric + 2 digits
        
        // Create base64 encoded sample FpML content
        String sampleFpML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><FpML><trade><tradeId>TEST-001</tradeId></trade></FpML>";
        validRequest.setFpmlContent(Base64.getEncoder().encodeToString(sampleFpML.getBytes()));
        validRequest.setCorrelationId("CORR-001");

        // Create mock response
        mockResponse = TradeConfirmationResponse.builder()
                .messageId("TEST-MSG-001")
                .processingId("PROC-001")
                .processingStatus("PROCESSED")
                .validationStatus("VALID")
                .receivedTimestamp(Instant.now())
                .processedTimestamp(Instant.now())
                .validationResults(List.of(
                        TradeConfirmationResponse.ValidationResult.builder()
                                .validationType("SCHEMA")
                                .severity("INFO")
                                .errorCode("VALIDATION_SUCCESS")
                                .errorMessage("FpML document structure is valid")
                                .build()
                ))
                .extractedTradeData(TradeConfirmationResponse.ExtractedTradeData.builder()
                        .tradeId("TEST-001")
                        .uti("UTI-TEST-001")
                        .currency("USD")
                        .build())
                .duplicateCheckResults(TradeConfirmationResponse.DuplicateCheckResults.builder()
                        .isDuplicate(false)
                        .duplicateCheckTimestamp(Instant.now())
                        .build())
                .build();
    }

    @Test
    @WithMockUser
    void submitTradeConfirmation_ValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        when(tradeConfirmationService.processTradeConfirmation(any(FpMLTradeConfirmationRequest.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When & Then
        mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header("X-Correlation-ID", "CORR-001"))
                .andExpect(status().isOk());
    }
}
