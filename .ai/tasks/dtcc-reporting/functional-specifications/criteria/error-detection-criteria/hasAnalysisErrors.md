# hasAnalysisErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasAnalysisErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether analysis process encountered errors that require attention or intervention
**Workflow Context**: Reconciliation workflows requiring analysis error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `analysisStatus`: string - Current analysis processing status
- `analysisErrors`: array - List of analysis errors encountered
- `analysisTimestamp`: ISO-8601 timestamp - When analysis completed
- `errorSeverity`: string - Highest severity level of analysis errors

**Optional Fields**:
- `analysisWarnings`: array - Non-critical analysis warnings
- `errorContext`: object - Additional analysis error context information
- `retryAttempts`: integer - Number of analysis retry attempts made
- `analysisStage`: string - Stage where analysis error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "analysis-error-check" - Evaluation context identifier

**Evaluation Context**:
- Analysis error severity classification
- Analysis error type categorization
- Analysis retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF analysisErrors.length > 0 OR
   analysisStatus == "FAILED" OR
   analysisStatus == "ERROR" OR
   analysisStatus == "INCOMPLETE" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF analysisStatus == "SUCCESS" AND
        analysisErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of analysis errors
- Secondary evaluation considers analysis processing status
- Tertiary evaluation assesses analysis error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Analysis error count aggregation
- Analysis severity level assessment
- Analysis status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: analysisErrors array contains one or more errors
- **Failed Status**: analysisStatus indicates failure, error, or incomplete state
- **High Severity**: errorSeverity indicates critical or high-priority analysis errors
- **Analysis Failures**: Analysis processing encountered system-level failures

**Success Scenarios**:
- **Analysis Errors**: Explicit errors recorded during analysis processing
- **Status Failures**: Analysis status indicates failure condition
- **Critical Errors**: High-severity analysis errors requiring immediate attention
- **Incomplete Analysis**: Analysis could not be completed due to errors

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: analysisErrors array is empty
- **Success Status**: analysisStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No analysis errors or failures detected

**Failure Scenarios**:
- **Successful Analysis**: Analysis processing completed without errors
- **Warning Only**: Only non-critical analysis warnings present
- **Clean State**: No analysis error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: analysisErrors array exists but is empty
- **Unknown Status**: analysisStatus not in recognized values
- **Mixed Severity**: Combination of different analysis error severity levels
- **Stale Errors**: Old analysis errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Analysis errors from previous attempts vs current attempt
- **Warning Escalation**: Analysis warnings that should be treated as errors
- **Transient Errors**: Temporary analysis errors that may resolve automatically

**Data Absence Handling**:
- Missing analysisErrors defaults to false (no errors)
- Missing analysisStatus defaults to false (assume success)
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
- **Error Classification Service**: Analysis error severity and type classification
- **Configuration Service**: Analysis error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Analysis error tracking and alerting integration

**Data Dependencies**:
- Analysis error severity classification rules
- Analysis error type categorization
- Analysis processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating analysis errors - Default: ["FAILED", "ERROR", "INCOMPLETE"]
- `includeWarnings`: boolean - Include warnings in analysis error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict analysis error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient analysis error types - Default: false
- `maxErrorAge`: integer - Maximum age of analysis errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed analysis error detection, include warnings
- Production: Strict analysis error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required analysis error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid analysis error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic analysis error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with analysis processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
