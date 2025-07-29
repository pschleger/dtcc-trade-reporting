# isMaxRetriesExceeded Criterion Specification

## 1. Component Overview
**Component Name**: isMaxRetriesExceeded
**Component Type**: CyodaCriterion
**Business Domain**: Error Handling
**Purpose**: Evaluates whether the maximum number of retry attempts has been exceeded and alternative processing or escalation should commence
**Workflow Context**: Error handling workflows across all business domains requiring retry limit enforcement and escalation procedures

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `attemptCount`: integer - Current number of processing attempts
- `maxRetries`: integer - Maximum allowed retry attempts
- `operationType`: string - Type of operation being retried (SUBMISSION, VALIDATION, PROCESSING)
- `lastAttemptTime`: ISO-8601 timestamp - Time of last attempt

**Optional Fields**:
- `criticalOperation`: boolean - Whether operation is critical requiring extended retries
- `escalationLevel`: string - Current escalation level (NONE, WARNING, CRITICAL)
- `manualRetryOverride`: boolean - Manual override to allow additional retries
- `operationPriority`: string - Priority level (LOW, MEDIUM, HIGH, CRITICAL)

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "retry-limits" - Tags for retry limit evaluation nodes
- `responseTimeoutMs`: 1500 - Maximum evaluation time (1.5 seconds)
- `context`: "retry-limit-check" - Evaluation context identifier

**Evaluation Context**:
- Retry policy configuration for different operation types
- Escalation procedures for exceeded retry limits
- Override mechanisms for critical operations

## 3. Evaluation Logic
**Decision Algorithm**:
```
effectiveMaxRetries = maxRetries

IF criticalOperation == true THEN
    effectiveMaxRetries = maxRetries * 2
END IF

IF operationPriority == "CRITICAL" THEN
    effectiveMaxRetries = effectiveMaxRetries + 5
ELSE IF operationPriority == "HIGH" THEN
    effectiveMaxRetries = effectiveMaxRetries + 2
END IF

IF manualRetryOverride == true THEN
    effectiveMaxRetries = effectiveMaxRetries + 10
END IF

IF attemptCount >= effectiveMaxRetries THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation compares attempt count to maximum retries
- Secondary evaluation applies critical operation adjustments
- Tertiary evaluation considers operation priority levels
- Override mechanism for manual retry extension
- Escalation consideration for retry limit management

**Calculation Methods**:
- Effective maximum retry calculation with adjustments
- Critical operation multiplier application
- Priority-based retry extension calculation
- Manual override addition for exceptional cases

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Limit Exceeded**: Attempt count >= effective maximum retries
- **Standard Operation**: Non-critical operation exceeding standard limits
- **Critical Operation**: Critical operation exceeding extended limits
- **Override Exhausted**: Manual override retries also exceeded

**Success Scenarios**:
- **Standard Limit**: Regular operation reaching retry limit
- **Critical Limit**: Critical operation reaching extended limit
- **Priority Limit**: High priority operation reaching adjusted limit
- **Override Limit**: Manual override retries exhausted

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Within Limit**: Attempt count < effective maximum retries
- **Critical Extension**: Critical operation within extended limits
- **Priority Extension**: High priority operation within adjusted limits
- **Override Available**: Manual override retries still available

**Failure Scenarios**:
- **Retries Available**: Standard retries still available
- **Critical Buffer**: Critical operation buffer not exhausted
- **Priority Buffer**: Priority adjustment buffer available
- **Override Buffer**: Manual override buffer available

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Limit**: Attempt count exactly equals maximum retries
- **Zero Retries**: No retries allowed (maxRetries = 0)
- **Unlimited Retries**: Very high retry limits (maxRetries = -1)
- **Negative Attempts**: Invalid attempt count values

**Special Scenarios**:
- **Concurrent Attempts**: Multiple simultaneous retry evaluations
- **Reset Attempts**: Attempt count reset during processing
- **Configuration Change**: Retry limits changed during processing
- **System Recovery**: Retry evaluation after system restart
- **Emergency Override**: Emergency manual override activation

**Data Absence Handling**:
- Missing attemptCount defaults to 0
- Missing maxRetries defaults to 3
- Missing operationType defaults to PROCESSING
- Missing lastAttemptTime uses current time

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 50 milliseconds (95th percentile)
- **Throughput**: 10000 evaluations per second
- **Availability**: 99.9% uptime

**Resource Requirements**:
- **CPU**: Very low intensity for simple comparisons
- **Memory**: 4MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Configuration Service**: Retry policy configuration and limits
- **Escalation Service**: Escalation procedures for exceeded limits

**External Dependencies**:
- None - Self-contained evaluation logic

**Data Dependencies**:
- Retry policy configuration parameters
- Operation type retry limits
- Priority level adjustments
- Override policy settings

## 9. Configuration
**Configurable Thresholds**:
- `defaultMaxRetries`: integer - Default maximum retry attempts - Default: 3
- `criticalOperationMultiplier`: integer - Multiplier for critical operations - Default: 2
- `priorityRetryBonus`: map - Additional retries by priority level - Default: {HIGH: 2, CRITICAL: 5}

**Evaluation Parameters**:
- `enableCriticalExtension`: boolean - Enable critical operation extensions - Default: true
- `enablePriorityAdjustment`: boolean - Enable priority-based adjustments - Default: true
- `maxOverrideRetries`: integer - Maximum manual override retries - Default: 10

**Environment-Specific Settings**:
- Development: Higher retry limits for testing
- Production: Standard retry limits for operational stability

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required attempt count fields not accessible
- **INVALID_COUNT**: Attempt count validation errors
- **CONFIGURATION_ERROR**: Retry policy configuration failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to true evaluation on data errors (fail-safe approach)
- Use default maxRetries for missing configuration
- Skip adjustments on configuration errors
- Retry mechanism for configuration service failures (max 1 retry)

**Error Propagation**:
- Evaluation errors logged with retry context
- Failed evaluations trigger manual review
- Configuration errors escalated to operations team
- Exceeded limits trigger escalation procedures
