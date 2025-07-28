# hasRetriesAvailable Criterion Specification

## 1. Component Overview
**Component Name**: hasRetriesAvailable
**Component Type**: CyodaCriterion
**Business Domain**: Data Quality Assessment
**Purpose**: Evaluates whether retry attempts still available
**Workflow Context**: Multiple workflows requiring hasRetriesAvailable evaluation

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `entityId`: string - Unique entity identifier
- `status`: string - Current entity status
- `timestamp`: ISO-8601 timestamp - Entity timestamp
- `data`: object - Entity data for evaluation

**Optional Fields**:
- `metadata`: object - Additional evaluation context
- `configuration`: object - Evaluation configuration overrides

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "evaluation" - Tags for evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time
- `context`: string - Evaluation context identifier

**Evaluation Context**:
- Business rule configuration
- Evaluation thresholds and parameters
- Historical data for comparison

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF primary_condition AND secondary_condition THEN
    RETURN true
ELSE IF exception_condition THEN
    RETURN true (with override)
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on entity state and data
- Secondary evaluation considers business rules
- Exception handling for special cases
- Default behavior for edge cases

**Calculation Methods**:
- Standard comparison operations
- Threshold-based evaluations
- Time-based calculations (if applicable)

## 4. Success Conditions
**Positive Evaluation Criteria**:
- Primary condition: Entity meets main evaluation criteria
- Data quality: Required data present and valid
- Business rules: All applicable business rules satisfied
- Temporal validity: Evaluation performed within valid timeframe

**Success Scenarios**:
- Standard success: All conditions met normally
- Override success: Conditions met with approved exceptions
- Conditional success: Conditions met with warnings

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- Primary condition: Entity fails main evaluation criteria
- Data issues: Required data missing or invalid
- Business rule violations: Applicable business rules not satisfied
- System issues: Evaluation cannot be completed

**Failure Scenarios**:
- Standard failure: Conditions not met
- Data failure: Insufficient or invalid data
- System failure: Technical evaluation failure

## 6. Edge Cases
**Boundary Conditions**:
- Threshold boundary values
- Time boundary conditions
- Data availability edge cases
- Concurrent evaluation scenarios

**Special Scenarios**:
- Emergency override conditions
- Maintenance mode behavior
- Degraded system performance

**Data Absence Handling**:
- Missing required data defaults to false
- Optional data absence handled gracefully
- Partial data scenarios evaluated appropriately

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 2 seconds (95th percentile)
- **Throughput**: 1000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for evaluation logic
- **Memory**: 64MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- Configuration Service
- Master Data Service (if applicable)
- Audit Service

**External Dependencies**:
- External data sources (if applicable)

**Data Dependencies**:
- Evaluation rule configuration
- Threshold and parameter settings
- Historical data (if applicable)

## 9. Configuration
**Configurable Thresholds**:
- `evaluationThreshold`: decimal - Primary evaluation threshold
- `timeoutSeconds`: integer - Evaluation timeout
- `strictMode`: boolean - Enable strict evaluation

**Evaluation Parameters**:
- `cacheResults`: boolean - Cache evaluation results
- `auditEvaluations`: boolean - Audit all evaluations
- `allowOverrides`: boolean - Allow manual overrides

**Environment-Specific Settings**:
- Development: Relaxed thresholds, extended timeouts
- Production: Standard thresholds, normal timeouts

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout
- **CONFIGURATION_ERROR**: Invalid evaluation configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability
- Retry mechanism for transient errors
- Fallback to cached results when appropriate

**Error Propagation**:
- Evaluation errors logged with context
- Failed evaluations trigger manual review
- Critical errors escalated appropriately
