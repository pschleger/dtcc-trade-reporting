# hasExecutionErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasExecutionErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether execution process encountered errors that require attention or intervention
**Workflow Context**: Amendment and cancellation workflows requiring execution error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `executionStatus`: string - Current execution processing status
- `executionErrors`: array - List of execution errors encountered
- `executionTimestamp`: ISO-8601 timestamp - When execution completed
- `errorSeverity`: string - Highest severity level of execution errors

**Optional Fields**:
- `executionWarnings`: array - Non-critical execution warnings
- `errorContext`: object - Additional execution error context information
- `retryAttempts`: integer - Number of execution retry attempts made
- `executionStage`: string - Stage where execution error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "execution-error-check" - Evaluation context identifier

**Evaluation Context**:
- Execution error severity classification
- Execution error type categorization
- Execution retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF executionErrors.length > 0 OR
   executionStatus == "FAILED" OR
   executionStatus == "ERROR" OR
   executionStatus == "ABORTED" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF executionStatus == "SUCCESS" AND
        executionErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of execution errors
- Secondary evaluation considers execution processing status
- Tertiary evaluation assesses execution error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Execution error count aggregation
- Execution severity level assessment
- Execution status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: executionErrors array contains one or more errors
- **Failed Status**: executionStatus indicates failure, error, or aborted state
- **High Severity**: errorSeverity indicates critical or high-priority execution errors
- **Execution Failures**: Execution processing encountered system-level failures

**Success Scenarios**:
- **Execution Errors**: Explicit errors recorded during execution processing
- **Status Failures**: Execution status indicates failure condition
- **Critical Errors**: High-severity execution errors requiring immediate attention
- **Aborted Execution**: Execution was aborted due to critical errors

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: executionErrors array is empty
- **Success Status**: executionStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No execution errors or failures detected

**Failure Scenarios**:
- **Successful Execution**: Execution processing completed without errors
- **Warning Only**: Only non-critical execution warnings present
- **Clean State**: No execution error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: executionErrors array exists but is empty
- **Unknown Status**: executionStatus not in recognized values
- **Mixed Severity**: Combination of different execution error severity levels
- **Stale Errors**: Old execution errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Execution errors from previous attempts vs current attempt
- **Warning Escalation**: Execution warnings that should be treated as errors
- **Transient Errors**: Temporary execution errors that may resolve automatically

**Data Absence Handling**:
- Missing executionErrors defaults to false (no errors)
- Missing executionStatus defaults to false (assume success)
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
- **Error Classification Service**: Execution error severity and type classification
- **Configuration Service**: Execution error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Execution error tracking and alerting integration

**Data Dependencies**:
- Execution error severity classification rules
- Execution error type categorization
- Execution processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating execution errors - Default: ["FAILED", "ERROR", "ABORTED"]
- `includeWarnings`: boolean - Include warnings in execution error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict execution error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient execution error types - Default: false
- `maxErrorAge`: integer - Maximum age of execution errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed execution error detection, include warnings
- Production: Strict execution error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required execution error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid execution error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic execution error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with execution processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
