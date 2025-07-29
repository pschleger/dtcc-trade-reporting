# isRetryDelayElapsed Criterion Specification

## 1. Component Overview
**Component Name**: isRetryDelayElapsed
**Component Type**: CyodaCriterion
**Business Domain**: Error Handling
**Purpose**: Evaluates whether the required delay period between retry attempts has elapsed and retry processing can commence
**Workflow Context**: Error handling and retry workflows for failed processing operations across all business domains

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `lastAttemptTime`: ISO-8601 timestamp - Time of last processing attempt
- `currentTime`: ISO-8601 timestamp - Current system time for evaluation
- `retryDelaySeconds`: integer - Required delay in seconds between retry attempts
- `attemptCount`: integer - Number of previous attempts

**Optional Fields**:
- `exponentialBackoff`: boolean - Whether to use exponential backoff strategy
- `backoffMultiplier`: double - Multiplier for exponential backoff calculation
- `maxDelaySeconds`: integer - Maximum delay cap for exponential backoff
- `jitterEnabled`: boolean - Whether to add random jitter to delay

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "retry-timing" - Tags for retry timing evaluation nodes
- `responseTimeoutMs`: 2000 - Maximum evaluation time (2 seconds)
- `context`: "retry-delay-check" - Evaluation context identifier

**Evaluation Context**:
- Retry strategy configuration for different operation types
- Backoff algorithm parameters for progressive delay
- Jitter configuration for distributed retry timing

## 3. Evaluation Logic
**Decision Algorithm**:
```
timeSinceLastAttempt = currentTime - lastAttemptTime

IF exponentialBackoff == true THEN
    calculatedDelay = retryDelaySeconds * (backoffMultiplier ^ (attemptCount - 1))
    IF calculatedDelay > maxDelaySeconds THEN
        calculatedDelay = maxDelaySeconds
    END IF
ELSE
    calculatedDelay = retryDelaySeconds
END IF

IF jitterEnabled == true THEN
    jitterAmount = random(0, calculatedDelay * 0.1)
    calculatedDelay = calculatedDelay + jitterAmount
END IF

IF timeSinceLastAttempt >= calculatedDelay THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on elapsed time since last attempt
- Secondary calculation applies exponential backoff if enabled
- Tertiary calculation adds jitter for distributed retry timing
- Delay calculation considers attempt count for progressive backoff

**Calculation Methods**:
- Time difference calculation between attempts
- Exponential backoff calculation with configurable multiplier
- Maximum delay cap enforcement for bounded retry delays
- Random jitter calculation for retry distribution

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Delay Elapsed**: Time since last attempt >= required delay
- **Backoff Complete**: Exponential backoff delay period completed
- **Jitter Applied**: Random jitter delay period completed
- **Valid Timing**: All timing calculations successful

**Success Scenarios**:
- **Fixed Delay**: Standard retry delay period elapsed
- **Exponential Backoff**: Progressive delay period completed
- **Jittered Retry**: Randomized delay period elapsed
- **Maximum Delay**: Capped delay period reached and elapsed

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Delay Pending**: Time since last attempt < required delay
- **Backoff Active**: Exponential backoff delay still in progress
- **Jitter Pending**: Random jitter delay still active
- **Invalid Timing**: Timing calculation errors

**Failure Scenarios**:
- **Too Soon**: Retry attempted before delay period
- **Backoff Wait**: Exponential delay still in progress
- **Jitter Wait**: Random delay still active
- **Timing Error**: Invalid time calculations

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Delay**: Time elapsed exactly equals required delay
- **Zero Delay**: Immediate retry with no delay requirement
- **Maximum Attempts**: Retry evaluation at maximum attempt limit
- **Time Precision**: Millisecond-level timing precision

**Special Scenarios**:
- **Clock Adjustment**: System clock changes during delay period
- **Negative Time**: Last attempt time in future due to clock skew
- **Overflow Protection**: Very large delay calculations
- **Concurrent Retries**: Multiple retry evaluations for same entity

**Data Absence Handling**:
- Missing lastAttemptTime defaults to false evaluation
- Missing currentTime uses system time
- Missing retryDelaySeconds defaults to 60 seconds
- Missing attemptCount defaults to 1

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 100 milliseconds (95th percentile)
- **Throughput**: 5000 evaluations per second
- **Availability**: 99.9% uptime

**Resource Requirements**:
- **CPU**: Very low intensity for time calculations
- **Memory**: 8MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Time Service**: System time management
- **Configuration Service**: Retry strategy configuration
- **Random Service**: Jitter calculation for distributed retries

**External Dependencies**:
- **NTP Service**: Network time synchronization for accurate timing

**Data Dependencies**:
- Retry strategy configuration parameters
- Backoff algorithm settings
- Jitter configuration settings

## 9. Configuration
**Configurable Thresholds**:
- `defaultRetryDelaySeconds`: integer - Default retry delay - Default: 60
- `defaultBackoffMultiplier`: double - Default exponential backoff multiplier - Default: 2.0
- `defaultMaxDelaySeconds`: integer - Default maximum delay cap - Default: 3600

**Evaluation Parameters**:
- `enableExponentialBackoff`: boolean - Enable exponential backoff by default - Default: true
- `enableJitter`: boolean - Enable jitter by default - Default: true
- `jitterPercentage`: double - Jitter percentage of delay - Default: 0.1

**Environment-Specific Settings**:
- Development: Shorter delays for faster testing
- Production: Standard retry delays for operational stability

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required timing fields not accessible
- **INVALID_TIME**: Time format or value validation errors
- **CALCULATION_ERROR**: Delay calculation failures
- **OVERFLOW_ERROR**: Delay calculation overflow
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on timing errors
- Fallback to system time for missing currentTime
- Use default delay for invalid retryDelaySeconds
- Skip jitter calculation on random service errors
- Retry mechanism for time service failures (max 1 retry)

**Error Propagation**:
- Evaluation errors logged with retry context
- Failed evaluations trigger manual review
- Timing errors escalated to operations team
- Calculation errors reported to development team
