package com.java_template.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.application.dto.request.FpMLTradeConfirmationRequest;
import com.java_template.application.dto.response.ApiResponse;
import com.java_template.application.dto.response.TradeConfirmationResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Simple test to verify OAuth2 authentication behavior with a single FpML sample.
 * This test helps isolate OAuth2 issues without running the full test suite.
 */
@Slf4j
@SpringBootTest(classes = com.java_template.Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimpleOAuth2Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    @WithMockUser
    void testVanillaSwapFpMLWithOAuth2() throws Exception {
        testFpMLSample("vanilla swap", "/fpml-official-samples/ird-ex01-vanilla-swap.xml", "TEST-VANILLA-SWAP");
    }

    @Test
    @WithMockUser
    void testITraxxIndexOptionFpMLWithOAuth2() throws Exception {
        testFpMLSample("iTraxx index option (requestConfirmation)", "/fpml-official-samples/itraxx-index-option.xml", "TEST-ITRAXX-OPTION");
    }

    @Test
    @WithMockUser
    void testCdsBasketFpMLWithOAuth2() throws Exception {
        testFpMLSample("CDS basket (executionNotification)", "/fpml-official-samples/cds-basket.xml", "TEST-CDS-BASKET");
    }

    /**
     * Common method to test FpML samples with OAuth2.
     * Reduces boilerplate code by handling the common test flow.
     *
     * @param sampleDescription Description of the sample being tested
     * @param resourcePath Path to the FpML file in test resources
     * @param messageIdPrefix Prefix for the generated message ID
     */
    private void testFpMLSample(String sampleDescription, String resourcePath, String messageIdPrefix) throws Exception {
        log.info("Starting OAuth2 test with {} FpML sample", sampleDescription);

        // Read the FpML file
        String fpmlContent = readFpMLFile(resourcePath);

        // Create the request
        String messageId = messageIdPrefix + "-" + System.currentTimeMillis();
        FpMLTradeConfirmationRequest request = createRequest(messageId, fpmlContent);

        log.info("Submitting {} trade confirmation request with messageId: {}", sampleDescription, messageId);

        // Submit the trade confirmation
        String correlationId = messageIdPrefix.toLowerCase().replace("_", "-") + "-test-" + UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", correlationId))
                .andReturn();

        // Check the response
        int statusCode = result.getResponse().getStatus();
        String responseBody = result.getResponse().getContentAsString();

        log.info("Response status: {}", statusCode);
        log.info("Response body: {}", responseBody);

        // Fail the test if status is not 200
        assertThat(statusCode).as("Expected 200 OK response").isEqualTo(200);

        log.info("Request succeeded with 200 OK");

        // Handle async processing if needed
        if (result.getRequest().isAsyncStarted()) {
            result = mockMvc.perform(asyncDispatch(result))
                    .andExpect(status().isOk())
                    .andReturn();
            responseBody = result.getResponse().getContentAsString();
            log.info("Async response body: {}", responseBody);
        }

        // Parse and validate response
        ApiResponse<?> response = objectMapper.readValue(responseBody, ApiResponse.class);
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();

        TradeConfirmationResponse tradeResponse = objectMapper.convertValue(
                response.getData(), TradeConfirmationResponse.class);

        log.info("{} trade confirmation response - Processing: {}, Validation: {}",
                sampleDescription, tradeResponse.getProcessingStatus(), tradeResponse.getValidationStatus());

        // Fail the test if processing status is FAILED
        assertThat(tradeResponse.getProcessingStatus())
                .as("Processing status should not be FAILED")
                .isNotEqualTo("FAILED");

        log.info("{} OAuth2 test completed", sampleDescription);
    }

    /**
     * Read FpML file content from test resources.
     */
    private String readFpMLFile(String resourcePath) throws Exception {
        try (var inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("FpML file not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private FpMLTradeConfirmationRequest createRequest(String messageId, String fpmlContent) {
        FpMLTradeConfirmationRequest request = new FpMLTradeConfirmationRequest();
        request.setMessageId(messageId);
        request.setMessageType("TRADE_CONFIRMATION");
        request.setFpmlVersion("5.13");
        request.setSenderLei("1234567890ABCDEFGH12");
        request.setReceiverLei("ZYXWVUTSRQ9876543201");
        request.setFpmlContent(Base64.getEncoder().encodeToString(fpmlContent.getBytes(StandardCharsets.UTF_8)));
        request.setCorrelationId("oauth2-test-correlation");
        return request;
    }
}
