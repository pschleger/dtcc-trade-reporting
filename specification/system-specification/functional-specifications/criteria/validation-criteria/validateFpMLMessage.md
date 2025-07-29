# validateFpMLMessage Criterion Specification

## 1. Component Overview
**Component Name**: validateFpMLMessage
**Component Type**: CyodaCriterion
**Business Domain**: Trade Confirmation Processing
**Purpose**: Evaluates whether an FpML trade confirmation message has been successfully validated and is ready for processing
**Workflow Context**: TradeConfirmationWorkflow (validating state transitions)

## 2. Input Parameters
**Entity Type**: TradeConfirmation
**Required Fields**:
- `fpmlMessage`: string - Raw FpML XML message content
- `validationStatus`: string - Current validation status
- `validationResults`: object - Detailed validation results
- `validationTimestamp`: ISO-8601 timestamp - When validation was performed
- `validationErrors`: array - List of validation errors if any

**Optional Fields**:
- `validationWarnings`: array - Non-critical validation warnings
- `dataQualityScore`: decimal - Overall data quality assessment (0.0-1.0)
- `businessRuleOverrides`: array - Approved business rule exceptions

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "validation" - Tags for validation evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time (5 seconds)
- `context`: "fpml-validation-check" - Evaluation context identifier

**Evaluation Context**:
- Validation rule configuration
- Business rule exception policies
- Data quality thresholds

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF validationStatus == "SUCCESS" AND
   validationResults.schemaValid == true AND
   validationResults.businessRulesValid == true AND
   (dataQualityScore >= threshold OR hasApprovedOverrides) THEN
    RETURN true
ELSE IF validationStatus == "FAILED" OR
        validationErrors.length > 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on validation status and results
- Secondary evaluation considers data quality score
- Tertiary evaluation allows for approved business rule overrides
- Default behavior is conservative (false) for ambiguous states

**Calculation Methods**:
- Data quality score calculation from validation metrics
- Business rule override validation against approved exception list
- Temporal validation ensuring recent validation results

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Validation Status**: validationStatus equals "SUCCESS"
- **Schema Compliance**: validationResults.schemaValid is true
- **Business Rules**: validationResults.businessRulesValid is true
- **Data Quality**: dataQualityScore >= 0.95 (configurable threshold)
- **Temporal Validity**: validationTimestamp within last 24 hours

**Success Scenarios**:
- **Standard Success**: All validation checks pass with high data quality score
- **Override Success**: Minor business rule violations with approved overrides
- **Warning Success**: Validation passes with non-critical warnings only

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Validation Failure**: validationStatus equals "FAILED" or "ERROR"
- **Schema Violations**: validationResults.schemaValid is false
- **Business Rule Violations**: validationResults.businessRulesValid is false without overrides
- **Data Quality Issues**: dataQualityScore < 0.95 without approved exceptions
- **Stale Validation**: validationTimestamp older than 24 hours

**Failure Scenarios**:
- **Schema Failure**: FpML message fails schema validation
- **Business Rule Failure**: Trade violates business rules without approved overrides
- **Data Quality Failure**: Poor data quality below acceptable threshold
- **System Failure**: Validation process encountered technical errors

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Validation**: Some validation checks completed, others pending
- **Timeout Scenarios**: Validation process timed out before completion
- **Missing Data**: Required validation fields not present in entity
- **Concurrent Updates**: Entity modified during validation evaluation

**Special Scenarios**:
- **Emergency Override**: Manual override for critical business trades
- **Regulatory Exception**: Special handling for regulatory reporting requirements
- **System Maintenance**: Degraded validation during system maintenance

**Data Absence Handling**:
- Missing validationStatus defaults to false evaluation
- Missing validationResults treated as validation failure
- Missing dataQualityScore uses conservative threshold

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 2 seconds (95th percentile)
- **Throughput**: 500 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for boolean logic evaluation
- **Memory**: 64MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Validation Service**: Access to validation results and status
- **Configuration Service**: Business rule and threshold configuration

**External Dependencies**:
- **Audit Service**: Validation history and override tracking

**Data Dependencies**:
- Validation rule configuration
- Business rule override policies
- Data quality threshold settings

## 9. Configuration
**Configurable Thresholds**:
- `dataQualityThreshold`: decimal - Minimum data quality score - Default: 0.95
- `validationTimeoutHours`: integer - Maximum validation age - Default: 24
- `allowBusinessRuleOverrides`: boolean - Enable override processing - Default: true

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict evaluation mode - Default: false
- `warningsAsErrors`: boolean - Treat warnings as errors - Default: false

**Environment-Specific Settings**:
- Development: Relaxed thresholds, extended timeout
- Production: Strict thresholds, standard timeout

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required validation data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false evaluation on data unavailability
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to manual review for persistent evaluation failures

**Error Propagation**:
- Evaluation errors logged for troubleshooting
- Failed evaluations trigger manual review workflow
- Critical errors escalated to operations team
