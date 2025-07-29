# Component Specification: hasValidationErrors

### 1. Component Overview
**Component Name**: hasValidationErrors
**Component Type**: CyodaCriterion
**Business Domain**: Validation Criteria
**Purpose**: Evaluates whether validation processes have encountered errors that require attention or remediation
**Workflow Context**: Used across all workflows to determine if validation errors exist and trigger appropriate error handling processes

### 2. Input Parameters
**Entity Type**: ValidationResult
**Required Fields**:
- `validationId`: string - Unique identifier for the validation process
- `validationStatus`: string - Overall status of validation (SUCCESS, FAILED, PARTIAL)
- `validationErrors`: array - List of validation errors encountered
- `validationTimestamp`: string (ISO-8601) - When validation was performed
- `entityType`: string - Type of entity that was validated

**Optional Fields**:
- `errorSeverity`: array - Severity levels of validation errors (CRITICAL, HIGH, MEDIUM, LOW)
- `errorCategories`: array - Categories of validation errors (SCHEMA, BUSINESS_RULE, DATA_QUALITY)
- `validationRules`: array - Specific validation rules that failed
- `errorCount`: integer - Total number of validation errors

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to evaluation context
- `calculationNodesTags`: string - Tags for distributed evaluation nodes
- `responseTimeoutMs`: integer - Maximum evaluation time in milliseconds (default: 1000)
- `context`: string - Evaluation context identifier

**Evaluation Context**:
- Validation error severity thresholds and classification rules
- Business impact assessment criteria for different error types
- Error handling and escalation requirements

### 3. Evaluation Logic
**Decision Algorithm**:
```
IF validationErrors.length > 0 THEN
    IF any error has severity = CRITICAL THEN
        RETURN true (critical errors present)
    ELSE IF errorCount > errorThreshold THEN
        RETURN true (error count exceeds threshold)
    ELSE IF validationStatus = FAILED THEN
        RETURN true (validation failed)
    ELSE
        RETURN false (errors present but within tolerance)
ELSE
    RETURN false (no validation errors)
```

**Boolean Logic**:
- Evaluates presence of validation errors in the validation result
- Considers error severity levels and counts in evaluation decision
- Applies configurable thresholds for error tolerance

**Calculation Methods**:
- Error count calculation from validation error array
- Severity level assessment based on error classification
- Error threshold comparison for tolerance evaluation

### 4. Success Conditions
**Positive Evaluation Criteria**:
- **Validation Errors Present**: ValidationErrors array contains one or more error entries
- **Critical Errors Detected**: Any validation error has severity level of CRITICAL
- **Error Threshold Exceeded**: Total error count exceeds configured threshold
- **Validation Status Failed**: Overall validation status indicates failure

**Success Scenarios**:
- **Schema Validation Errors**: Entity fails schema validation with structural errors
- **Business Rule Violations**: Entity violates business rules during validation
- **Data Quality Issues**: Entity contains data quality problems requiring attention
- **Critical Error Detection**: Validation detects critical errors requiring immediate action

### 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Validation Errors**: ValidationErrors array is empty or null
- **Validation Success**: Overall validation status indicates success
- **Errors Within Tolerance**: Error count is within acceptable threshold limits
- **No Critical Errors**: No validation errors have critical severity level

**Failure Scenarios**:
- **Clean Validation**: Entity passes all validation checks without errors
- **Minor Warnings Only**: Validation produces only minor warnings within tolerance
- **Successful Remediation**: Previous validation errors have been successfully resolved
- **Empty Validation Result**: No validation process has been performed

### 6. Edge Cases
**Boundary Conditions**:
- **Null Validation Result**: Handle cases where validation result is null or undefined
- **Empty Error Array**: Distinguish between empty array and missing error information
- **Zero Error Threshold**: Handle configuration where no errors are tolerated
- **Unknown Error Severity**: Handle validation errors without severity classification

**Special Scenarios**:
- **Partial Validation**: Handle scenarios where validation is incomplete but has errors
- **Validation Timeout**: Handle cases where validation process timed out with partial results
- **System Validation Errors**: Handle technical validation errors vs business validation errors

**Data Absence Handling**:
- Return false when validation result data is completely unavailable
- Log warning when required validation fields are missing
- Apply conservative evaluation when error severity information is absent

### 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 100 milliseconds (95th percentile)
- **Throughput**: 10000 evaluations per second
- **Availability**: 99.99% uptime

**Resource Requirements**:
- **CPU**: Minimal processing for error array analysis and counting
- **Memory**: 64MB for validation result processing and error analysis

### 8. Dependencies
**Internal Dependencies**:
- **ValidationService**: For accessing validation results and error information
- **ErrorClassificationService**: For error severity and category determination
- **ConfigurationService**: For error threshold and tolerance configuration

**External Dependencies**:
- **Validation Database**: For validation result persistence and retrieval (SLA: 99.9% availability)

**Data Dependencies**:
- Validation error classification and severity mapping
- Error threshold configuration and tolerance rules
- Validation rule definitions and business requirements

### 9. Configuration
**Configurable Thresholds**:
- `errorCountThreshold`: integer - Maximum acceptable error count - Default: 0
- `criticalErrorTolerance`: boolean - Whether to tolerate critical errors - Default: false
- `severityLevels`: array - Recognized error severity levels - Default: ["CRITICAL", "HIGH", "MEDIUM", "LOW"]

**Evaluation Parameters**:
- `includeWarnings`: boolean - Whether to include warnings as errors - Default: false
- `errorCategoryFilter`: array - Error categories to consider in evaluation - Default: ["all"]
- `validationTimeoutMs`: integer - Timeout for validation result retrieval - Default: 1000

**Environment-Specific Settings**:
- **Development**: Higher error tolerance for testing and development
- **Production**: Zero tolerance for critical errors with strict thresholds

### 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Validation result data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout period
- **CONFIGURATION_ERROR**: Invalid threshold or configuration parameters
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Return false (no errors detected) when validation data is unavailable
- Apply default thresholds when configuration is invalid
- Log evaluation errors for monitoring and troubleshooting

**Error Propagation**:
- Evaluation errors are logged with full context and validation details
- Critical evaluation failures trigger monitoring alerts
- Failed evaluations are tracked for system reliability assessment
