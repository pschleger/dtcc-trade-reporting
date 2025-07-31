package com.java_template.application.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumeration of supported FpML document types.
 * Each document type represents a different kind of FpML message that can be processed by the system.
 */
@Getter
public enum FpMLDocumentType {
    
    // Core document types
    DATA_DOCUMENT("dataDocument", "Standard FpML data document", true),
    FPML("FpML", "Generic FpML root element", true),
    
    // Confirmation and execution types
    REQUEST_CONFIRMATION("requestConfirmation", "Request for trade confirmation", true),
    CONFIRMATION_AGREED("confirmationAgreed", "Confirmation agreement", true),
    EXECUTION_NOTIFICATION("executionNotification", "Trade execution notification", true),
    EXECUTION_RETRACTED("executionRetracted", "Retraction of trade execution", true),
    
    // Statement types
    DEAL_STATEMENT("dealStatement", "Deal statement document", false),
    OUTSTANDING_CONTRACTS_STATEMENT("outstandingContractsStatement", "Outstanding contracts statement", false),
    FACILITY_POSITION_STATEMENT("facilityPositionStatement", "Facility position statement", false),
    FACILITY_STATEMENT("facilityStatement", "Facility statement", false),
    LOAN_PARTY_PROFILE_STATEMENT("loanPartyProfileStatement", "Loan party profile statement", false),
    LOAN_LEGAL_ACTION_STATEMENT("loanLegalActionStatement", "Loan legal action statement", false),
    
    // Notification types
    FACILITY_NOTIFICATION("facilityNotification", "Facility notification", false),
    LC_NOTIFICATION("lcNotification", "Letter of credit notification", false),
    LOAN_ALLOCATION_NOTIFICATION("loanAllocationNotification", "Loan allocation notification", false),
    LOAN_BULK_SERVICING_NOTIFICATION("loanBulkServicingNotification", "Loan bulk servicing notification", false),
    LOAN_CONTRACT_NOTIFICATION("loanContractNotification", "Loan contract notification", false),
    LOAN_COVENANT_OBLIGATION_NOTIFICATION("loanCovenantObligationNotification", "Loan covenant obligation notification", false),
    LOAN_LEGAL_ACTION_NOTIFICATION("loanLegalActionNotification", "Loan legal action notification", false),
    LOAN_TRADE_NOTIFICATION("loanTradeNotification", "Loan trade notification", false),
    
    // Acknowledgement and exception types
    LOAN_NOTIFICATION_ACKNOWLEDGEMENT("loanNotificationAcknowledgement", "Loan notification acknowledgement", false),
    LOAN_NOTIFICATION_EXCEPTION("loanNotificationException", "Loan notification exception", false),
    LOAN_NOTIFICATION_RETRACTED("loanNotificationRetracted", "Loan notification retraction", false),
    
    // Request types
    REQUEST_CLEARING("requestClearing", "Request for clearing", true);

    private final String elementName;
    private final String description;
    private final boolean requiresTrade;

    FpMLDocumentType(String elementName, String description, boolean requiresTrade) {
        this.elementName = elementName;
        this.description = description;
        this.requiresTrade = requiresTrade;
    }

    /**
     * Get all supported FpML document type element names.
     *
     * @return Set of all supported element names
     */
    public static Set<String> getAllElementNames() {
        return Arrays.stream(values())
                .map(FpMLDocumentType::getElementName)
                .collect(Collectors.toSet());
    }

    /**
     * Get all document types that require a trade element.
     *
     * @return Set of document types that require trade elements
     */
    public static Set<FpMLDocumentType> getTradeRequiredTypes() {
        return Arrays.stream(values())
                .filter(FpMLDocumentType::isRequiresTrade)
                .collect(Collectors.toSet());
    }

    /**
     * Find document type by element name.
     *
     * @param elementName The FpML element name
     * @return The corresponding document type, or null if not found
     */
    public static FpMLDocumentType fromElementName(String elementName) {
        return Arrays.stream(values())
                .filter(type -> type.getElementName().equals(elementName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if the given element name is supported.
     *
     * @param elementName The FpML element name to check
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(String elementName) {
        return fromElementName(elementName) != null;
    }

    /**
     * Generate XPath expression for validating root elements.
     *
     * @return XPath expression that matches any supported root element
     */
    public static String generateRootElementXPath() {
        String conditions = Arrays.stream(values())
                .map(type -> "local-name()='" + type.getElementName() + "'")
                .collect(Collectors.joining(" or "));
        return "/*[" + conditions + "]";
    }

    /**
     * Generate human-readable list of supported document types.
     *
     * @return Comma-separated list of supported element names
     */
    public static String getSupportedTypesAsString() {
        return Arrays.stream(values())
                .map(FpMLDocumentType::getElementName)
                .collect(Collectors.joining(", "));
    }
}
