# hasProcessingErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasProcessingErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether processing encountered errors that require attention or intervention
**Workflow Context**: Multiple workflows requiring error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `processingStatus`: string - Current processing status
- `processingErrors`: array - List of processing errors encountered
- `processingTimestamp`: ISO-8601 timestamp - When processing completed
- `errorSeverity`: string - Highest severity level of errors

**Optional Fields**:
- `processingWarnings`: array - Non-critical processing warnings
- `errorContext`: object - Additional error context information
- `retryAttempts`: integer - Number of retry attempts made

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "processing-error-check" - Evaluation context identifier

**Evaluation Context**:
- Error severity classification
- Error type categorization
- Retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF processingErrors.length > 0 OR
   processingStatus == "FAILED" OR
   processingStatus == "ERROR" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF processingStatus == "SUCCESS" AND
        processingErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of processing errors
- Secondary evaluation considers processing status
- Tertiary evaluation assesses error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Error count aggregation
- Severity level assessment
- Status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: processingErrors array contains one or more errors
- **Failed Status**: processingStatus indicates failure or error state
- **High Severity**: errorSeverity indicates critical or high-priority errors
- **System Errors**: Processing encountered system-level failures

**Success Scenarios**:
- **Processing Errors**: Explicit errors recorded during processing
- **Status Failures**: Processing status indicates failure condition
- **Critical Errors**: High-severity errors requiring immediate attention
- **System Failures**: Infrastructure or service failures detected

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: processingErrors array is empty
- **Success Status**: processingStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No errors or failures detected

**Failure Scenarios**:
- **Successful Processing**: Processing completed without errors
- **Warning Only**: Only non-critical warnings present
- **Clean State**: No error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: processingErrors array exists but is empty
- **Unknown Status**: processingStatus not in recognized values
- **Mixed Severity**: Combination of different error severity levels
- **Stale Errors**: Old errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Errors from previous attempts vs current attempt
- **Warning Escalation**: Warnings that should be treated as errors
- **Transient Errors**: Temporary errors that may resolve automatically

**Data Absence Handling**:
- Missing processingErrors defaults to false (no errors)
- Missing processingStatus defaults to false (assume success)
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
- **Error Classification Service**: Error severity and type classification
- **Configuration Service**: Error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Error tracking and alerting integration

**Data Dependencies**:
- Error severity classification rules
- Error type categorization
- Processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating errors - Default: ["FAILED", "ERROR"]
- `includeWarnings`: boolean - Include warnings in error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient error types - Default: false
- `maxErrorAge`: integer - Maximum age of errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed error detection, include warnings
- Production: Strict error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
