package com.java_template.application.service;

import com.java_template.application.dto.request.FpMLTradeConfirmationRequest;
import com.java_template.application.dto.response.TradeConfirmationResponse;
import com.java_template.application.enums.FpMLDocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Service for processing FpML XML messages.
 * Handles XML validation, parsing, and data extraction from FpML trade confirmations.
 */
@Slf4j
@Service
public class FpMLProcessingService {

    private final DocumentBuilderFactory documentBuilderFactory;
    private final XPathFactory xPathFactory;

    public FpMLProcessingService() {
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setNamespaceAware(true);
        this.xPathFactory = XPathFactory.newInstance();
    }

    /**
     * Process an FpML trade confirmation request.
     * Validates the XML structure and extracts trade data.
     *
     * @param request The FpML trade confirmation request
     * @return Processing results with validation status and extracted data
     */
    public TradeConfirmationResponse processFpMLMessage(FpMLTradeConfirmationRequest request) {
        log.info("Processing FpML message with ID: {}", request.getMessageId());

        String processingId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();

        try {
            // Decode base64 FpML content
            String fpmlXml = decodeBase64Content(request.getFpmlContent());

            // Parse XML document
            Document document = parseXmlDocument(fpmlXml);

            // Validate FpML structure
            List<TradeConfirmationResponse.ValidationResult> validationResults = validateFpMLStructure(document);

            // Extract trade data if validation passes
            TradeConfirmationResponse.ExtractedTradeData extractedData = null;
            String validationStatus = "VALID";
            String processingStatus = "PROCESSED";

            boolean hasErrors = validationResults.stream()
                    .anyMatch(result -> "ERROR".equals(result.getSeverity()));

            if (!hasErrors) {
                extractedData = extractTradeData(document);
            } else {
                validationStatus = "INVALID";
                processingStatus = "FAILED";
            }

            return TradeConfirmationResponse.builder()
                    .messageId(request.getMessageId())
                    .processingId(processingId)
                    .processingStatus(processingStatus)
                    .validationStatus(validationStatus)
                    .receivedTimestamp(startTime)
                    .processedTimestamp(Instant.now())
                    .validationResults(validationResults)
                    .extractedTradeData(extractedData)
                    .duplicateCheckResults(performDuplicateCheck(request))
                    .links(buildResponseLinks(processingId, request.getMessageId()))
                    .build();

        } catch (Exception e) {
            log.error("Error processing FpML message {}: {}", request.getMessageId(), e.getMessage(), e);

            return TradeConfirmationResponse.builder()
                    .messageId(request.getMessageId())
                    .processingId(processingId)
                    .processingStatus("FAILED")
                    .validationStatus("SCHEMA_ERROR")
                    .receivedTimestamp(startTime)
                    .processedTimestamp(Instant.now())
                    .processingErrors(List.of(
                            TradeConfirmationResponse.ProcessingError.builder()
                                    .errorCode("PROCESSING_ERROR")
                                    .errorMessage("Failed to process FpML message: " + e.getMessage())
                                    .timestamp(Instant.now())
                                    .retryable(false)
                                    .build()
                    ))
                    .build();
        }
    }

    /**
     * Decode base64 encoded FpML content.
     */
    private String decodeBase64Content(String base64Content) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 encoded FpML content", e);
        }
    }

    /**
     * Parse XML document from FpML string.
     */
    private Document parseXmlDocument(String fpmlXml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(fpmlXml.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Validate FpML document structure and content.
     */
    private List<TradeConfirmationResponse.ValidationResult> validateFpMLStructure(
            Document document) {

        List<TradeConfirmationResponse.ValidationResult> results = new ArrayList<>();

        try {
            XPath xpath = xPathFactory.newXPath();

            // Validate root element using structured approach
            String rootElementXPath = FpMLDocumentType.generateRootElementXPath();
            XPathExpression rootExpr = xpath.compile(rootElementXPath);
            if (rootExpr.evaluate(document, XPathConstants.NODE) == null) {
                results.add(createValidationResult("SCHEMA", "ERROR", "INVALID_ROOT",
                        "Document must have a supported FpML root element. Supported types: " +
                        FpMLDocumentType.getSupportedTypesAsString(), "/", null, null));
            } else {
                // Determine the actual document type
                FpMLDocumentType documentType = determineDocumentType(document, xpath);
                if (documentType != null) {
                    log.info("Detected FpML document type: {} ({})", documentType.getElementName(), documentType.getDescription());

                    // Validate trade element exists only for document types that require it
                    if (documentType.isRequiresTrade()) {
                        XPathExpression tradeExpr = xpath.compile("//*[local-name()='trade']");
                        if (tradeExpr.evaluate(document, XPathConstants.NODE) == null) {
                            results.add(createValidationResult("SCHEMA", "ERROR", "MISSING_TRADE",
                                    "FpML document of type '" + documentType.getElementName() + "' must contain a trade element", "//trade", null, null));
                        }
                    }
                } else {
                    log.warn("Could not determine specific FpML document type");
                }
            }

            // Validate party information
            XPathExpression partyExpr = xpath.compile("//*[local-name()='party']");
            if (partyExpr.evaluate(document, XPathConstants.NODE) == null) {
                results.add(createValidationResult("SCHEMA", "WARNING", "MISSING_PARTIES",
                        "FpML document should contain party information", "//party", null, null));
            }

            // If no errors found, add success result
            if (results.isEmpty()) {
                results.add(createValidationResult("SCHEMA", "INFO", "VALIDATION_SUCCESS",
                        "FpML document structure is valid", null, null, null));
            }

        } catch (XPathExpressionException e) {
            log.error("XPath validation error: {}", e.getMessage(), e);
            results.add(createValidationResult("SCHEMA", "ERROR", "XPATH_ERROR",
                    "Error during XML validation: " + e.getMessage(), null, null, null));
        }

        return results;
    }

    /**
     * Extract trade data from validated FpML document.
     */
    private TradeConfirmationResponse.ExtractedTradeData extractTradeData(Document document) {
        try {
            XPath xpath = xPathFactory.newXPath();

            // Extract basic trade information
            String tradeId = extractXPathValue(xpath, document, "//*[local-name()='tradeId']/@*[local-name()='tradeIdScheme']");
            String uti = extractXPathValue(xpath, document, "//*[local-name()='uti']/text()");
            String usi = extractXPathValue(xpath, document, "//*[local-name()='usi']/text()");

            return TradeConfirmationResponse.ExtractedTradeData.builder()
                    .tradeId(tradeId)
                    .uti(uti)
                    .usi(usi)
                    .tradeDate(extractXPathValue(xpath, document, "//*[local-name()='tradeDate']/text()"))
                    .effectiveDate(extractXPathValue(xpath, document, "//*[local-name()='effectiveDate']/text()"))
                    .maturityDate(extractXPathValue(xpath, document, "//*[local-name()='maturityDate']/text()"))
                    .notionalAmount(extractXPathValue(xpath, document, "//*[local-name()='notionalAmount']/text()"))
                    .currency(extractXPathValue(xpath, document, "//*[local-name()='currency']/text()"))
                    .productType(extractXPathValue(xpath, document, "//*[local-name()='productType']/text()"))
                    .counterparties(extractCounterparties(xpath, document))
                    .build();

        } catch (Exception e) {
            log.error("Error extracting trade data: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract XPath value safely.
     */
    private String extractXPathValue(XPath xpath, Document document, String expression) {
        try {
            return xpath.evaluate(expression, document);
        } catch (XPathExpressionException e) {
            log.warn("Failed to extract XPath value for expression: {}", expression);
            return null;
        }
    }

    /**
     * Extract counterparty information from FpML document.
     */
    private List<TradeConfirmationResponse.CounterpartyInfo> extractCounterparties(XPath xpath, Document document) {
        List<TradeConfirmationResponse.CounterpartyInfo> counterparties = new ArrayList<>();

        // This is a simplified extraction - real implementation would be more complex
        String partyALei = extractXPathValue(xpath, document, "//*[local-name()='party'][1]/*[local-name()='partyId'][@*[local-name()='partyIdScheme']='http://www.fpml.org/coding-scheme/external/iso17442']/text()");
        String partyBLei = extractXPathValue(xpath, document, "//*[local-name()='party'][2]/*[local-name()='partyId'][@*[local-name()='partyIdScheme']='http://www.fpml.org/coding-scheme/external/iso17442']/text()");

        if (partyALei != null) {
            counterparties.add(TradeConfirmationResponse.CounterpartyInfo.builder()
                    .lei(partyALei)
                    .role("PARTY_A")
                    .tradingCapacity("PRINCIPAL")
                    .build());
        }

        if (partyBLei != null) {
            counterparties.add(TradeConfirmationResponse.CounterpartyInfo.builder()
                    .lei(partyBLei)
                    .role("PARTY_B")
                    .tradingCapacity("PRINCIPAL")
                    .build());
        }

        return counterparties;
    }

    /**
     * Perform duplicate check for the message.
     * <p>
     * TODO: Implement proper duplicate detection logic
     * - Check messageId against database of previously processed messages
     * - Calculate and compare message hash (SHA-256 of FpML content)
     * - Consider time window for duplicate detection (e.g., 24 hours)
     * - Handle edge cases like legitimate resubmissions vs duplicates
     */
    private TradeConfirmationResponse.DuplicateCheckResults performDuplicateCheck(FpMLTradeConfirmationRequest request) {
        // TODO: Replace this mock implementation with real duplicate detection
        // Current implementation always returns false (no duplicates detected)
        return TradeConfirmationResponse.DuplicateCheckResults.builder()
                .isDuplicate(false)
                .duplicateCheckTimestamp(Instant.now())
                .duplicateCheckCriteria(List.of("messageId", "messageHash"))
                .build();
    }

    /**
     * Build response links for the processed message.
     */
    private TradeConfirmationResponse.ResponseLinks buildResponseLinks(String processingId, String messageId) {
        return TradeConfirmationResponse.ResponseLinks.builder()
                .self("/api/v1/trade-confirmations/" + processingId)
                .status("/api/v1/trade-confirmations/" + processingId + "/status")
                .build();
    }

    /**
     * Determine the specific FpML document type from the root element.
     */
    private FpMLDocumentType determineDocumentType(Document document, XPath xpath) {
        try {
            // Get the root element name
            XPathExpression rootNameExpr = xpath.compile("local-name(/*)");
            String rootElementName = rootNameExpr.evaluate(document);

            if (rootElementName != null && !rootElementName.isEmpty()) {
                return FpMLDocumentType.fromElementName(rootElementName);
            }
        } catch (XPathExpressionException e) {
            log.error("Error determining document type: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Create a validation result object.
     */
    @SuppressWarnings("SameParameterValue")
    private TradeConfirmationResponse.ValidationResult createValidationResult(
            String validationType, String severity, String errorCode, String errorMessage,
            String fieldPath, String expectedValue, String actualValue) {

        return TradeConfirmationResponse.ValidationResult.builder()
                .validationType(validationType)
                .severity(severity)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .fieldPath(fieldPath)
                .expectedValue(expectedValue)
                .actualValue(actualValue)
                .build();
    }
}
