# hasReportingErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasReportingErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether reporting process encountered errors that require attention or intervention
**Workflow Context**: Regulatory reporting workflows requiring reporting error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `reportingStatus`: string - Current reporting processing status
- `reportingErrors`: array - List of reporting errors encountered
- `reportingTimestamp`: ISO-8601 timestamp - When reporting completed
- `errorSeverity`: string - Highest severity level of reporting errors

**Optional Fields**:
- `reportingWarnings`: array - Non-critical reporting warnings
- `errorContext`: object - Additional reporting error context information
- `retryAttempts`: integer - Number of reporting retry attempts made
- `reportingStage`: string - Stage where reporting error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "reporting-error-check" - Evaluation context identifier

**Evaluation Context**:
- Reporting error severity classification
- Reporting error type categorization
- Reporting retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF reportingErrors.length > 0 OR
   reportingStatus == "FAILED" OR
   reportingStatus == "ERROR" OR
   reportingStatus == "REJECTED" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF reportingStatus == "SUCCESS" AND
        reportingErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of reporting errors
- Secondary evaluation considers reporting processing status
- Tertiary evaluation assesses reporting error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Reporting error count aggregation
- Reporting severity level assessment
- Reporting status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: reportingErrors array contains one or more errors
- **Failed Status**: reportingStatus indicates failure, error, or rejection state
- **High Severity**: errorSeverity indicates critical or high-priority reporting errors
- **Reporting Failures**: Reporting processing encountered system-level failures

**Success Scenarios**:
- **Reporting Errors**: Explicit errors recorded during reporting processing
- **Status Failures**: Reporting status indicates failure condition
- **Critical Errors**: High-severity reporting errors requiring immediate attention
- **Rejection Errors**: Report rejected by regulatory authority due to errors

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: reportingErrors array is empty
- **Success Status**: reportingStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No reporting errors or failures detected

**Failure Scenarios**:
- **Successful Reporting**: Reporting processing completed without errors
- **Warning Only**: Only non-critical reporting warnings present
- **Clean State**: No reporting error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: reportingErrors array exists but is empty
- **Unknown Status**: reportingStatus not in recognized values
- **Mixed Severity**: Combination of different reporting error severity levels
- **Stale Errors**: Old reporting errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Reporting errors from previous attempts vs current attempt
- **Warning Escalation**: Reporting warnings that should be treated as errors
- **Transient Errors**: Temporary reporting errors that may resolve automatically

**Data Absence Handling**:
- Missing reportingErrors defaults to false (no errors)
- Missing reportingStatus defaults to false (assume success)
- Missing errorSeverity defaults to false (no critical errors)

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 1 second (95th percentile)
- **Throughput**: 2000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Very low intensity for simple boolean evaluation
- **Memory**: 16MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Error Classification Service**: Reporting error severity and type classification
- **Configuration Service**: Reporting error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Reporting error tracking and alerting integration

**Data Dependencies**:
- Reporting error severity classification rules
- Reporting error type categorization
- Reporting processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating reporting errors - Default: ["FAILED", "ERROR", "REJECTED"]
- `includeWarnings`: boolean - Include warnings in reporting error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict reporting error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient reporting error types - Default: false
- `maxErrorAge`: integer - Maximum age of reporting errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed reporting error detection, include warnings
- Production: Strict reporting error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required reporting error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid reporting error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic reporting error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with reporting processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
