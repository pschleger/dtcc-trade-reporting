# hasReconciliationErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasReconciliationErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether reconciliation process encountered errors that require attention or intervention
**Workflow Context**: Reconciliation workflows requiring reconciliation error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `reconciliationStatus`: string - Current reconciliation processing status
- `reconciliationErrors`: array - List of reconciliation errors encountered
- `reconciliationTimestamp`: ISO-8601 timestamp - When reconciliation completed
- `errorSeverity`: string - Highest severity level of reconciliation errors

**Optional Fields**:
- `reconciliationWarnings`: array - Non-critical reconciliation warnings
- `errorContext`: object - Additional reconciliation error context information
- `retryAttempts`: integer - Number of reconciliation retry attempts made
- `reconciliationStage`: string - Stage where reconciliation error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "reconciliation-error-check" - Evaluation context identifier

**Evaluation Context**:
- Reconciliation error severity classification
- Reconciliation error type categorization
- Reconciliation retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF reconciliationErrors.length > 0 OR
   reconciliationStatus == "FAILED" OR
   reconciliationStatus == "ERROR" OR
   reconciliationStatus == "INCOMPLETE" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF reconciliationStatus == "SUCCESS" AND
        reconciliationErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of reconciliation errors
- Secondary evaluation considers reconciliation processing status
- Tertiary evaluation assesses reconciliation error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Reconciliation error count aggregation
- Reconciliation severity level assessment
- Reconciliation status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: reconciliationErrors array contains one or more errors
- **Failed Status**: reconciliationStatus indicates failure, error, or incomplete state
- **High Severity**: errorSeverity indicates critical or high-priority reconciliation errors
- **Reconciliation Failures**: Reconciliation processing encountered system-level failures

**Success Scenarios**:
- **Reconciliation Errors**: Explicit errors recorded during reconciliation processing
- **Status Failures**: Reconciliation status indicates failure condition
- **Critical Errors**: High-severity reconciliation errors requiring immediate attention
- **Incomplete Reconciliation**: Reconciliation could not be completed due to errors

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: reconciliationErrors array is empty
- **Success Status**: reconciliationStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No reconciliation errors or failures detected

**Failure Scenarios**:
- **Successful Reconciliation**: Reconciliation processing completed without errors
- **Warning Only**: Only non-critical reconciliation warnings present
- **Clean State**: No reconciliation error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: reconciliationErrors array exists but is empty
- **Unknown Status**: reconciliationStatus not in recognized values
- **Mixed Severity**: Combination of different reconciliation error severity levels
- **Stale Errors**: Old reconciliation errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Reconciliation errors from previous attempts vs current attempt
- **Warning Escalation**: Reconciliation warnings that should be treated as errors
- **Transient Errors**: Temporary reconciliation errors that may resolve automatically

**Data Absence Handling**:
- Missing reconciliationErrors defaults to false (no errors)
- Missing reconciliationStatus defaults to false (assume success)
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
- **Error Classification Service**: Reconciliation error severity and type classification
- **Configuration Service**: Reconciliation error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Reconciliation error tracking and alerting integration

**Data Dependencies**:
- Reconciliation error severity classification rules
- Reconciliation error type categorization
- Reconciliation processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating reconciliation errors - Default: ["FAILED", "ERROR", "INCOMPLETE"]
- `includeWarnings`: boolean - Include warnings in reconciliation error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict reconciliation error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient reconciliation error types - Default: false
- `maxErrorAge`: integer - Maximum age of reconciliation errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed reconciliation error detection, include warnings
- Production: Strict reconciliation error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required reconciliation error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid reconciliation error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic reconciliation error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with reconciliation processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
