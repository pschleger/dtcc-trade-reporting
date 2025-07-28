# isGracePeriodElapsed Criterion Specification

## 1. Component Overview
**Component Name**: isGracePeriodElapsed
**Component Type**: CyodaCriterion
**Business Domain**: Regulatory Reporting
**Purpose**: Evaluates whether the regulatory grace period for processing or submission has elapsed and escalation procedures should commence
**Workflow Context**: Regulatory reporting and compliance workflows requiring grace period management for DTCC submissions and processing deadlines

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `deadlineTime`: ISO-8601 timestamp - Original regulatory deadline
- `currentTime`: ISO-8601 timestamp - Current system time for evaluation
- `gracePeriodMinutes`: integer - Grace period duration in minutes after deadline
- `processingStatus`: string - Current processing or submission status

**Optional Fields**:
- `escalationLevel`: string - Current escalation level (NONE, WARNING, CRITICAL)
- `businessDayOnly`: boolean - Whether grace period only applies on business days
- `automaticExtension`: boolean - Whether automatic extension is available
- `extensionMinutes`: integer - Additional extension period if available

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "grace-period" - Tags for grace period evaluation nodes
- `responseTimeoutMs`: 2500 - Maximum evaluation time (2.5 seconds)
- `context`: "grace-period-check" - Evaluation context identifier

**Evaluation Context**:
- Regulatory calendar for business day calculations
- Escalation procedures for grace period violations
- Extension mechanisms for exceptional circumstances

## 3. Evaluation Logic
**Decision Algorithm**:
```
adjustedCurrentTime = convertToTimeZone(currentTime, regulatoryTimeZone)
adjustedDeadlineTime = convertToTimeZone(deadlineTime, regulatoryTimeZone)
gracePeriodEnd = adjustedDeadlineTime + gracePeriodMinutes

IF automaticExtension == true AND extensionMinutes > 0 THEN
    gracePeriodEnd = gracePeriodEnd + extensionMinutes
END IF

IF businessDayOnly == true THEN
    gracePeriodEnd = adjustToNextBusinessDay(gracePeriodEnd)
END IF

IF adjustedCurrentTime > gracePeriodEnd AND
   processingStatus != "COMPLETED" AND
   processingStatus != "SUBMITTED" THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on current time vs grace period end
- Secondary evaluation considers business day adjustments
- Tertiary evaluation checks processing status to avoid false positives
- Extension mechanism for exceptional circumstances
- Time zone conversion for accurate regulatory compliance

**Calculation Methods**:
- Grace period end calculation from deadline plus grace minutes
- Business day adjustment for regulatory compliance
- Automatic extension calculation when available
- Time zone conversion for accurate deadline management

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Grace Period Elapsed**: Current time > grace period end time
- **Processing Incomplete**: Processing or submission not completed
- **Business Day Compliance**: Grace period properly adjusted for business days
- **No Extensions**: All available extensions exhausted

**Success Scenarios**:
- **Standard Expiration**: Grace period elapsed on business day
- **Weekend Expiration**: Grace period adjusted to next business day
- **Extended Expiration**: Grace period with automatic extension elapsed
- **Critical Escalation**: Grace period elapsed requiring immediate action

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Grace Period Active**: Current time <= grace period end time
- **Processing Complete**: Processing or submission already completed
- **Extension Available**: Automatic extension still available
- **Business Day Pending**: Grace period extends to next business day

**Failure Scenarios**:
- **Within Grace**: Current time still within grace period
- **Already Completed**: Processing completed before grace period
- **Extension Active**: Automatic extension preventing expiration
- **Holiday Adjustment**: Grace period extended due to holiday

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Expiration**: Current time exactly at grace period end
- **Zero Grace Period**: No grace period configured
- **Negative Grace**: Grace period in past due to clock issues
- **Maximum Extension**: Multiple extensions reaching limit

**Special Scenarios**:
- **Holiday Grace**: Grace period spanning regulatory holidays
- **Weekend Grace**: Grace period ending on weekend
- **Emergency Extension**: Manual extension for exceptional circumstances
- **System Outage**: Grace period during system maintenance
- **Clock Synchronization**: Time differences between systems

**Data Absence Handling**:
- Missing deadlineTime defaults to false evaluation
- Missing currentTime uses system time
- Missing gracePeriodMinutes defaults to 0 (no grace period)
- Missing processingStatus defaults to PENDING

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 300 milliseconds (95th percentile)
- **Throughput**: 3000 evaluations per second
- **Availability**: 99.95% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for time calculations and business day validation
- **Memory**: 12MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Time Service**: System time and time zone management
- **Configuration Service**: Grace period configuration and extension settings
- **Calendar Service**: Regulatory business day validation

**External Dependencies**:
- **NTP Service**: Network time synchronization for accurate timing
- **Regulatory Calendar Service**: DTCC business day calendar for compliance

**Data Dependencies**:
- Regulatory time zone configuration
- Business day calendar data
- Grace period configuration parameters
- Extension policy settings

## 9. Configuration
**Configurable Thresholds**:
- `defaultGracePeriodMinutes`: integer - Default grace period duration - Default: 60
- `maxExtensionMinutes`: integer - Maximum automatic extension - Default: 120
- `enableBusinessDayAdjustment`: boolean - Enable business day adjustments - Default: true

**Evaluation Parameters**:
- `strictDeadlineValidation`: boolean - Enable strict deadline validation - Default: true
- `allowWeekendGrace`: boolean - Allow grace period on weekends - Default: false
- `escalationThresholdMinutes`: integer - Minutes before grace period for escalation - Default: 30

**Environment-Specific Settings**:
- Development: Extended grace periods for testing
- Production: Standard regulatory grace period compliance

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required deadline fields not accessible
- **INVALID_TIME**: Time format or value validation errors
- **TIMEZONE_ERROR**: Time zone conversion failures
- **CALENDAR_ERROR**: Business day validation failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on timing errors
- Fallback to system time for missing currentTime
- Use system time zone for invalid time zone
- Skip business day adjustment on calendar errors
- Retry mechanism for time service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with grace period context
- Failed evaluations trigger manual review
- Time service errors escalated to operations team
- Calendar errors reported to regulatory compliance team
- Grace period violations trigger immediate escalation
