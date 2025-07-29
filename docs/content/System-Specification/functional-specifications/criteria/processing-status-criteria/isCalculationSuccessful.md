# isCalculationSuccessful Criterion Specification

## 1. Component Overview
**Component Name**: isCalculationSuccessful
**Component Type**: CyodaCriterion
**Business Domain**: Processing Status Evaluation
**Purpose**: Evaluates whether position calculation process completed successfully with acceptable results
**Workflow Context**: PositionWorkflow (calculating state transitions)

## 2. Input Parameters
**Entity Type**: Position
**Required Fields**:
- `calculationStatus`: string - Current calculation status
- `calculationResults`: object - Detailed calculation results
- `calculationTimestamp`: ISO-8601 timestamp - When calculation completed
- `dataQualityScore`: decimal - Overall data quality assessment (0.0-1.0)
- `validationResults`: object - Post-calculation validation results

**Optional Fields**:
- `calculationErrors`: array - List of calculation errors if any
- `calculationWarnings`: array - Non-critical calculation warnings
- `performanceMetrics`: object - Calculation performance data

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "calculation" - Tags for calculation evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time (5 seconds)
- `context`: "calculation-success-check" - Evaluation context identifier

**Evaluation Context**:
- Data quality thresholds
- Calculation validation rules
- Performance benchmarks

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF calculationStatus == "SUCCESS" AND
   calculationResults != null AND
   dataQualityScore >= threshold AND
   validationResults.allChecksPass == true AND
   calculationTimestamp within acceptable range THEN
    RETURN true
ELSE IF calculationStatus == "FAILED" OR
        calculationErrors.length > 0 OR
        dataQualityScore < threshold THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on calculation status
- Secondary evaluation considers data quality score
- Tertiary evaluation validates calculation results
- Temporal validation ensures recent calculation

**Calculation Methods**:
- Data quality score threshold comparison
- Validation result aggregation
- Temporal freshness calculation

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Calculation Status**: calculationStatus equals "SUCCESS"
- **Results Present**: calculationResults contains required metrics
- **Data Quality**: dataQualityScore >= 0.95 (configurable threshold)
- **Validation Passed**: validationResults.allChecksPass is true
- **Temporal Validity**: calculationTimestamp within last 4 hours

**Success Scenarios**:
- **Standard Success**: All calculation checks pass with high data quality
- **Warning Success**: Calculation passes with non-critical warnings
- **Threshold Success**: Calculation passes at minimum acceptable quality level

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Calculation Failure**: calculationStatus equals "FAILED" or "ERROR"
- **Missing Results**: calculationResults is null or incomplete
- **Data Quality Issues**: dataQualityScore < 0.95 without approved exceptions
- **Validation Failure**: validationResults.allChecksPass is false
- **Stale Calculation**: calculationTimestamp older than 4 hours

**Failure Scenarios**:
- **Calculation Error**: Mathematical or system errors during calculation
- **Data Quality Failure**: Input data quality below acceptable threshold
- **Validation Failure**: Post-calculation validation checks failed
- **Timeout Failure**: Calculation process timed out before completion

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Calculations**: Some metrics calculated, others missing
- **Threshold Boundaries**: Data quality score exactly at threshold
- **Time Boundaries**: Calculation timestamp at edge of acceptable range
- **Concurrent Updates**: Entity modified during evaluation

**Special Scenarios**:
- **Emergency Processing**: Manual override for critical business needs
- **System Maintenance**: Degraded calculation during maintenance windows
- **Market Closure**: Different validation rules during market closure

**Data Absence Handling**:
- Missing calculationStatus defaults to false evaluation
- Missing calculationResults treated as calculation failure
- Missing dataQualityScore uses conservative threshold

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 2 seconds (95th percentile)
- **Throughput**: 500 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for boolean logic evaluation
- **Memory**: 64MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Calculation Service**: Access to calculation results and status
- **Validation Service**: Post-calculation validation results
- **Configuration Service**: Data quality thresholds and validation rules

**External Dependencies**:
- **Audit Service**: Calculation history and performance tracking

**Data Dependencies**:
- Calculation result validation rules
- Data quality threshold configuration
- Performance benchmark settings

## 9. Configuration
**Configurable Thresholds**:
- `dataQualityThreshold`: decimal - Minimum data quality score - Default: 0.95
- `calculationTimeoutHours`: integer - Maximum calculation age - Default: 4
- `allowPartialResults`: boolean - Accept partial calculation results - Default: false

**Evaluation Parameters**:
- `strictMode`: boolean - Enable strict evaluation mode - Default: true
- `warningsAsErrors`: boolean - Treat warnings as errors - Default: false
- `performanceValidation`: boolean - Include performance validation - Default: true

**Environment-Specific Settings**:
- Development: Relaxed thresholds, extended timeout
- Production: Strict thresholds, standard timeout

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required calculation data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **CONFIGURATION_ERROR**: Invalid evaluation configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false evaluation on data unavailability
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to cached results for minor system issues

**Error Propagation**:
- Evaluation errors logged with calculation context
- Failed evaluations trigger manual review workflow
- Critical errors escalated to risk management team
