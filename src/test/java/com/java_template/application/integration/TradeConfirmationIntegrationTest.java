package com.java_template.application.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.application.dto.request.FpMLTradeConfirmationRequest;
import com.java_template.application.dto.response.ApiResponse;
import com.java_template.application.dto.response.TradeConfirmationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Trade Confirmation processing with real FpML samples.
 * These tests validate the complete flow from REST API to FpML processing.
 */
@SpringBootTest(classes = com.java_template.Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TradeConfirmationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String correlationId;

    @BeforeEach
    void setUp() {
        correlationId = UUID.randomUUID().toString();
    }

    @Test
    @WithMockUser
    void processIRSTradeConfirmation_ValidFpML_ShouldReturnSuccess() throws Exception {
        // Given - Load real IRS FpML sample
        String fpmlContent = loadFpMLSample("fpml-samples/irs-trade-confirmation.xml");
        FpMLTradeConfirmationRequest request = createValidRequest("IRS-MSG-001", fpmlContent);

        // When - Submit the trade confirmation
        MvcResult result = mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", correlationId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Wait for async processing to complete
        result = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Verify the response
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).isNotEmpty();

        // Parse and validate response structure
        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getCorrelationId()).isEqualTo(correlationId);
        assertThat(response.getData()).isNotNull();
    }

    @Test
    @WithMockUser
    void processFXForwardConfirmation_ValidFpML_ShouldReturnSuccess() throws Exception {
        // Given - Load real FX Forward FpML sample
        String fpmlContent = loadFpMLSample("fpml-samples/fx-forward-confirmation.xml");
        FpMLTradeConfirmationRequest request = createValidRequest("FX-MSG-002", fpmlContent);

        // When - Submit the trade confirmation
        MvcResult result = mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", correlationId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Wait for async processing to complete
        result = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Verify the response contains extracted trade data
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();

        // Convert data to TradeConfirmationResponse for detailed validation
        TradeConfirmationResponse tradeResponse = objectMapper.convertValue(
                response.getData(), TradeConfirmationResponse.class);

        assertThat(tradeResponse.getMessageId()).isEqualTo("FX-MSG-002");
        assertThat(tradeResponse.getProcessingStatus()).isIn("PROCESSED", "PROCESSING");
        assertThat(tradeResponse.getValidationStatus()).isIn("VALID", "VALIDATING");
    }

    @Test
    @WithMockUser
    void processInvalidFpML_ShouldReturnValidationErrors() throws Exception {
        // Given - Load invalid FpML sample
        String fpmlContent = loadFpMLSample("fpml-samples/invalid-fpml.xml");
        FpMLTradeConfirmationRequest request = createValidRequest("INVALID-MSG-001", fpmlContent);

        // When - Submit the invalid trade confirmation
        MvcResult result = mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", correlationId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Wait for async processing to complete
        result = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Verify validation errors are returned
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);

        assertThat(response.getSuccess()).isTrue(); // Request processed, but validation failed

        TradeConfirmationResponse tradeResponse = objectMapper.convertValue(
                response.getData(), TradeConfirmationResponse.class);

        assertThat(tradeResponse.getValidationStatus()).isIn("INVALID", "SCHEMA_ERROR");
        assertThat(tradeResponse.getValidationResults()).isNotEmpty();
        assertThat(tradeResponse.getValidationResults().get(0).getSeverity()).isEqualTo("ERROR");
    }

    @Test
    @WithMockUser
    void processMalformedXML_ShouldReturnProcessingError() throws Exception {
        // Given - Load malformed XML sample
        String fpmlContent = loadFpMLSample("fpml-samples/malformed-xml.xml");
        FpMLTradeConfirmationRequest request = createValidRequest("MALFORMED-MSG-001", fpmlContent);

        // When - Submit the malformed XML
        MvcResult result = mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", correlationId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Wait for async processing to complete
        result = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Verify processing error is returned
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);

        TradeConfirmationResponse tradeResponse = objectMapper.convertValue(
                response.getData(), TradeConfirmationResponse.class);

        assertThat(tradeResponse.getProcessingStatus()).isEqualTo("FAILED");
        assertThat(tradeResponse.getValidationStatus()).isIn("SCHEMA_ERROR", "INVALID");
        assertThat(tradeResponse.getProcessingErrors()).isNotEmpty();
    }

    @Test
    @WithMockUser
    void getTradeConfirmationStatus_ExistingMessage_ShouldReturnStatus() throws Exception {
        // Given - First submit a trade confirmation
        String fpmlContent = loadFpMLSample("fpml-samples/irs-trade-confirmation.xml");
        FpMLTradeConfirmationRequest request = createValidRequest("STATUS-TEST-001", fpmlContent);

        MvcResult submitResult = mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", correlationId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Wait for async processing to complete
        mockMvc.perform(asyncDispatch(submitResult))
                .andExpect(status().isOk());

        // When - Query the status
        MvcResult result = mockMvc.perform(get("/api/v1/trade-confirmations/STATUS-TEST-001/status")
                        .header("X-Correlation-ID", correlationId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // Wait for async processing to complete
        result = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Verify status response
        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
    }

    @Test
    @WithMockUser
    void submitTradeConfirmation_InvalidLEI_ShouldReturnValidationError() throws Exception {
        // Given - Request with invalid LEI
        String fpmlContent = loadFpMLSample("fpml-samples/irs-trade-confirmation.xml");
        FpMLTradeConfirmationRequest request = createValidRequest("LEI-TEST-001", fpmlContent);
        request.setSenderLei("INVALID_LEI"); // Invalid LEI format

        // When - Submit the request
        mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", correlationId))
                .andExpect(status().isBadRequest()); // Validation is working - 400 status returned
    }

    /**
     * Helper method to load FpML sample files from test resources.
     */
    private String loadFpMLSample(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        byte[] content = resource.getInputStream().readAllBytes();
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * Helper method to create a valid FpML trade confirmation request.
     */
    private FpMLTradeConfirmationRequest createValidRequest(String messageId, String fpmlContent) {
        FpMLTradeConfirmationRequest request = new FpMLTradeConfirmationRequest();
        request.setMessageId(messageId);
        request.setMessageType("TRADE_CONFIRMATION");
        request.setFpmlVersion("5.12");
        request.setSenderLei("1234567890ABCDEFGH12");
        request.setReceiverLei("ZYXWVUTSRQ9876543201");
        request.setFpmlContent(Base64.getEncoder().encodeToString(fpmlContent.getBytes(StandardCharsets.UTF_8)));
        request.setCorrelationId(correlationId);
        return request;
    }
}
