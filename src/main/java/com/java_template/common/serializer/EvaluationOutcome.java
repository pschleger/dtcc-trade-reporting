package com.java_template.common.serializer;

/**
 * ABOUTME: Sealed class hierarchy representing the outcome of an evaluation in the EvaluationChain.
 * Provides type-safe representation of success and failure outcomes with clear contracts.
 * 
 * Sealed class representing the outcome of an evaluation.
 * Can be either Success (no additional information) or Fail (with reason).
 */
public sealed class EvaluationOutcome permits EvaluationOutcome.Success, EvaluationOutcome.Fail {
    
    /**
     * Represents a successful evaluation outcome.
     * No additional information is needed for success cases.
     */
    public static final class Success extends EvaluationOutcome {
        private static final Success INSTANCE = new Success();
        
        private Success() {}
        
        /**
         * Gets the singleton instance of Success.
         * 
         * @return Success instance
         */
        public static Success getInstance() {
            return INSTANCE;
        }
        
        @Override
        public String toString() {
            return "EvaluationOutcome.Success";
        }
    }
    
    /**
     * Represents a failed evaluation outcome.
     * Contains the reason for the failure with optional category.
     */
    public static final class Fail extends EvaluationOutcome {
        private final String reason;
        private final String category;
        
        private Fail(String reason, String category) {
            this.reason = reason;
            this.category = category;
        }
        
        /**
         * Creates a Fail outcome with the given reason and category.
         * 
         * @param reason the reason for failure
         * @param category the category of failure
         * @return Fail outcome
         */
        public static Fail of(String reason, String category) {
            return new Fail(reason, category);
        }
        
        /**
         * Creates a Fail outcome with the given reason and default category.
         * 
         * @param reason the reason for failure
         * @return Fail outcome
         */
        public static Fail of(String reason) {
            return new Fail(reason, StandardEvalReasonCategories.VALIDATION_FAILURE.getCode());
        }
        
        /**
         * Creates a Fail outcome for a structural validation failure.
         * 
         * @param reason the reason for structural failure
         * @return Fail outcome
         */
        public static Fail structuralFailure(String reason) {
            return new Fail(reason, StandardEvalReasonCategories.STRUCTURAL_FAILURE.getCode());
        }
        
        /**
         * Creates a Fail outcome for a business rule failure.
         * 
         * @param reason the reason for business rule failure
         * @return Fail outcome
         */
        public static Fail businessRuleFailure(String reason) {
            return new Fail(reason, StandardEvalReasonCategories.BUSINESS_RULE_FAILURE.getCode());
        }
        
        /**
         * Creates a Fail outcome for a data quality failure.
         * 
         * @param reason the reason for data quality failure
         * @return Fail outcome
         */
        public static Fail dataQualityFailure(String reason) {
            return new Fail(reason, StandardEvalReasonCategories.DATA_QUALITY_FAILURE.getCode());
        }
        
        /**
         * Creates a Fail outcome with the given reason and category enum.
         * 
         * @param reason the reason for failure
         * @param category the category of failure
         * @return Fail outcome
         */
        public static Fail of(String reason, StandardEvalReasonCategories category) {
            return new Fail(reason, category.getCode());
        }
        
        /**
         * Gets the reason for the failure.
         * 
         * @return the failure reason
         */
        public String getReason() {
            return reason;
        }
        
        /**
         * Gets the category of the failure.
         * 
         * @return the failure category
         */
        public String getCategory() {
            return category;
        }
        
        /**
         * Formats the failure reason for display.
         * 
         * @return formatted reason string
         */
        public String formatReason() {
            if (category != null && !category.isEmpty()) {
                return String.format("[%s] %s", category, reason);
            }
            return reason;
        }
        
        /**
         * Converts this Fail outcome to an EvaluationReason.
         * 
         * @return EvaluationReason equivalent
         */
        public EvaluationReason toEvaluationReason() {
            return new EvaluationReason(reason, category);
        }
        
        @Override
        public String toString() {
            return String.format("EvaluationOutcome.Fail{reason='%s', category='%s'}", reason, category);
        }
    }
    
    /**
     * Factory method to create a Success outcome.
     * 
     * @return Success outcome
     */
    public static Success success() {
        return Success.getInstance();
    }
    
    /**
     * Factory method to create a Fail outcome.
     * 
     * @param reason the reason for failure
     * @return Fail outcome
     */
    public static Fail fail(String reason) {
        return Fail.of(reason);
    }
    
    /**
     * Factory method to create a Fail outcome with category.
     * 
     * @param reason the reason for failure
     * @param category the category of failure
     * @return Fail outcome
     */
    public static Fail fail(String reason, StandardEvalReasonCategories category) {
        return Fail.of(reason, category);
    }
}
