# hasImpactAssessmentErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasImpactAssessmentErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether impact assessment process encountered errors that require attention or intervention
**Workflow Context**: Amendment and cancellation workflows requiring impact assessment error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `impactAssessmentStatus`: string - Current impact assessment processing status
- `impactAssessmentErrors`: array - List of impact assessment errors encountered
- `impactAssessmentTimestamp`: ISO-8601 timestamp - When impact assessment completed
- `errorSeverity`: string - Highest severity level of impact assessment errors

**Optional Fields**:
- `impactAssessmentWarnings`: array - Non-critical impact assessment warnings
- `errorContext`: object - Additional impact assessment error context information
- `retryAttempts`: integer - Number of impact assessment retry attempts made
- `assessmentStage`: string - Stage where impact assessment error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "impact-assessment-error-check" - Evaluation context identifier

**Evaluation Context**:
- Impact assessment error severity classification
- Impact assessment error type categorization
- Impact assessment retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF impactAssessmentErrors.length > 0 OR
   impactAssessmentStatus == "FAILED" OR
   impactAssessmentStatus == "ERROR" OR
   impactAssessmentStatus == "INCOMPLETE" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF impactAssessmentStatus == "SUCCESS" AND
        impactAssessmentErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of impact assessment errors
- Secondary evaluation considers impact assessment processing status
- Tertiary evaluation assesses impact assessment error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Impact assessment error count aggregation
- Impact assessment severity level assessment
- Impact assessment status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: impactAssessmentErrors array contains one or more errors
- **Failed Status**: impactAssessmentStatus indicates failure, error, or incomplete state
- **High Severity**: errorSeverity indicates critical or high-priority impact assessment errors
- **Assessment Failures**: Impact assessment processing encountered system-level failures

**Success Scenarios**:
- **Assessment Errors**: Explicit errors recorded during impact assessment processing
- **Status Failures**: Impact assessment status indicates failure condition
- **Critical Errors**: High-severity impact assessment errors requiring immediate attention
- **Incomplete Assessment**: Impact assessment could not be completed due to errors

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: impactAssessmentErrors array is empty
- **Success Status**: impactAssessmentStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No impact assessment errors or failures detected

**Failure Scenarios**:
- **Successful Assessment**: Impact assessment processing completed without errors
- **Warning Only**: Only non-critical impact assessment warnings present
- **Clean State**: No impact assessment error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: impactAssessmentErrors array exists but is empty
- **Unknown Status**: impactAssessmentStatus not in recognized values
- **Mixed Severity**: Combination of different impact assessment error severity levels
- **Stale Errors**: Old impact assessment errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Impact assessment errors from previous attempts vs current attempt
- **Warning Escalation**: Impact assessment warnings that should be treated as errors
- **Transient Errors**: Temporary impact assessment errors that may resolve automatically

**Data Absence Handling**:
- Missing impactAssessmentErrors defaults to false (no errors)
- Missing impactAssessmentStatus defaults to false (assume success)
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
- **Error Classification Service**: Impact assessment error severity and type classification
- **Configuration Service**: Impact assessment error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Impact assessment error tracking and alerting integration

**Data Dependencies**:
- Impact assessment error severity classification rules
- Impact assessment error type categorization
- Impact assessment processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating impact assessment errors - Default: ["FAILED", "ERROR", "INCOMPLETE"]
- `includeWarnings`: boolean - Include warnings in impact assessment error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict impact assessment error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient impact assessment error types - Default: false
- `maxErrorAge`: integer - Maximum age of impact assessment errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed impact assessment error detection, include warnings
- Production: Strict impact assessment error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required impact assessment error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid impact assessment error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic impact assessment error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with impact assessment processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
