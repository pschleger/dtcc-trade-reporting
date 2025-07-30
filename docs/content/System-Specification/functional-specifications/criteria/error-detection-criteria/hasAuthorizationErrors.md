# hasAuthorizationErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasAuthorizationErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection Evaluation
**Purpose**: Evaluates whether authorization process encountered errors that require attention or remediation
**Workflow Context**: Multiple workflows (authorization error detection state transitions)

## 2. Input Parameters
**Entity Type**: AuthorizationRequest
**Required Fields**:
- `authorizationStatus`: string - Current authorization status
- `authorizationErrors`: array - List of authorization errors
- `authorizationTimestamp`: ISO-8601 timestamp - When authorization was attempted
- `errorSeverity`: string - Highest severity level of errors encountered
- `errorCategories`: array - Categories of errors encountered

**Optional Fields**:
- `authorizationWarnings`: array - Non-critical authorization warnings
- `permissionErrors`: array - Permission-related errors
- `errorDetails`: object - Detailed error information

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "authorization-error-check" - Evaluation context identifier

**Evaluation Context**:
- Error severity thresholds
- Error category classifications
- Authorization permission requirements

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF authorizationErrors.length > 0 OR
   authorizationStatus == "FAILED" OR
   errorSeverity in ["CRITICAL", "HIGH"] OR
   permissionErrors.length > 0 THEN
    RETURN true
ELSE IF authorizationStatus == "SUCCESS" AND
        authorizationErrors.length == 0 AND
        errorSeverity not in ["CRITICAL", "HIGH"] THEN
    RETURN false
ELSE
    RETURN false (default to no errors detected)
```

**Boolean Logic**:
- Primary evaluation based on presence of authorization errors
- Secondary evaluation considers authorization status
- Tertiary evaluation validates error severity levels
- Quaternary evaluation checks permission errors

**Calculation Methods**:
- Error count validation
- Error severity assessment
- Permission error analysis

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: authorizationErrors.length > 0
- **Failed Status**: authorizationStatus equals "FAILED" or "DENIED"
- **High Severity**: errorSeverity in ["CRITICAL", "HIGH"]
- **Permission Failures**: permissionErrors.length > 0
- **Critical Categories**: errorCategories contains critical error types

**Success Scenarios**:
- **Access Denied**: User lacks required permissions
- **Authentication Failures**: Invalid credentials or expired tokens
- **Role Violations**: User role insufficient for requested action
- **Policy Violations**: Action violates security or business policies

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: authorizationErrors.length == 0
- **Success Status**: authorizationStatus equals "SUCCESS" or "GRANTED"
- **Low Severity**: errorSeverity in ["LOW", "INFO"]
- **No Permission Errors**: permissionErrors.length == 0
- **No Critical Categories**: errorCategories contains only non-critical types

**Failure Scenarios**:
- **Successful Authorization**: Authorization completed without errors
- **Minor Warnings**: Only non-critical warnings present
- **Recoverable Issues**: Errors that can be automatically resolved

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Authorizations**: Some permissions granted, others denied
- **Threshold Boundaries**: Error severity exactly at threshold
- **Complex Permissions**: Multi-level permissions with mixed results
- **Time-based Permissions**: Authorization valid only during specific periods

**Special Scenarios**:
- **System Maintenance**: Expected errors during maintenance windows
- **Emergency Access**: Override mechanisms for emergency situations
- **Delegation**: Authorization through delegated permissions

**Data Absence Handling**:
- Missing authorizationErrors treated as empty array (no errors)
- Missing authorizationStatus defaults to "UNKNOWN" (triggers error detection)
- Missing errorSeverity defaults to "UNKNOWN" (triggers error detection)

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 300 milliseconds (95th percentile)
- **Throughput**: 2000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Very low intensity for boolean logic evaluation
- **Memory**: 16MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Authorization Service**: Access to authorization results and status
- **Permission Service**: User permission and role information
- **Configuration Service**: Error thresholds and authorization rules

**External Dependencies**:
- **Identity Provider**: User authentication and role information
- **Audit Service**: Error history and pattern tracking

**Data Dependencies**:
- Error severity classification configuration
- Error category configuration
- Authorization permission rules

## 9. Configuration
**Configurable Thresholds**:
- `criticalErrorSeverities`: array - Error severities considered critical - Default: ["CRITICAL", "HIGH"]
- `ignoredErrorCategories`: array - Error categories to ignore - Default: []
- `permissionErrorThreshold`: integer - Maximum permission errors allowed - Default: 0

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict error detection mode - Default: true
- `warningsAsErrors`: boolean - Treat warnings as errors - Default: false
- `includePermissionErrors`: boolean - Include permission errors - Default: true

**Environment-Specific Settings**:
- Development: Relaxed error detection, more lenient permissions
- Production: Strict error detection, complete authorization

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required authorization data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid evaluation configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to true evaluation on data unavailability (assume errors present)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to cached results for minor system issues

**Error Propagation**:
- Evaluation errors logged with authorization context
- Failed evaluations trigger manual review workflow
- Critical errors escalated to security team
