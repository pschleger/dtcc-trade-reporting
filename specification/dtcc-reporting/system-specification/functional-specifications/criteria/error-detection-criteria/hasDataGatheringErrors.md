# hasDataGatheringErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasDataGatheringErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether data gathering process encountered errors that require attention or intervention
**Workflow Context**: Reconciliation workflows requiring data gathering error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `dataGatheringStatus`: string - Current data gathering processing status
- `dataGatheringErrors`: array - List of data gathering errors encountered
- `dataGatheringTimestamp`: ISO-8601 timestamp - When data gathering completed
- `errorSeverity`: string - Highest severity level of data gathering errors

**Optional Fields**:
- `dataGatheringWarnings`: array - Non-critical data gathering warnings
- `errorContext`: object - Additional data gathering error context information
- `retryAttempts`: integer - Number of data gathering retry attempts made
- `gatheringStage`: string - Stage where data gathering error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "data-gathering-error-check" - Evaluation context identifier

**Evaluation Context**:
- Data gathering error severity classification
- Data gathering error type categorization
- Data gathering retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF dataGatheringErrors.length > 0 OR
   dataGatheringStatus == "FAILED" OR
   dataGatheringStatus == "ERROR" OR
   dataGatheringStatus == "INCOMPLETE" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF dataGatheringStatus == "SUCCESS" AND
        dataGatheringErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of data gathering errors
- Secondary evaluation considers data gathering processing status
- Tertiary evaluation assesses data gathering error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Data gathering error count aggregation
- Data gathering severity level assessment
- Data gathering status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: dataGatheringErrors array contains one or more errors
- **Failed Status**: dataGatheringStatus indicates failure, error, or incomplete state
- **High Severity**: errorSeverity indicates critical or high-priority data gathering errors
- **Gathering Failures**: Data gathering processing encountered system-level failures

**Success Scenarios**:
- **Gathering Errors**: Explicit errors recorded during data gathering processing
- **Status Failures**: Data gathering status indicates failure condition
- **Critical Errors**: High-severity data gathering errors requiring immediate attention
- **Incomplete Gathering**: Data gathering could not be completed due to errors

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: dataGatheringErrors array is empty
- **Success Status**: dataGatheringStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No data gathering errors or failures detected

**Failure Scenarios**:
- **Successful Gathering**: Data gathering processing completed without errors
- **Warning Only**: Only non-critical data gathering warnings present
- **Clean State**: No data gathering error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: dataGatheringErrors array exists but is empty
- **Unknown Status**: dataGatheringStatus not in recognized values
- **Mixed Severity**: Combination of different data gathering error severity levels
- **Stale Errors**: Old data gathering errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Data gathering errors from previous attempts vs current attempt
- **Warning Escalation**: Data gathering warnings that should be treated as errors
- **Transient Errors**: Temporary data gathering errors that may resolve automatically

**Data Absence Handling**:
- Missing dataGatheringErrors defaults to false (no errors)
- Missing dataGatheringStatus defaults to false (assume success)
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
- **Error Classification Service**: Data gathering error severity and type classification
- **Configuration Service**: Data gathering error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Data gathering error tracking and alerting integration

**Data Dependencies**:
- Data gathering error severity classification rules
- Data gathering error type categorization
- Data gathering processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating data gathering errors - Default: ["FAILED", "ERROR", "INCOMPLETE"]
- `includeWarnings`: boolean - Include warnings in data gathering error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict data gathering error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient data gathering error types - Default: false
- `maxErrorAge`: integer - Maximum age of data gathering errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed data gathering error detection, include warnings
- Production: Strict data gathering error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required data gathering error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid data gathering error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic data gathering error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with data gathering processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
