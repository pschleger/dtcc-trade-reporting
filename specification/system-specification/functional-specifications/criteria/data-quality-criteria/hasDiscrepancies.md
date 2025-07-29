# hasDiscrepancies Criterion Specification

## 1. Component Overview
**Component Name**: hasDiscrepancies
**Component Type**: CyodaCriterion
**Business Domain**: Data Quality Management
**Purpose**: Evaluates whether discrepancies have been identified in data comparison, reconciliation, or validation processes requiring investigation and resolution
**Workflow Context**: Reconciliation processing and data quality workflows requiring discrepancy detection and management

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `entityId`: string - Unique entity identifier being evaluated
- `comparisonResult`: string - Result of data comparison (MATCH, DISCREPANCY, ERROR)
- `discrepancyCount`: integer - Number of discrepancies identified
- `comparisonTimestamp`: ISO-8601 timestamp - When comparison was performed

**Optional Fields**:
- `discrepancyTypes`: array - Types of discrepancies found (AMOUNT, DATE, COUNTERPARTY)
- `discrepancySeverity`: string - Severity level (LOW, MEDIUM, HIGH, CRITICAL)
- `toleranceThreshold`: decimal - Acceptable tolerance threshold for differences
- `autoResolvable`: boolean - Whether discrepancies can be automatically resolved

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "discrepancy-detection" - Tags for discrepancy evaluation nodes
- `responseTimeoutMs`: 2500 - Maximum evaluation time (2.5 seconds)
- `context`: "discrepancy-check" - Evaluation context identifier

**Evaluation Context**:
- Data comparison and reconciliation results
- Discrepancy classification and severity assessment
- Tolerance threshold management for acceptable differences

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF entityId == null OR entityId.isEmpty() THEN
    RETURN false
END IF

IF comparisonResult == "DISCREPANCY" THEN
    RETURN true
END IF

IF discrepancyCount != null AND discrepancyCount > 0 THEN
    RETURN true
END IF

IF discrepancyTypes != null AND discrepancyTypes.size() > 0 THEN
    RETURN true
END IF

IF discrepancySeverity != null AND 
   (discrepancySeverity == "MEDIUM" OR 
    discrepancySeverity == "HIGH" OR 
    discrepancySeverity == "CRITICAL") THEN
    RETURN true
END IF

RETURN false
```

**Boolean Logic**:
- Primary evaluation checks comparison result for discrepancy status
- Secondary evaluation examines discrepancy count for positive values
- Tertiary evaluation considers discrepancy types list presence
- Quaternary evaluation checks severity level for significant discrepancies
- Entity identifier validation for context

**Calculation Methods**:
- Comparison result enumeration validation
- Discrepancy count validation and threshold checking
- Discrepancy type list processing and validation
- Severity level classification and assessment

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Discrepancy Result**: comparisonResult equals "DISCREPANCY"
- **Positive Count**: discrepancyCount > 0
- **Types Present**: discrepancyTypes list contains items
- **Significant Severity**: discrepancySeverity indicates medium or higher severity
- **Valid Entity**: entityId is present and valid

**Success Scenarios**:
- **Standard Discrepancy**: Comparison identified data differences
- **Multiple Discrepancies**: Multiple types of discrepancies found
- **Severe Discrepancy**: High or critical severity discrepancies
- **Tolerance Exceeded**: Differences exceed acceptable tolerance
- **Complex Discrepancy**: Multiple discrepancy types requiring investigation

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Match Result**: comparisonResult equals "MATCH"
- **Zero Count**: discrepancyCount equals 0 or null
- **Empty Types**: discrepancyTypes list is empty or null
- **Low Severity**: discrepancySeverity equals "LOW" or null
- **Invalid Entity**: entityId is null or empty

**Failure Scenarios**:
- **Perfect Match**: No discrepancies found in comparison
- **Within Tolerance**: Differences within acceptable tolerance
- **Clean Data**: Data quality validation passed
- **Auto-Resolved**: Discrepancies automatically resolved
- **No Comparison**: Comparison not performed or failed

## 6. Edge Cases
**Boundary Conditions**:
- **Tolerance Boundary**: Differences exactly at tolerance threshold
- **Zero Discrepancies**: Explicit zero count vs null count
- **Empty Lists**: Discrepancy types list exists but is empty
- **Severity Boundary**: Discrepancies at severity threshold

**Special Scenarios**:
- **Partial Discrepancies**: Some fields match, others don't
- **Temporal Discrepancies**: Time-based differences in data
- **Rounding Discrepancies**: Differences due to rounding or precision
- **System Discrepancies**: Differences due to system processing
- **Data Migration**: Discrepancies during data migration

**Data Absence Handling**:
- Missing entityId defaults to false evaluation
- Missing comparisonResult defaults to "ERROR"
- Missing discrepancyCount defaults to null (not counted as discrepancy)
- Missing discrepancyTypes defaults to empty list

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 200 milliseconds (95th percentile)
- **Throughput**: 4000 evaluations per second
- **Availability**: 99.9% uptime

**Resource Requirements**:
- **CPU**: Low intensity for list processing and enumeration validation
- **Memory**: 10MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Comparison Service**: Data comparison and reconciliation results
- **Data Quality Service**: Data quality validation and assessment
- **Configuration Service**: Tolerance threshold and severity configuration

**External Dependencies**:
- **Reference Data Service**: Master data for comparison validation
- **Audit Service**: Discrepancy tracking and audit trail

**Data Dependencies**:
- Comparison result data
- Discrepancy classification data
- Tolerance threshold configuration
- Severity level definitions

## 9. Configuration
**Configurable Thresholds**:
- `defaultToleranceThreshold`: decimal - Default tolerance for differences - Default: 0.01
- `severityThresholds`: map - Thresholds for severity classification
- `autoResolutionEnabled`: boolean - Enable automatic discrepancy resolution - Default: true

**Evaluation Parameters**:
- `strictComparisonValidation`: boolean - Enable strict comparison validation - Default: true
- `includeLowSeverity`: boolean - Include low severity discrepancies - Default: false
- `requireDiscrepancyTypes`: boolean - Require discrepancy type classification - Default: false

**Environment-Specific Settings**:
- Development: Relaxed tolerance thresholds for testing
- Production: Strict tolerance thresholds for data quality

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required comparison fields not accessible
- **INVALID_RESULT**: Comparison result validation errors
- **COUNT_ERROR**: Discrepancy count validation failures
- **SEVERITY_ERROR**: Severity classification failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to true evaluation on comparison errors (conservative approach)
- Fallback to count-based evaluation on result errors
- Skip severity check on classification errors
- Retry mechanism for service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with entity context
- Failed evaluations trigger manual review
- Comparison errors escalated to data quality team
- Service errors reported to operations team
