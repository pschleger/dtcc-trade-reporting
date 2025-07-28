# hasApplicationErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasApplicationErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether application process encountered errors that require attention or intervention
**Workflow Context**: Amendment and cancellation workflows requiring application error detection for proper error handling

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `applicationStatus`: string - Current application processing status
- `applicationErrors`: array - List of application errors encountered
- `applicationTimestamp`: ISO-8601 timestamp - When application processing completed
- `errorSeverity`: string - Highest severity level of application errors

**Optional Fields**:
- `applicationWarnings`: array - Non-critical application warnings
- `errorContext`: object - Additional application error context information
- `retryAttempts`: integer - Number of application retry attempts made
- `applicationStage`: string - Stage where application error occurred

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "application-error-check" - Evaluation context identifier

**Evaluation Context**:
- Application error severity classification
- Application error type categorization
- Application retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF applicationErrors.length > 0 OR
   applicationStatus == "FAILED" OR
   applicationStatus == "ERROR" OR
   applicationStatus == "REJECTED" OR
   errorSeverity in ["CRITICAL", "HIGH"] THEN
    RETURN true
ELSE IF applicationStatus == "SUCCESS" AND
        applicationErrors.length == 0 THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of application errors
- Secondary evaluation considers application processing status
- Tertiary evaluation assesses application error severity levels
- Default behavior assumes no errors for ambiguous states

**Calculation Methods**:
- Application error count aggregation
- Application severity level assessment
- Application status-based error detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: applicationErrors array contains one or more errors
- **Failed Status**: applicationStatus indicates failure, error, or rejection state
- **High Severity**: errorSeverity indicates critical or high-priority application errors
- **Application Failures**: Application processing encountered system-level failures

**Success Scenarios**:
- **Application Errors**: Explicit errors recorded during application processing
- **Status Failures**: Application status indicates failure condition
- **Critical Errors**: High-severity application errors requiring immediate attention
- **Rejection Errors**: Application rejected due to business rule violations

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: applicationErrors array is empty
- **Success Status**: applicationStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Clean Processing**: No application errors or failures detected

**Failure Scenarios**:
- **Successful Application**: Application processing completed without errors
- **Warning Only**: Only non-critical application warnings present
- **Clean State**: No application error indicators present in entity

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: applicationErrors array exists but is empty
- **Unknown Status**: applicationStatus not in recognized values
- **Mixed Severity**: Combination of different application error severity levels
- **Stale Errors**: Old application errors from previous processing attempts

**Special Scenarios**:
- **Retry Scenarios**: Application errors from previous attempts vs current attempt
- **Warning Escalation**: Application warnings that should be treated as errors
- **Transient Errors**: Temporary application errors that may resolve automatically

**Data Absence Handling**:
- Missing applicationErrors defaults to false (no errors)
- Missing applicationStatus defaults to false (assume success)
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
- **Error Classification Service**: Application error severity and type classification
- **Configuration Service**: Application error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Application error tracking and alerting integration

**Data Dependencies**:
- Application error severity classification rules
- Application error type categorization
- Application processing status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating application errors - Default: ["FAILED", "ERROR", "REJECTED"]
- `includeWarnings`: boolean - Include warnings in application error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict application error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient application error types - Default: false
- `maxErrorAge`: integer - Maximum age of application errors to consider (hours) - Default: 24

**Environment-Specific Settings**:
- Development: Relaxed application error detection, include warnings
- Production: Strict application error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required application error data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid application error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic application error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with application processing context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to operations team
