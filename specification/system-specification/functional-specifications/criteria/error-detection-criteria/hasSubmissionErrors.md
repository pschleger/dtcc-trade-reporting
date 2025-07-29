# hasSubmissionErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasSubmissionErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection Evaluation
**Purpose**: Evaluates whether submission process encountered errors that require attention or remediation
**Workflow Context**: RegulatoryReportingWorkflow (error detection state transitions)

## 2. Input Parameters
**Entity Type**: SubmissionRequest
**Required Fields**:
- `submissionStatus`: string - Current submission status
- `submissionErrors`: array - List of submission errors
- `submissionTimestamp`: ISO-8601 timestamp - When submission was attempted
- `errorSeverity`: string - Highest severity level of errors encountered
- `errorCategories`: array - Categories of errors encountered

**Optional Fields**:
- `submissionWarnings`: array - Non-critical submission warnings
- `retryAttempts`: integer - Number of retry attempts made
- `errorDetails`: object - Detailed error information

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "submission-error-check" - Evaluation context identifier

**Evaluation Context**:
- Error severity thresholds
- Error category classifications
- Retry attempt limits

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF submissionErrors.length > 0 OR
   submissionStatus == "FAILED" OR
   errorSeverity in ["CRITICAL", "HIGH"] OR
   retryAttempts >= maxRetries THEN
    RETURN true
ELSE IF submissionStatus == "SUCCESS" AND
        submissionErrors.length == 0 AND
        errorSeverity not in ["CRITICAL", "HIGH"] THEN
    RETURN false
ELSE
    RETURN false (default to no errors detected)
```

**Boolean Logic**:
- Primary evaluation based on presence of submission errors
- Secondary evaluation considers submission status
- Tertiary evaluation validates error severity levels
- Quaternary evaluation checks retry attempt limits

**Calculation Methods**:
- Error count validation
- Error severity assessment
- Retry attempt threshold comparison

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: submissionErrors.length > 0
- **Failed Status**: submissionStatus equals "FAILED" or "ERROR"
- **High Severity**: errorSeverity in ["CRITICAL", "HIGH"]
- **Retry Exceeded**: retryAttempts >= maxRetries (default: 3)
- **Critical Categories**: errorCategories contains critical error types

**Success Scenarios**:
- **Network Errors**: Communication failures with external systems
- **Validation Errors**: Data validation failures during submission
- **Authentication Errors**: Security or credential failures
- **Timeout Errors**: Submission process exceeded time limits

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: submissionErrors.length == 0
- **Success Status**: submissionStatus equals "SUCCESS"
- **Low Severity**: errorSeverity in ["LOW", "INFO"]
- **Within Retry Limits**: retryAttempts < maxRetries
- **No Critical Categories**: errorCategories contains only non-critical types

**Failure Scenarios**:
- **Successful Submission**: All submissions completed without errors
- **Minor Warnings**: Only non-critical warnings present
- **Recoverable Issues**: Errors that can be automatically resolved

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Submissions**: Some reports submitted, others failed
- **Threshold Boundaries**: Error severity exactly at threshold
- **Retry Boundaries**: Retry attempts exactly at limit
- **Mixed Error Types**: Combination of critical and non-critical errors

**Special Scenarios**:
- **System Maintenance**: Expected errors during maintenance windows
- **Network Degradation**: Temporary connectivity issues
- **External System Issues**: Errors caused by external system problems

**Data Absence Handling**:
- Missing submissionErrors treated as empty array (no errors)
- Missing submissionStatus defaults to "UNKNOWN" (triggers error detection)
- Missing errorSeverity defaults to "UNKNOWN" (triggers error detection)

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 500 milliseconds (95th percentile)
- **Throughput**: 1500 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Very low intensity for boolean logic evaluation
- **Memory**: 16MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Submission Service**: Access to submission results and status
- **Error Classification Service**: Error severity and category information
- **Configuration Service**: Error thresholds and retry limits

**External Dependencies**:
- **Audit Service**: Error history and pattern tracking

**Data Dependencies**:
- Error severity classification configuration
- Error category configuration
- Retry limit configuration

## 9. Configuration
**Configurable Thresholds**:
- `maxRetryAttempts`: integer - Maximum retry attempts before error - Default: 3
- `criticalErrorSeverities`: array - Error severities considered critical - Default: ["CRITICAL", "HIGH"]
- `ignoredErrorCategories`: array - Error categories to ignore - Default: []

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict error detection mode - Default: true
- `warningsAsErrors`: boolean - Treat warnings as errors - Default: false
- `includeRetryErrors`: boolean - Include retry-related errors - Default: true

**Environment-Specific Settings**:
- Development: Relaxed error detection, higher retry limits
- Production: Strict error detection, standard retry limits

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required submission data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid evaluation configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to true evaluation on data unavailability (assume errors present)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to cached results for minor system issues

**Error Propagation**:
- Evaluation errors logged with submission context
- Failed evaluations trigger manual review workflow
- Critical errors escalated to operations team
