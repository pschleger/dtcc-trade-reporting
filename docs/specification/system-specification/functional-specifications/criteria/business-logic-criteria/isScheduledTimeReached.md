# isScheduledTimeReached Criterion Specification

## 1. Component Overview
**Component Name**: isScheduledTimeReached
**Component Type**: CyodaCriterion
**Business Domain**: Workflow Control
**Purpose**: Evaluates whether scheduled processing time has been reached and processing should commence
**Workflow Context**: Batch processing and regulatory reporting workflows requiring time-based processing triggers

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `scheduledTime`: ISO-8601 timestamp - Scheduled processing time
- `currentTime`: ISO-8601 timestamp - Current system time for evaluation
- `timeZone`: string - Time zone for scheduled processing
- `processingStatus`: string - Current processing status

**Optional Fields**:
- `gracePeriodMinutes`: integer - Grace period after scheduled time
- `earlyStartMinutes`: integer - Minutes before scheduled time to allow early start
- `holidayCalendar`: string - Holiday calendar for business day calculations
- `scheduleOverride`: boolean - Manual override for scheduled processing

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "scheduling" - Tags for scheduling evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time (5 seconds)
- `context`: "schedule-check" - Evaluation context identifier

**Evaluation Context**:
- Business calendar for date calculations
- Time zone configuration for accurate time comparison
- Schedule override mechanism for manual control

## 3. Evaluation Logic
**Decision Algorithm**:
```
adjustedCurrentTime = convertToTimeZone(currentTime, timeZone)
adjustedScheduledTime = convertToTimeZone(scheduledTime, timeZone)
earlyStartTime = adjustedScheduledTime - earlyStartMinutes
gracePeriodEndTime = adjustedScheduledTime + gracePeriodMinutes

IF scheduleOverride == true THEN
    RETURN true
ELSE IF adjustedCurrentTime >= earlyStartTime AND
        adjustedCurrentTime <= gracePeriodEndTime AND
        processingStatus != "COMPLETED" THEN
    RETURN true
ELSE IF adjustedCurrentTime >= adjustedScheduledTime AND
        processingStatus != "COMPLETED" THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on current time vs scheduled time
- Secondary evaluation considers grace period and early start allowances
- Tertiary evaluation checks processing status to avoid duplicate processing
- Override mechanism for manual schedule control

**Calculation Methods**:
- Time zone conversion for accurate time comparison
- Grace period calculation for flexible scheduling
- Early start window calculation for proactive processing

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Time Reached**: Current time >= scheduled time
- **Grace Period**: Current time within grace period after scheduled time
- **Early Start**: Current time within early start window before scheduled time
- **Manual Override**: scheduleOverride flag is true

**Success Scenarios**:
- **Exact Time**: Current time matches scheduled time exactly
- **Grace Period**: Processing triggered within grace period
- **Early Processing**: Processing allowed before scheduled time
- **Override Processing**: Manual override triggers immediate processing

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Too Early**: Current time before early start window
- **Too Late**: Current time after grace period
- **Already Processed**: Processing status indicates completion
- **No Override**: Manual override not active

**Failure Scenarios**:
- **Premature Check**: Evaluation performed too early
- **Missed Window**: Evaluation performed after grace period
- **Duplicate Processing**: Processing already completed
- **Standard Schedule**: No override and outside time window

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Boundary**: Current time exactly at scheduled time
- **Time Zone Changes**: Daylight saving time transitions
- **Missing Times**: Null or invalid time values
- **System Clock Drift**: Differences between system clocks

**Special Scenarios**:
- **Holiday Processing**: Scheduled time falls on holiday
- **Weekend Processing**: Scheduled time falls on weekend
- **Leap Year**: February 29th scheduling considerations
- **Emergency Processing**: Override for urgent processing needs

**Data Absence Handling**:
- Missing scheduledTime defaults to false evaluation
- Missing currentTime uses system time
- Missing timeZone uses system default time zone

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 1 second (95th percentile)
- **Throughput**: 1000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for time calculations
- **Memory**: 32MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Time Service**: System time and time zone management
- **Configuration Service**: Schedule configuration and override settings

**External Dependencies**:
- **NTP Service**: Network time synchronization for accurate time
- **Calendar Service**: Holiday calendar for business day calculations

**Data Dependencies**:
- System time zone configuration
- Holiday calendar data
- Schedule configuration parameters

## 9. Configuration
**Configurable Thresholds**:
- `defaultGracePeriodMinutes`: integer - Default grace period after scheduled time - Default: 15
- `defaultEarlyStartMinutes`: integer - Default early start window before scheduled time - Default: 5
- `enableHolidayAdjustment`: boolean - Enable holiday calendar adjustments - Default: true

**Evaluation Parameters**:
- `strictTimeValidation`: boolean - Enable strict time validation - Default: true
- `allowWeekendProcessing`: boolean - Allow processing on weekends - Default: false
- `timeToleranceSeconds`: integer - Time comparison tolerance in seconds - Default: 30

**Environment-Specific Settings**:
- Development: Extended grace periods for testing
- Production: Standard regulatory schedule adherence

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required time fields not accessible
- **INVALID_TIME**: Time format or value validation errors
- **TIMEZONE_ERROR**: Time zone conversion failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on time errors
- Fallback to system time for missing currentTime
- Use system time zone for invalid time zone
- Retry mechanism for time service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with schedule context
- Failed evaluations trigger manual review
- Time service errors escalated to operations team
