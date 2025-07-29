# hasAmendmentErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasAmendmentErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection Evaluation
**Purpose**: Evaluates whether amendment process encountered errors that require attention or remediation
**Workflow Context**: AmendmentWorkflow (error detection state transitions)

## 2. Input Parameters
**Entity Type**: AmendmentRequest
**Required Fields**:
- `amendmentStatus`: string - Current amendment status
- `amendmentErrors`: array - List of amendment errors
- `amendmentTimestamp`: ISO-8601 timestamp - When amendment was attempted
- `errorSeverity`: string - Highest severity level of errors encountered
- `errorCategories`: array - Categories of errors encountered

**Optional Fields**:
- `amendmentWarnings`: array - Non-critical amendment warnings
- `validationErrors`: array - Amendment validation errors
- `errorDetails`: object - Detailed error information

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "amendment-error-check" - Evaluation context identifier

**Evaluation Context**:
- Error severity thresholds
- Error category classifications
- Amendment validation requirements

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF amendmentErrors.length > 0 OR
   amendmentStatus == "FAILED" OR
   errorSeverity in ["CRITICAL", "HIGH"] OR
   validationErrors.length > 0 THEN
    RETURN true
ELSE IF amendmentStatus == "SUCCESS" AND
        amendmentErrors.length == 0 AND
        errorSeverity not in ["CRITICAL", "HIGH"] THEN
    RETURN false
ELSE
    RETURN false (default to no errors detected)
```

**Boolean Logic**:
- Primary evaluation based on presence of amendment errors
- Secondary evaluation considers amendment status
- Tertiary evaluation validates error severity levels
- Quaternary evaluation checks validation errors

**Calculation Methods**:
- Error count validation
- Error severity assessment
- Validation error analysis

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: amendmentErrors.length > 0
- **Failed Status**: amendmentStatus equals "FAILED" or "ERROR"
- **High Severity**: errorSeverity in ["CRITICAL", "HIGH"]
- **Validation Failures**: validationErrors.length > 0
- **Critical Categories**: errorCategories contains critical error types

**Success Scenarios**:
- **Business Rule Violations**: Amendment violates business rules
- **Data Validation Errors**: Amendment data fails validation checks
- **Authorization Errors**: Insufficient permissions for amendment
- **Impact Assessment Errors**: Unable to assess amendment impact

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: amendmentErrors.length == 0
- **Success Status**: amendmentStatus equals "SUCCESS"
- **Low Severity**: errorSeverity in ["LOW", "INFO"]
- **No Validation Errors**: validationErrors.length == 0
- **No Critical Categories**: errorCategories contains only non-critical types

**Failure Scenarios**:
- **Successful Amendment**: Amendment completed without errors
- **Minor Warnings**: Only non-critical warnings present
- **Recoverable Issues**: Errors that can be automatically resolved

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Amendments**: Some fields amended, others failed
- **Threshold Boundaries**: Error severity exactly at threshold
- **Complex Validations**: Multi-step validation with mixed results
- **Cascading Effects**: Amendment errors causing downstream issues

**Special Scenarios**:
- **System Maintenance**: Expected errors during maintenance windows
- **Market Hours**: Different validation rules during market hours
- **Emergency Amendments**: Relaxed validation for emergency situations

**Data Absence Handling**:
- Missing amendmentErrors treated as empty array (no errors)
- Missing amendmentStatus defaults to "UNKNOWN" (triggers error detection)
- Missing errorSeverity defaults to "UNKNOWN" (triggers error detection)

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 500 milliseconds (95th percentile)
- **Throughput**: 1200 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Very low intensity for boolean logic evaluation
- **Memory**: 24MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Amendment Service**: Access to amendment results and status
- **Validation Service**: Amendment validation results
- **Configuration Service**: Error thresholds and validation rules

**External Dependencies**:
- **Audit Service**: Error history and pattern tracking

**Data Dependencies**:
- Error severity classification configuration
- Error category configuration
- Amendment validation rules

## 9. Configuration
**Configurable Thresholds**:
- `criticalErrorSeverities`: array - Error severities considered critical - Default: ["CRITICAL", "HIGH"]
- `ignoredErrorCategories`: array - Error categories to ignore - Default: []
- `validationErrorThreshold`: integer - Maximum validation errors allowed - Default: 0

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict error detection mode - Default: true
- `warningsAsErrors`: boolean - Treat warnings as errors - Default: false
- `includeValidationErrors`: boolean - Include validation errors - Default: true

**Environment-Specific Settings**:
- Development: Relaxed error detection, more lenient validation
- Production: Strict error detection, comprehensive validation

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required amendment data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid evaluation configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to true evaluation on data unavailability (assume errors present)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to cached results for minor system issues

**Error Propagation**:
- Evaluation errors logged with amendment context
- Failed evaluations trigger manual review workflow
- Critical errors escalated to operations team
