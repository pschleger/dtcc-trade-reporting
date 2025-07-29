# hasCalculationErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasCalculationErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether calculation processes encountered errors that require attention or intervention
**Workflow Context**: PositionWorkflow and other workflows requiring calculation error detection

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `calculationStatus`: string - Current calculation status
- `calculationErrors`: array - List of calculation errors encountered
- `calculationTimestamp`: ISO-8601 timestamp - When calculation completed
- `calculationResults`: object - Calculation results and metadata

**Optional Fields**:
- `calculationWarnings`: array - Non-critical calculation warnings
- `errorSeverity`: string - Highest severity level of calculation errors
- `retryAttempts`: integer - Number of calculation retry attempts made

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "calculation-error-check" - Evaluation context identifier

**Evaluation Context**:
- Calculation error severity classification
- Error type categorization
- Retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF calculationErrors.length > 0 OR
   calculationStatus == "FAILED" OR
   calculationStatus == "ERROR" OR
   errorSeverity in ["CRITICAL", "HIGH"] OR
   calculationResults == null THEN
    RETURN true
ELSE IF calculationStatus == "SUCCESS" AND
        calculationErrors.length == 0 AND
        calculationResults != null THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of calculation errors
- Secondary evaluation considers calculation status
- Tertiary evaluation assesses error severity levels
- Result validation for calculation completeness

**Calculation Methods**:
- Error count aggregation
- Severity level assessment
- Status-based error detection
- Result presence validation

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: calculationErrors array contains one or more errors
- **Failed Status**: calculationStatus indicates failure or error state
- **High Severity**: errorSeverity indicates critical or high-priority errors
- **Missing Results**: calculationResults is null or incomplete

**Success Scenarios**:
- **Calculation Errors**: Explicit errors recorded during calculation
- **Status Failures**: Calculation status indicates failure condition
- **Critical Errors**: High-severity errors requiring immediate attention
- **Result Failures**: Calculation failed to produce expected results

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: calculationErrors array is empty
- **Success Status**: calculationStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Results Present**: calculationResults contains valid results

**Failure Scenarios**:
- **Successful Calculation**: Calculation completed without errors
- **Warning Only**: Only non-critical warnings present
- **Clean State**: No error indicators present in calculation

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: calculationErrors array exists but is empty
- **Unknown Status**: calculationStatus not in recognized values
- **Mixed Severity**: Combination of different error severity levels
- **Partial Results**: Some calculation results present, others missing

**Special Scenarios**:
- **Retry Scenarios**: Errors from previous attempts vs current attempt
- **Warning Escalation**: Warnings that should be treated as errors
- **Transient Errors**: Temporary errors that may resolve automatically

**Data Absence Handling**:
- Missing calculationErrors defaults to false (no errors)
- Missing calculationStatus defaults to false (assume success)
- Missing calculationResults may indicate error condition

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
- **Calculation Service**: Access to calculation results and error information
- **Error Classification Service**: Error severity and type classification
- **Configuration Service**: Error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Calculation error tracking and alerting integration

**Data Dependencies**:
- Calculation error severity classification rules
- Error type categorization
- Calculation status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating errors - Default: ["FAILED", "ERROR"]
- `includeWarnings`: boolean - Include warnings in error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient error types - Default: false
- `requireResults`: boolean - Require calculation results for success - Default: true

**Environment-Specific Settings**:
- Development: Relaxed error detection, include warnings
- Production: Strict error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required calculation data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with calculation context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to calculation team
