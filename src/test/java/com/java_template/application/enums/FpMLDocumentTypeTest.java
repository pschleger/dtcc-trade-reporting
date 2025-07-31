package com.java_template.application.enums;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FpMLDocumentType enum.
 */
class FpMLDocumentTypeTest {

    @Test
    void testGetAllElementNames() {
        Set<String> elementNames = FpMLDocumentType.getAllElementNames();
        
        // Verify we have all the expected document types
        assertThat(elementNames).hasSize(24);
        assertThat(elementNames).contains(
                "dataDocument",
                "FpML",
                "requestConfirmation",
                "confirmationAgreed",
                "executionNotification",
                "executionRetracted",
                "dealStatement",
                "outstandingContractsStatement",
                "facilityPositionStatement",
                "facilityStatement",
                "loanPartyProfileStatement",
                "loanLegalActionStatement",
                "facilityNotification",
                "lcNotification",
                "loanAllocationNotification",
                "loanBulkServicingNotification",
                "loanContractNotification",
                "loanCovenantObligationNotification",
                "loanLegalActionNotification",
                "loanTradeNotification",
                "loanNotificationAcknowledgement",
                "loanNotificationException",
                "loanNotificationRetracted",
                "requestClearing"
        );
    }

    @Test
    void testGetTradeRequiredTypes() {
        Set<FpMLDocumentType> tradeRequiredTypes = FpMLDocumentType.getTradeRequiredTypes();
        
        // Verify that core document types require trade elements
        assertThat(tradeRequiredTypes).contains(
                FpMLDocumentType.DATA_DOCUMENT,
                FpMLDocumentType.FPML,
                FpMLDocumentType.REQUEST_CONFIRMATION,
                FpMLDocumentType.CONFIRMATION_AGREED,
                FpMLDocumentType.EXECUTION_NOTIFICATION,
                FpMLDocumentType.EXECUTION_RETRACTED,
                FpMLDocumentType.REQUEST_CLEARING
        );
        
        // Verify that statement and notification types don't require trade elements
        assertThat(tradeRequiredTypes).doesNotContain(
                FpMLDocumentType.DEAL_STATEMENT,
                FpMLDocumentType.FACILITY_NOTIFICATION,
                FpMLDocumentType.LOAN_NOTIFICATION_ACKNOWLEDGEMENT
        );
    }

    @Test
    void testFromElementName() {
        assertThat(FpMLDocumentType.fromElementName("dataDocument"))
                .isEqualTo(FpMLDocumentType.DATA_DOCUMENT);
        assertThat(FpMLDocumentType.fromElementName("requestConfirmation"))
                .isEqualTo(FpMLDocumentType.REQUEST_CONFIRMATION);
        assertThat(FpMLDocumentType.fromElementName("executionNotification"))
                .isEqualTo(FpMLDocumentType.EXECUTION_NOTIFICATION);
        assertThat(FpMLDocumentType.fromElementName("nonExistentType"))
                .isNull();
    }

    @Test
    void testIsSupported() {
        assertThat(FpMLDocumentType.isSupported("dataDocument")).isTrue();
        assertThat(FpMLDocumentType.isSupported("requestConfirmation")).isTrue();
        assertThat(FpMLDocumentType.isSupported("executionNotification")).isTrue();
        assertThat(FpMLDocumentType.isSupported("facilityNotification")).isTrue();
        assertThat(FpMLDocumentType.isSupported("nonExistentType")).isFalse();
    }

    @Test
    void testGenerateRootElementXPath() {
        String xpath = FpMLDocumentType.generateRootElementXPath();
        
        // Verify the XPath contains all expected document types
        assertThat(xpath).contains("local-name()='dataDocument'");
        assertThat(xpath).contains("local-name()='FpML'");
        assertThat(xpath).contains("local-name()='requestConfirmation'");
        assertThat(xpath).contains("local-name()='executionNotification'");
        assertThat(xpath).contains("local-name()='facilityNotification'");
        assertThat(xpath).contains("local-name()='requestClearing'");
        
        // Verify it's a proper XPath expression
        assertThat(xpath).startsWith("/*[");
        assertThat(xpath).endsWith("]");
        assertThat(xpath).contains(" or ");
    }

    @Test
    void testGetSupportedTypesAsString() {
        String supportedTypes = FpMLDocumentType.getSupportedTypesAsString();
        
        // Verify it contains all expected types
        assertThat(supportedTypes).contains("dataDocument");
        assertThat(supportedTypes).contains("requestConfirmation");
        assertThat(supportedTypes).contains("executionNotification");
        assertThat(supportedTypes).contains("facilityNotification");
        assertThat(supportedTypes).contains("requestClearing");
        
        // Verify it's comma-separated
        assertThat(supportedTypes).contains(", ");
    }

    @Test
    void testDocumentTypeProperties() {
        // Test core document types
        assertThat(FpMLDocumentType.DATA_DOCUMENT.getElementName()).isEqualTo("dataDocument");
        assertThat(FpMLDocumentType.DATA_DOCUMENT.getDescription()).isEqualTo("Standard FpML data document");
        assertThat(FpMLDocumentType.DATA_DOCUMENT.isRequiresTrade()).isTrue();
        
        // Test notification types
        assertThat(FpMLDocumentType.FACILITY_NOTIFICATION.getElementName()).isEqualTo("facilityNotification");
        assertThat(FpMLDocumentType.FACILITY_NOTIFICATION.getDescription()).isEqualTo("Facility notification");
        assertThat(FpMLDocumentType.FACILITY_NOTIFICATION.isRequiresTrade()).isFalse();
        
        // Test execution types
        assertThat(FpMLDocumentType.EXECUTION_NOTIFICATION.getElementName()).isEqualTo("executionNotification");
        assertThat(FpMLDocumentType.EXECUTION_NOTIFICATION.getDescription()).isEqualTo("Trade execution notification");
        assertThat(FpMLDocumentType.EXECUTION_NOTIFICATION.isRequiresTrade()).isTrue();
    }
}
