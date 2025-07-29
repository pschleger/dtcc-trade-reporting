# hasCancellationErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasCancellationErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection Evaluation
**Purpose**: Evaluates whether cancellation process encountered errors that require attention or remediation
**Workflow Context**: CancellationWorkflow (error detection state transitions)

## 2. Input Parameters
**Entity Type**: CancellationRequest
**Required Fields**:
- `cancellationStatus`: string - Current cancellation status
- `cancellationErrors`: array - List of cancellation errors
- `cancellationTimestamp`: ISO-8601 timestamp - When cancellation was attempted
- `errorSeverity`: string - Highest severity level of errors encountered
- `errorCategories`: array - Categories of errors encountered

**Optional Fields**:
- `cancellationWarnings`: array - Non-critical cancellation warnings
- `authorizationErrors`: array - Cancellation authorization errors
- `errorDetails`: object - Detailed error information

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "cancellation-error-check" - Evaluation context identifier

**Evaluation Context**:
- Error severity thresholds
- Error category classifications
- Cancellation authorization requirements

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF cancellationErrors.length > 0 OR
   cancellationStatus == "FAILED" OR
   errorSeverity in ["CRITICAL", "HIGH"] OR
   authorizationErrors.length > 0 THEN
    RETURN true
ELSE IF cancellationStatus == "SUCCESS" AND
        cancellationErrors.length == 0 AND
        errorSeverity not in ["CRITICAL", "HIGH"] THEN
    RETURN false
ELSE
    RETURN false (default to no errors detected)
```

**Boolean Logic**:
- Primary evaluation based on presence of cancellation errors
- Secondary evaluation considers cancellation status
- Tertiary evaluation validates error severity levels
- Quaternary evaluation checks authorization errors

**Calculation Methods**:
- Error count validation
- Error severity assessment
- Authorization error analysis

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: cancellationErrors.length > 0
- **Failed Status**: cancellationStatus equals "FAILED" or "ERROR"
- **High Severity**: errorSeverity in ["CRITICAL", "HIGH"]
- **Authorization Failures**: authorizationErrors.length > 0
- **Critical Categories**: errorCategories contains critical error types

**Success Scenarios**:
- **Authorization Denied**: Insufficient permissions for cancellation
- **Business Rule Violations**: Cancellation violates business rules
- **Impact Assessment Errors**: Unable to assess cancellation impact
- **Timing Errors**: Cancellation attempted outside allowed time windows

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: cancellationErrors.length == 0
- **Success Status**: cancellationStatus equals "SUCCESS"
- **Low Severity**: errorSeverity in ["LOW", "INFO"]
- **No Authorization Errors**: authorizationErrors.length == 0
- **No Critical Categories**: errorCategories contains only non-critical types

**Failure Scenarios**:
- **Successful Cancellation**: Cancellation completed without errors
- **Minor Warnings**: Only non-critical warnings present
- **Recoverable Issues**: Errors that can be automatically resolved

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Cancellations**: Some aspects cancelled, others failed
- **Threshold Boundaries**: Error severity exactly at threshold
- **Complex Authorizations**: Multi-level authorization with mixed results
- **Cascading Effects**: Cancellation errors causing downstream issues

**Special Scenarios**:
- **System Maintenance**: Expected errors during maintenance windows
- **Market Hours**: Different cancellation rules during market hours
- **Emergency Cancellations**: Relaxed validation for emergency situations

**Data Absence Handling**:
- Missing cancellationErrors treated as empty array (no errors)
- Missing cancellationStatus defaults to "UNKNOWN" (triggers error detection)
- Missing errorSeverity defaults to "UNKNOWN" (triggers error detection)

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 500 milliseconds (95th percentile)
- **Throughput**: 1200 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Very low intensity for boolean logic evaluation
- **Memory**: 24MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Cancellation Service**: Access to cancellation results and status
- **Authorization Service**: Cancellation authorization results
- **Configuration Service**: Error thresholds and authorization rules

**External Dependencies**:
- **Audit Service**: Error history and pattern tracking

**Data Dependencies**:
- Error severity classification configuration
- Error category configuration
- Cancellation authorization rules

## 9. Configuration
**Configurable Thresholds**:
- `criticalErrorSeverities`: array - Error severities considered critical - Default: ["CRITICAL", "HIGH"]
- `ignoredErrorCategories`: array - Error categories to ignore - Default: []
- `authorizationErrorThreshold`: integer - Maximum authorization errors allowed - Default: 0

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict error detection mode - Default: true
- `warningsAsErrors`: boolean - Treat warnings as errors - Default: false
- `includeAuthorizationErrors`: boolean - Include authorization errors - Default: true

**Environment-Specific Settings**:
- Development: Relaxed error detection, more lenient authorization
- Production: Strict error detection, comprehensive authorization

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required cancellation data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid evaluation configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to true evaluation on data unavailability (assume errors present)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to cached results for minor system issues

**Error Propagation**:
- Evaluation errors logged with cancellation context
- Failed evaluations trigger manual review workflow
- Critical errors escalated to operations team
