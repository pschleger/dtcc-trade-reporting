package com.java_template.common.serializer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ABOUTME: Test class for EvaluationOutcome logical chaining capabilities.
 * Demonstrates and verifies the and(), or(), allOf(), and anyOf() operations.
 */
class EvaluationOutcomeTest {

    @Test
    @DisplayName("Should chain outcomes with AND logic - success and success")
    void testAndChaining_SuccessAndSuccess() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.success();
        EvaluationOutcome second = EvaluationOutcome.success();

        // When
        EvaluationOutcome result = first.and(second);

        // Then
        assertTrue(result.isSuccess());
        assertInstanceOf(EvaluationOutcome.Success.class, result);
    }

    @Test
    @DisplayName("Should chain outcomes with AND logic - success and failure")
    void testAndChaining_SuccessAndFailure() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.success();
        EvaluationOutcome second = EvaluationOutcome.Fail.structuralFailure("Second validation failed");

        // When
        EvaluationOutcome result = first.and(second);

        // Then
        assertTrue(result.isFailure());
        assertInstanceOf(EvaluationOutcome.Fail.class, result);
        assertEquals("Second validation failed", ((EvaluationOutcome.Fail) result).getReason());
    }

    @Test
    @DisplayName("Should chain outcomes with AND logic - failure and success")
    void testAndChaining_FailureAndSuccess() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.Fail.businessRuleFailure("First validation failed");
        EvaluationOutcome second = EvaluationOutcome.success();

        // When
        EvaluationOutcome result = first.and(second);

        // Then
        assertTrue(result.isFailure());
        assertInstanceOf(EvaluationOutcome.Fail.class, result);
        assertEquals("First validation failed", ((EvaluationOutcome.Fail) result).getReason());
        assertEquals("BUSINESS_RULE_FAILURE", ((EvaluationOutcome.Fail) result).getCategory());
    }

    @Test
    @DisplayName("Should chain outcomes with AND logic - failure and failure returns first")
    void testAndChaining_FailureAndFailure() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.Fail.structuralFailure("First failure");
        EvaluationOutcome second = EvaluationOutcome.Fail.dataQualityFailure("Second failure");

        // When
        EvaluationOutcome result = first.and(second);

        // Then
        assertTrue(result.isFailure());
        assertEquals("First failure", ((EvaluationOutcome.Fail) result).getReason());
        assertEquals("STRUCTURAL_FAILURE", ((EvaluationOutcome.Fail) result).getCategory());
    }

    @Test
    @DisplayName("Should chain outcomes with OR logic - success or failure")
    void testOrChaining_SuccessOrFailure() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.success();
        EvaluationOutcome second = EvaluationOutcome.Fail.structuralFailure("Second validation failed");

        // When
        EvaluationOutcome result = first.or(second);

        // Then
        assertTrue(result.isSuccess());
        assertInstanceOf(EvaluationOutcome.Success.class, result);
    }

    @Test
    @DisplayName("Should chain outcomes with OR logic - failure or success")
    void testOrChaining_FailureOrSuccess() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.Fail.businessRuleFailure("First validation failed");
        EvaluationOutcome second = EvaluationOutcome.success();

        // When
        EvaluationOutcome result = first.or(second);

        // Then
        assertTrue(result.isSuccess());
        assertInstanceOf(EvaluationOutcome.Success.class, result);
    }

    @Test
    @DisplayName("Should chain outcomes with OR logic - failure or failure returns last")
    void testOrChaining_FailureOrFailure() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.Fail.structuralFailure("First failure");
        EvaluationOutcome second = EvaluationOutcome.Fail.dataQualityFailure("Second failure");

        // When
        EvaluationOutcome result = first.or(second);

        // Then
        assertTrue(result.isFailure());
        assertEquals("Second failure", ((EvaluationOutcome.Fail) result).getReason());
        assertEquals("DATA_QUALITY_FAILURE", ((EvaluationOutcome.Fail) result).getCategory());
    }

    @Test
    @DisplayName("Should use allOf for multiple AND operations")
    void testAllOf_MultipleOutcomes() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.success();
        EvaluationOutcome second = EvaluationOutcome.success();
        EvaluationOutcome third = EvaluationOutcome.Fail.businessRuleFailure("Third failed");
        EvaluationOutcome fourth = EvaluationOutcome.success();

        // When
        EvaluationOutcome result = EvaluationOutcome.allOf(first, second, third, fourth);

        // Then
        assertTrue(result.isFailure());
        assertEquals("Third failed", ((EvaluationOutcome.Fail) result).getReason());
        assertEquals("BUSINESS_RULE_FAILURE", ((EvaluationOutcome.Fail) result).getCategory());
    }

    @Test
    @DisplayName("Should use allOf for all successful outcomes")
    void testAllOf_AllSuccessful() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.success();
        EvaluationOutcome second = EvaluationOutcome.success();
        EvaluationOutcome third = EvaluationOutcome.success();

        // When
        EvaluationOutcome result = EvaluationOutcome.allOf(first, second, third);

        // Then
        assertTrue(result.isSuccess());
        assertInstanceOf(EvaluationOutcome.Success.class, result);
    }

    @Test
    @DisplayName("Should use anyOf for multiple OR operations")
    void testAnyOf_MultipleOutcomes() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.Fail.structuralFailure("First failed");
        EvaluationOutcome second = EvaluationOutcome.success();
        EvaluationOutcome third = EvaluationOutcome.Fail.businessRuleFailure("Third failed");

        // When
        EvaluationOutcome result = EvaluationOutcome.anyOf(first, second, third);

        // Then
        assertTrue(result.isSuccess());
        assertInstanceOf(EvaluationOutcome.Success.class, result);
    }

    @Test
    @DisplayName("Should use anyOf for all failed outcomes")
    void testAnyOf_AllFailed() {
        // Given
        EvaluationOutcome first = EvaluationOutcome.Fail.structuralFailure("First failed");
        EvaluationOutcome second = EvaluationOutcome.Fail.businessRuleFailure("Second failed");
        EvaluationOutcome third = EvaluationOutcome.Fail.dataQualityFailure("Third failed");

        // When
        EvaluationOutcome result = EvaluationOutcome.anyOf(first, second, third);

        // Then
        assertTrue(result.isFailure());
        assertEquals("Third failed", ((EvaluationOutcome.Fail) result).getReason());
        assertEquals("DATA_QUALITY_FAILURE", ((EvaluationOutcome.Fail) result).getCategory());
    }

    @Test
    @DisplayName("Should demonstrate complex chaining scenarios")
    void testComplexChaining() {
        // Given - simulate a complex validation scenario
        EvaluationOutcome structuralCheck = EvaluationOutcome.success();
        EvaluationOutcome businessRuleCheck = EvaluationOutcome.success();
        EvaluationOutcome dataQualityCheck = EvaluationOutcome.Fail.dataQualityFailure("Invalid data format");
        
        // When - chain with AND logic (all must pass)
        EvaluationOutcome strictValidation = structuralCheck
            .and(businessRuleCheck)
            .and(dataQualityCheck);
        
        // When - chain with OR logic (any can pass)
        EvaluationOutcome lenientValidation = structuralCheck
            .or(businessRuleCheck)
            .or(dataQualityCheck);

        // Then
        assertTrue(strictValidation.isFailure()); // Fails because data quality failed
        assertTrue(lenientValidation.isSuccess()); // Succeeds because structural check passed
    }

    @Test
    @DisplayName("Should provide convenience methods for checking outcome type")
    void testConvenienceMethods() {
        // Given
        EvaluationOutcome success = EvaluationOutcome.success();
        EvaluationOutcome failure = EvaluationOutcome.Fail.structuralFailure("Test failure");

        // Then
        assertTrue(success.isSuccess());
        assertFalse(success.isFailure());
        
        assertFalse(failure.isSuccess());
        assertTrue(failure.isFailure());
    }
}
