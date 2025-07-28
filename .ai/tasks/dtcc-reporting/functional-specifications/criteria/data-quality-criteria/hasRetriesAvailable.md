# hasRetriesAvailable Criterion Specification

## 1. Component Overview
**Component Name**: hasRetriesAvailable
**Component Type**: CyodaCriterion
**Business Domain**: Error Handling
**Purpose**: Evaluates whether retry attempts are still available for failed operations and retry processing can be attempted
**Workflow Context**: Error handling and retry workflows across all business domains requiring retry availability assessment

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `entityId`: string - Unique entity identifier for retry tracking
- `attemptCount`: integer - Current number of processing attempts
- `maxRetries`: integer - Maximum allowed retry attempts
- `operationType`: string - Type of operation being retried

**Optional Fields**:
- `lastAttemptTime`: ISO-8601 timestamp - Time of last attempt
- `retryDelaySeconds`: integer - Required delay between retry attempts
- `criticalOperation`: boolean - Whether operation is critical requiring extended retries
- `manualRetryOverride`: boolean - Manual override to allow additional retries

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "retry-availability" - Tags for retry availability evaluation nodes
- `responseTimeoutMs`: 1500 - Maximum evaluation time (1.5 seconds)
- `context`: "retry-availability-check" - Evaluation context identifier

**Evaluation Context**:
- Retry policy configuration for different operation types
- Retry limit management and tracking
- Override mechanisms for critical operations

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF entityId == null OR entityId.isEmpty() THEN
    RETURN false
END IF

effectiveMaxRetries = maxRetries

IF criticalOperation == true THEN
    effectiveMaxRetries = maxRetries * 2
END IF

IF manualRetryOverride == true THEN
    effectiveMaxRetries = effectiveMaxRetries + 10
END IF

IF attemptCount < effectiveMaxRetries THEN
    RETURN true
ELSE
    RETURN false
END IF
```

**Boolean Logic**:
- Primary evaluation compares attempt count to maximum retries
- Secondary evaluation applies critical operation adjustments
- Tertiary evaluation considers manual override extensions
- Entity identifier validation for context
- Effective maximum retry calculation with adjustments

**Calculation Methods**:
- Effective maximum retry calculation with critical operation multiplier
- Manual override addition for exceptional cases
- Attempt count comparison against adjusted limits
- Retry availability determination based on remaining attempts

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Retries Available**: attemptCount < effective maximum retries
- **Valid Entity**: entityId is present and valid
- **Critical Extension**: Critical operation within extended limits
- **Override Extension**: Manual override providing additional retries

**Success Scenarios**:
- **Standard Retries**: Regular operation with retries available
- **Critical Retries**: Critical operation with extended retry limit
- **Override Retries**: Manual override providing additional attempts
- **Fresh Operation**: New operation with full retry allowance

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Retries Exhausted**: attemptCount >= effective maximum retries
- **Invalid Entity**: entityId is null or empty
- **Limit Reached**: Standard retry limit reached
- **Override Exhausted**: Manual override retries also exhausted

**Failure Scenarios**:
- **No Retries Left**: All retry attempts exhausted
- **Limit Exceeded**: Operation exceeded retry limits
- **Critical Limit**: Even critical operation limits exhausted
- **Override Limit**: Manual override attempts exhausted
- **Invalid Operation**: Operation identifier not valid

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Limit**: Attempt count exactly equals maximum retries
- **Zero Retries**: No retries allowed (maxRetries = 0)
- **Unlimited Retries**: Very high retry limits
- **Negative Attempts**: Invalid attempt count values

**Special Scenarios**:
- **Reset Attempts**: Attempt count reset during processing
- **Configuration Change**: Retry limits changed during processing
- **System Recovery**: Retry evaluation after system restart
- **Emergency Override**: Emergency manual override activation
- **Concurrent Attempts**: Multiple simultaneous retry evaluations

**Data Absence Handling**:
- Missing entityId defaults to false evaluation
- Missing attemptCount defaults to 0
- Missing maxRetries defaults to 3
- Missing operationType defaults to PROCESSING

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 50 milliseconds (95th percentile)
- **Throughput**: 10000 evaluations per second
- **Availability**: 99.9% uptime

**Resource Requirements**:
- **CPU**: Very low intensity for simple arithmetic operations
- **Memory**: 4MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Configuration Service**: Retry policy configuration and limits
- **Retry Tracking Service**: Attempt count tracking and management

**External Dependencies**:
- None - Self-contained evaluation logic

**Data Dependencies**:
- Retry policy configuration parameters
- Attempt count tracking data
- Operation type retry limits
- Override policy settings

## 9. Configuration
**Configurable Thresholds**:
- `defaultMaxRetries`: integer - Default maximum retry attempts - Default: 3
- `criticalOperationMultiplier`: integer - Multiplier for critical operations - Default: 2
- `maxOverrideRetries`: integer - Maximum manual override retries - Default: 10

**Evaluation Parameters**:
- `enableCriticalExtension`: boolean - Enable critical operation extensions - Default: true
- `enableManualOverride`: boolean - Enable manual override functionality - Default: true
- `strictLimitValidation`: boolean - Enable strict limit validation - Default: true

**Environment-Specific Settings**:
- Development: Higher retry limits for testing
- Production: Standard retry limits for operational stability

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required attempt count fields not accessible
- **INVALID_COUNT**: Attempt count validation errors
- **CONFIGURATION_ERROR**: Retry policy configuration failures
- **CALCULATION_ERROR**: Retry limit calculation failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on data errors (fail-safe approach)
- Use default maxRetries for missing configuration
- Skip adjustments on configuration errors
- Retry mechanism for configuration service failures (max 1 retry)

**Error Propagation**:
- Evaluation errors logged with entity context
- Failed evaluations trigger manual review
- Configuration errors escalated to operations team
- Calculation errors reported to development team
