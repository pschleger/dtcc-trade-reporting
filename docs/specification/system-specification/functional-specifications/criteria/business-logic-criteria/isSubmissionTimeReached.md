# isSubmissionTimeReached Criterion Specification

## 1. Component Overview
**Component Name**: isSubmissionTimeReached
**Component Type**: CyodaCriterion
**Business Domain**: Regulatory Reporting
**Purpose**: Evaluates whether the scheduled submission time for regulatory reports has been reached and submission should commence
**Workflow Context**: Regulatory reporting workflows requiring time-based submission triggers for DTCC GTR reporting

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `submissionTime`: ISO-8601 timestamp - Scheduled submission time for regulatory report
- `currentTime`: ISO-8601 timestamp - Current system time for evaluation
- `reportType`: string - Type of regulatory report (TRADE_REPORT, POSITION_REPORT, etc.)
- `submissionStatus`: string - Current submission status

**Optional Fields**:
- `submissionWindow`: integer - Submission window in minutes after scheduled time
- `earlySubmissionMinutes`: integer - Minutes before scheduled time to allow early submission
- `businessDayOnly`: boolean - Whether submission only allowed on business days
- `submissionOverride`: boolean - Manual override for immediate submission

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "submission-timing" - Tags for submission timing evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "submission-check" - Evaluation context identifier

**Evaluation Context**:
- Regulatory calendar for business day calculations
- Time zone configuration for accurate submission timing
- Submission window management for regulatory compliance

## 3. Evaluation Logic
**Decision Algorithm**:
```
adjustedCurrentTime = convertToTimeZone(currentTime, reportTimeZone)
adjustedSubmissionTime = convertToTimeZone(submissionTime, reportTimeZone)
earlySubmissionTime = adjustedSubmissionTime - earlySubmissionMinutes
submissionWindowEnd = adjustedSubmissionTime + submissionWindow

IF submissionOverride == true THEN
    RETURN true
ELSE IF businessDayOnly == true AND !isBusinessDay(adjustedCurrentTime) THEN
    RETURN false
ELSE IF adjustedCurrentTime >= earlySubmissionTime AND
        adjustedCurrentTime <= submissionWindowEnd AND
        submissionStatus != "SUBMITTED" THEN
    RETURN true
ELSE IF adjustedCurrentTime >= adjustedSubmissionTime AND
        submissionStatus != "SUBMITTED" THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on current time vs scheduled submission time
- Secondary evaluation considers submission window and early submission allowances
- Tertiary evaluation checks submission status to avoid duplicate submissions
- Business day validation for regulatory compliance
- Override mechanism for urgent submission needs

**Calculation Methods**:
- Time zone conversion for accurate submission timing
- Submission window calculation for regulatory compliance
- Early submission window calculation for proactive reporting
- Business day validation using regulatory calendar

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Time Reached**: Current time >= scheduled submission time
- **Submission Window**: Current time within submission window after scheduled time
- **Early Submission**: Current time within early submission window before scheduled time
- **Manual Override**: submissionOverride flag is true
- **Business Day**: Current time falls on valid business day (if required)

**Success Scenarios**:
- **Exact Time**: Current time matches submission time exactly
- **Window Submission**: Submission triggered within allowed window
- **Early Submission**: Submission allowed before scheduled time
- **Override Submission**: Manual override triggers immediate submission
- **Business Day Submission**: Submission on valid regulatory business day

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Too Early**: Current time before early submission window
- **Too Late**: Current time after submission window
- **Already Submitted**: Submission status indicates completion
- **Non-Business Day**: Current time falls on holiday/weekend (if business day required)
- **No Override**: Manual override not active

**Failure Scenarios**:
- **Premature Check**: Evaluation performed too early
- **Missed Window**: Evaluation performed after submission window
- **Duplicate Submission**: Submission already completed
- **Holiday Restriction**: Submission attempted on non-business day
- **Standard Schedule**: No override and outside submission window

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Boundary**: Current time exactly at submission time
- **Time Zone Changes**: Daylight saving time transitions during submission window
- **Missing Times**: Null or invalid time values
- **System Clock Drift**: Differences between system clocks
- **Window Boundaries**: Exact start/end of submission window

**Special Scenarios**:
- **Holiday Submission**: Scheduled time falls on regulatory holiday
- **Weekend Submission**: Scheduled time falls on weekend
- **Emergency Submission**: Override for urgent regulatory reporting
- **Late Submission**: Submission after regulatory deadline
- **Cross-Day Submission**: Submission window spans multiple days

**Data Absence Handling**:
- Missing submissionTime defaults to false evaluation
- Missing currentTime uses system time
- Missing reportType defaults to TRADE_REPORT
- Missing submissionStatus defaults to PENDING

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 500 milliseconds (95th percentile)
- **Throughput**: 2000 evaluations per second
- **Availability**: 99.95% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for time calculations and business day validation
- **Memory**: 16MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Time Service**: System time and time zone management
- **Configuration Service**: Submission schedule configuration and override settings
- **Calendar Service**: Regulatory business day validation

**External Dependencies**:
- **NTP Service**: Network time synchronization for accurate submission timing
- **Regulatory Calendar Service**: DTCC business day calendar for submission validation

**Data Dependencies**:
- System time zone configuration
- Regulatory calendar data
- Submission schedule configuration parameters
- Report type submission requirements

## 9. Configuration
**Configurable Thresholds**:
- `defaultSubmissionWindow`: integer - Default submission window after scheduled time - Default: 30
- `defaultEarlySubmissionMinutes`: integer - Default early submission window - Default: 10
- `enableBusinessDayValidation`: boolean - Enable business day validation - Default: true

**Evaluation Parameters**:
- `strictTimeValidation`: boolean - Enable strict time validation - Default: true
- `allowWeekendSubmission`: boolean - Allow submission on weekends - Default: false
- `timeToleranceSeconds`: integer - Time comparison tolerance in seconds - Default: 15

**Environment-Specific Settings**:
- Development: Extended submission windows for testing
- Production: Standard regulatory submission timing adherence

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required time fields not accessible
- **INVALID_TIME**: Time format or value validation errors
- **TIMEZONE_ERROR**: Time zone conversion failures
- **CALENDAR_ERROR**: Business day validation failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on time errors
- Fallback to system time for missing currentTime
- Use system time zone for invalid time zone
- Skip business day validation on calendar errors
- Retry mechanism for time service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with submission context
- Failed evaluations trigger manual review
- Time service errors escalated to operations team
- Calendar errors reported to regulatory compliance team
