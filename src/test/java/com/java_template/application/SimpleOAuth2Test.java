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
    void testSingleFpMLWithOAuth2() throws Exception {
        log.info("Starting simple OAuth2 test with a single FpML sample");

        // Create a simple FpML content for testing
        // language=XML
        String simpleFpmlContent = """
<?xml version="1.0" encoding="utf-8"?>
<!--View is confirmation-->
<!--Version is 5-13-->
<!--NS is http://www.fpml.org/FpML-5/confirmation-->
<!--
  == Copyright (c) 2022-2025 All rights reserved.
  == Financial Products Markup Language is subject to the FpML public license.
  == A copy of this license is available at http://www.fpml.org/license/license.html
  -->
<dataDocument xmlns="http://www.fpml.org/FpML-5/confirmation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" fpmlVersion="5-13" xsi:schemaLocation="http://www.fpml.org/FpML-5/confirmation ../../fpml-main-5-13.xsd http://www.w3.org/2000/09/xmldsig# ../../xmldsig-core-schema.xsd">
  <trade>
    <tradeHeader>
      <partyTradeIdentifier>
        <partyReference href="party1" />
        <tradeId tradeIdScheme="http://www.partyA.com/swaps/trade-id">TW9235</tradeId>
      </partyTradeIdentifier>
      <partyTradeIdentifier>
        <partyReference href="party2" />
        <tradeId tradeIdScheme="http://www.barclays.com/swaps/trade-id">SW2000</tradeId>
      </partyTradeIdentifier>
      <tradeDate>1994-12-12</tradeDate>
    </tradeHeader>
    <swap>
<!-- Party A pays the floating rate every 6 months, based on 6M EUR-LIBOR-BBA,
            on an ACT/360 basis -->
      <swapStream>
        <payerPartyReference href="party1" />
        <receiverPartyReference href="party2" />
        <calculationPeriodDates id="floatingCalcPeriodDates">
          <effectiveDate>
            <unadjustedDate>1994-12-14</unadjustedDate>
            <dateAdjustments>
              <businessDayConvention>NONE</businessDayConvention>
            </dateAdjustments>
          </effectiveDate>
          <terminationDate>
            <unadjustedDate>1999-12-14</unadjustedDate>
            <dateAdjustments>
              <businessDayConvention>MODFOLLOWING</businessDayConvention>
              <businessCenters id="primaryBusinessCenters">
                <businessCenter>DEFR</businessCenter>
              </businessCenters>
            </dateAdjustments>
          </terminationDate>
          <calculationPeriodDatesAdjustments>
            <businessDayConvention>MODFOLLOWING</businessDayConvention>
            <businessCentersReference href="primaryBusinessCenters" />
          </calculationPeriodDatesAdjustments>
          <calculationPeriodFrequency>
            <periodMultiplier>6</periodMultiplier>
            <period>M</period>
            <rollConvention>14</rollConvention>
          </calculationPeriodFrequency>
        </calculationPeriodDates>
        <paymentDates>
          <calculationPeriodDatesReference href="floatingCalcPeriodDates" />
          <paymentFrequency>
            <periodMultiplier>6</periodMultiplier>
            <period>M</period>
          </paymentFrequency>
          <payRelativeTo>CalculationPeriodEndDate</payRelativeTo>
          <paymentDatesAdjustments>
            <businessDayConvention>MODFOLLOWING</businessDayConvention>
            <businessCentersReference href="primaryBusinessCenters" />
          </paymentDatesAdjustments>
        </paymentDates>
        <resetDates id="resetDates">
          <calculationPeriodDatesReference href="floatingCalcPeriodDates" />
          <resetRelativeTo>CalculationPeriodStartDate</resetRelativeTo>
          <fixingDates>
            <periodMultiplier>-2</periodMultiplier>
            <period>D</period>
            <dayType>Business</dayType>
            <businessDayConvention>NONE</businessDayConvention>
            <businessCenters>
              <businessCenter>GBLO</businessCenter>
            </businessCenters>
            <dateRelativeTo href="resetDates" />
          </fixingDates>
          <resetFrequency>
            <periodMultiplier>6</periodMultiplier>
            <period>M</period>
          </resetFrequency>
          <resetDatesAdjustments>
            <businessDayConvention>MODFOLLOWING</businessDayConvention>
            <businessCentersReference href="primaryBusinessCenters" />
          </resetDatesAdjustments>
        </resetDates>
        <calculationPeriodAmount>
          <calculation>
            <notionalSchedule>
              <notionalStepSchedule>
                <initialValue>50000000.00</initialValue>
                <currency currencyScheme="http://www.fpml.org/coding-scheme/external/iso4217">EUR</currency>
              </notionalStepSchedule>
            </notionalSchedule>
            <floatingRateCalculation>
              <floatingRateIndex>EUR-LIBOR-BBA</floatingRateIndex>
              <indexTenor>
                <periodMultiplier>6</periodMultiplier>
                <period>M</period>
              </indexTenor>
            </floatingRateCalculation>
            <dayCountFraction>ACT/360</dayCountFraction>
          </calculation>
        </calculationPeriodAmount>
      </swapStream>
<!-- Barclays pays the 6% fixed rate every year on a 30E/360 basis -->
      <swapStream>
        <payerPartyReference href="party2" />
        <receiverPartyReference href="party1" />
        <calculationPeriodDates id="fixedCalcPeriodDates">
          <effectiveDate>
            <unadjustedDate>1994-12-14</unadjustedDate>
            <dateAdjustments>
              <businessDayConvention>NONE</businessDayConvention>
            </dateAdjustments>
          </effectiveDate>
          <terminationDate>
            <unadjustedDate>1999-12-14</unadjustedDate>
            <dateAdjustments>
              <businessDayConvention>MODFOLLOWING</businessDayConvention>
              <businessCentersReference href="primaryBusinessCenters" />
            </dateAdjustments>
          </terminationDate>
          <calculationPeriodDatesAdjustments>
            <businessDayConvention>MODFOLLOWING</businessDayConvention>
            <businessCentersReference href="primaryBusinessCenters" />
          </calculationPeriodDatesAdjustments>
          <calculationPeriodFrequency>
            <periodMultiplier>1</periodMultiplier>
            <period>Y</period>
            <rollConvention>14</rollConvention>
          </calculationPeriodFrequency>
        </calculationPeriodDates>
        <paymentDates>
          <calculationPeriodDatesReference href="fixedCalcPeriodDates" />
          <paymentFrequency>
            <periodMultiplier>1</periodMultiplier>
            <period>Y</period>
          </paymentFrequency>
          <payRelativeTo>CalculationPeriodEndDate</payRelativeTo>
          <paymentDatesAdjustments>
            <businessDayConvention>MODFOLLOWING</businessDayConvention>
            <businessCentersReference href="primaryBusinessCenters" />
          </paymentDatesAdjustments>
        </paymentDates>
        <calculationPeriodAmount>
          <calculation>
            <notionalSchedule>
              <notionalStepSchedule>
                <initialValue>50000000.00</initialValue>
                <currency currencyScheme="http://www.fpml.org/coding-scheme/external/iso4217">EUR</currency>
              </notionalStepSchedule>
            </notionalSchedule>
            <fixedRateSchedule>
              <initialValue>0.06</initialValue>
            </fixedRateSchedule>
            <dayCountFraction>30E/360</dayCountFraction>
          </calculation>
        </calculationPeriodAmount>
      </swapStream>
    </swap>
  </trade>
  <party id="party1">
    <partyId partyIdScheme="http://www.fpml.org/coding-scheme/external/iso17442">549300VBWWV6BYQOWM67</partyId>
    <partyName>Party A</partyName>
  </party>
  <party id="party2">
    <partyId partyIdScheme="http://www.fpml.org/coding-scheme/external/iso17442">529900DTJ5A7S5UCBB52</partyId>
  </party>
</dataDocument>
""";

        // Create the request
        String messageId = "TEST-OAUTH2-" + System.currentTimeMillis();
        FpMLTradeConfirmationRequest request = createRequest(messageId, simpleFpmlContent);

        log.info("Submitting trade confirmation request with messageId: {}", messageId);

        // Submit the trade confirmation
        MvcResult result = mockMvc.perform(post("/api/v1/trade-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Correlation-ID", "oauth2-test-" + UUID.randomUUID()))
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

        log.info("Trade confirmation response - Processing: {}, Validation: {}",
                tradeResponse.getProcessingStatus(), tradeResponse.getValidationStatus());

        // Fail the test if processing status is FAILED
        assertThat(tradeResponse.getProcessingStatus())
                .as("Processing status should not be FAILED")
                .isNotEqualTo("FAILED");

        // Check if OAuth2 worked (no SYSTEM_ERROR due to auth issues)
        if ("SYSTEM_ERROR".equals(tradeResponse.getValidationStatus()) &&
            tradeResponse.getProcessingErrors() != null) {

            boolean hasOAuthError = tradeResponse.getProcessingErrors().stream()
                    .anyMatch(error -> error.getErrorMessage().contains("invalid_token_response") ||
                                     error.getErrorMessage().contains("[no body]"));

            if (hasOAuthError) {
                log.error("OAuth2 authentication failed - this indicates the credentials issue");
            } else {
                log.info("OAuth2 authentication succeeded - error is not auth-related");
            }
        }

        log.info("Simple OAuth2 test completed");
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
