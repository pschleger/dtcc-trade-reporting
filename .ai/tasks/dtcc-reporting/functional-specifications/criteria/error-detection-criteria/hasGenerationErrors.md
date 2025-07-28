# hasGenerationErrors Criterion Specification

## 1. Component Overview
**Component Name**: hasGenerationErrors
**Component Type**: CyodaCriterion
**Business Domain**: Error Detection
**Purpose**: Evaluates whether report generation processes encountered errors that require attention or intervention
**Workflow Context**: RegulatoryReportWorkflow and other workflows requiring generation error detection

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `generationStatus`: string - Current generation status
- `generationErrors`: array - List of generation errors encountered
- `generationTimestamp`: ISO-8601 timestamp - When generation completed
- `generationResults`: object - Generation results and metadata

**Optional Fields**:
- `generationWarnings`: array - Non-critical generation warnings
- `errorSeverity`: string - Highest severity level of generation errors
- `retryAttempts`: integer - Number of generation retry attempts made

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "error-detection" - Tags for error detection evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "generation-error-check" - Evaluation context identifier

**Evaluation Context**:
- Generation error severity classification
- Error type categorization
- Retry policy configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF generationErrors.length > 0 OR
   generationStatus == "FAILED" OR
   generationStatus == "ERROR" OR
   errorSeverity in ["CRITICAL", "HIGH"] OR
   generationResults == null OR
   generationResults.contentGenerated == false THEN
    RETURN true
ELSE IF generationStatus == "SUCCESS" AND
        generationErrors.length == 0 AND
        generationResults != null AND
        generationResults.contentGenerated == true THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on presence of generation errors
- Secondary evaluation considers generation status
- Tertiary evaluation assesses error severity levels
- Result validation for generation completeness

**Calculation Methods**:
- Error count aggregation
- Severity level assessment
- Status-based error detection
- Content generation validation

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Errors Present**: generationErrors array contains one or more errors
- **Failed Status**: generationStatus indicates failure or error state
- **High Severity**: errorSeverity indicates critical or high-priority errors
- **Missing Content**: generationResults is null or indicates no content generated

**Success Scenarios**:
- **Generation Errors**: Explicit errors recorded during generation
- **Status Failures**: Generation status indicates failure condition
- **Critical Errors**: High-severity errors requiring immediate attention
- **Content Failures**: Generation failed to produce expected content

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Errors**: generationErrors array is empty
- **Success Status**: generationStatus indicates successful completion
- **Low Severity**: Only low-severity warnings present
- **Content Generated**: generationResults indicates successful content generation

**Failure Scenarios**:
- **Successful Generation**: Generation completed without errors
- **Warning Only**: Only non-critical warnings present
- **Clean State**: No error indicators present in generation

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Error Array**: generationErrors array exists but is empty
- **Unknown Status**: generationStatus not in recognized values
- **Mixed Severity**: Combination of different error severity levels
- **Partial Generation**: Some content generated, other parts failed

**Special Scenarios**:
- **Retry Scenarios**: Errors from previous attempts vs current attempt
- **Warning Escalation**: Warnings that should be treated as errors
- **Transient Errors**: Temporary errors that may resolve automatically

**Data Absence Handling**:
- Missing generationErrors defaults to false (no errors)
- Missing generationStatus defaults to false (assume success)
- Missing generationResults may indicate error condition

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
- **Generation Service**: Access to generation results and error information
- **Error Classification Service**: Error severity and type classification
- **Configuration Service**: Error detection thresholds and rules

**External Dependencies**:
- **Monitoring Service**: Generation error tracking and alerting integration

**Data Dependencies**:
- Generation error severity classification rules
- Error type categorization
- Generation status definitions

## 9. Configuration
**Configurable Thresholds**:
- `criticalSeverityLevels`: array - Severity levels considered critical - Default: ["CRITICAL", "HIGH"]
- `errorStatusValues`: array - Status values indicating errors - Default: ["FAILED", "ERROR"]
- `includeWarnings`: boolean - Include warnings in error detection - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict error detection - Default: true
- `ignoreTransientErrors`: boolean - Ignore transient error types - Default: false
- `requireContent`: boolean - Require content generation for success - Default: true

**Environment-Specific Settings**:
- Development: Relaxed error detection, include warnings
- Production: Strict error detection, critical errors only

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required generation data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid error detection configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (assume no errors)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic error detection for configuration issues

**Error Propagation**:
- Evaluation errors logged with generation context
- Failed evaluations trigger manual review
- Critical evaluation errors escalated to generation team
