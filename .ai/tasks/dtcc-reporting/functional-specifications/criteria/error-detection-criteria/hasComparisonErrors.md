# hasComparisonErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasComparisonErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether comparison process encountered errors that require attention or intervention
**Workflow Context**: Reconciliation workflows requiring comparison error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `comparisonStatus`: string - Current comparison processing status
- `comparisonErrors`: array - List of comparison errors encountered
- `comparisonTimestamp`: ISO-8601 timestamp - When comparison completed
- `errorSeverity`: string - Highest severity level of comparison errors

**Optional Fields**:
- `comparisonWarnings`: array - Non-critical comparison warnings
- `errorContext`: object - Additional comparison error context information
- `retryAttempts`: integer - Number of comparison retry attempts made
- `comparisonStage`: string - Stage where comparison error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "comparison-error-check" - Evaluation context identifier

**Evaluation Context**:
- Comparison error severity classification
- Comparison error type categorization
- Comparison retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF comparisonErrors.length > 0 OR
   comparisonStatus == "FAILED" OR
   comparisonStatus == "ERROR" OR
   comparisonStatus == "INCOMPLETE" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF comparisonStatus == "SUCCESS" AND
        comparisonErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of comparison errors
- Secondary evaluation considers comparison processing status
- Tertiary evaluation assesses comparison error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Comparison error count aggregation
- Comparison severity level assessment
- Comparison status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: comparisonErrors array contains one or more errors
- **Failed Status**: comparisonStatus indicates failure, error, or incomplete state
- **High Severity**: errorSeverity indicates critical or high-priority comparison errors
- **Comparison Failures**: Comparison processing encountered system-level failures

**Success Scenarios**:
- **Comparison Errors**: Explicit errors recorded during comparison processing
- **Status Failures**: Comparison status indicates failure condition
- **Critical Errors**: High-severity comparison errors requiring immediate attention
- **Incomplete Comparison**: Comparison could not be completed due to errors

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: comparisonErrors array is empty
- **Success Status**: comparisonStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No comparison errors or failures detected

**Failure Scenarios**:
- **Successful Comparison**: Comparison processing completed without errors
- **Warning Only**: Only non-critical comparison warnings present
- **Clean State**: No comparison error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: comparisonErrors array exists but is empty
- **Unknown Status**: comparisonStatus not in recognized values
- **Mixed Severity**: Combination of different comparison error severity levels
- **Stale Errors**: Old comparison errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Comparison errors from previous attempts vs current attempt
- **Warning Escalation**: Comparison warnings that should be treated as errors
- **Transient Errors**: Temporary comparison errors that may resolve automatically

**Data Absence Handling**:
- Missing comparisonErrors defaults to false (no errors)
- Missing comparisonStatus defaults to false (assume success)
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
- **Error Classification Service**: Comparison error severity and type classification
- **Configuration Service**: Comparison error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Comparison error tracking and alerting integration

**Data Dependencies**:
- Comparison error severity classification rules
- Comparison error type categorization
- Comparison processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating comparison errors - Default: ["FAILED", "ERROR", "INCOMPLETE"]
- `includeWarnings`: boolean - Include warnings in comparison error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict comparison error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient comparison error types - Default: false
- `maxErrorAge`: integer - Maximum age of comparison errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed comparison error detection, include warnings
- Production: Strict comparison error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required comparison error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid comparison error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic comparison error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with comparison processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
