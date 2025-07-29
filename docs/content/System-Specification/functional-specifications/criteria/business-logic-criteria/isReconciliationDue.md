# isReconciliationDue Criterion Specification

## 1. Component Overview
**Component Name**: isReconciliationDue
**Component Type**: CyodaCriterion
**Business Domain**: Business Logic Evaluation
**Purpose**: Evaluates whether position reconciliation is due based on schedule, triggers, or business rules
**Workflow Context**: PositionWorkflow (active state transitions to reconciling state)

## 2. Input Parameters
**Entity Type**: Position
**Required Fields**:
- `positionId`: string - Unique position identifier
- `lastReconciliationDate`: ISO-8601 date - Date of last reconciliation
- `currentDate`: ISO-8601 date - Current business date for evaluation
- `reconciliationSchedule`: string - Reconciliation frequency (daily, weekly, monthly)
- `positionStatus`: string - Current position status

**Optional Fields**:
- `reconciliationTriggers`: array - Specific triggers for reconciliation
- `reconciliationOverride`: boolean - Manual override for reconciliation timing
- `holidayCalendar`: string - Holiday calendar for business date calculations
- `counterpartyRequirements`: object - Counterparty-specific reconciliation requirements

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "reconciliation" - Tags for reconciliation evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time (5 seconds)
- `context`: "reconciliation-due-check" - Evaluation context identifier

**Evaluation Context**:
- Reconciliation schedule configuration
- Business calendar for date calculations
- Trigger condition definitions

## 3. Evaluation Logic
**Decision Algorithm**:
```
daysSinceLastReconciliation = calculateBusinessDays(lastReconciliationDate, currentDate)
scheduledInterval = getReconciliationInterval(reconciliationSchedule)
triggerConditions = evaluateTriggerConditions(reconciliationTriggers)

IF positionStatus == "active" AND
   (daysSinceLastReconciliation >= scheduledInterval OR
    triggerConditions.anyTriggered == true) AND
   NOT reconciliationOverride THEN
    RETURN true
ELSE IF reconciliationOverride == true AND
        reconciliationOverride.forceReconciliation == true THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on scheduled reconciliation intervals
- Secondary evaluation considers trigger conditions
- Override evaluation for manual reconciliation control
- Position status validation for reconciliation eligibility

**Calculation Methods**:
- Business day calculation excluding weekends and holidays
- Schedule interval lookup from configuration
- Trigger condition evaluation using business rules

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Active Position**: positionStatus equals "active"
- **Schedule Due**: Days since last reconciliation >= scheduled interval
- **Trigger Activated**: One or more reconciliation triggers activated
- **Manual Override**: reconciliationOverride forces reconciliation

**Success Scenarios**:
- **Scheduled Reconciliation**: Regular scheduled reconciliation due
- **Trigger-Based**: Specific business trigger activated reconciliation
- **Manual Override**: Manual reconciliation requested
- **Holiday Adjustment**: Reconciliation adjusted for holiday calendars

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Inactive Position**: positionStatus is not "active"
- **Schedule Not Due**: Days since last reconciliation < scheduled interval
- **No Triggers**: No reconciliation triggers activated
- **Override Prevents**: reconciliationOverride prevents reconciliation

**Failure Scenarios**:
- **Too Early**: Scheduled reconciliation not yet due
- **Inactive Position**: Position status prevents reconciliation
- **Override Block**: Manual override blocks automatic reconciliation
- **No Triggers**: No business conditions trigger reconciliation

## 6. Edge Cases
**Boundary Conditions**:
- **First Reconciliation**: Position never reconciled before
- **Weekend/Holiday**: Reconciliation due on non-business day
- **Schedule Change**: Reconciliation schedule recently changed
- **Multiple Triggers**: Multiple reconciliation triggers activated simultaneously

**Special Scenarios**:
- **Emergency Reconciliation**: Urgent reconciliation required
- **Counterparty Request**: Counterparty-requested reconciliation
- **Regulatory Requirement**: Regulatory-driven reconciliation timing
- **System Maintenance**: Reconciliation during maintenance windows

**Data Absence Handling**:
- Missing lastReconciliationDate triggers immediate reconciliation
- Missing reconciliationSchedule uses default daily schedule
- Missing currentDate uses system date

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 2 seconds (95th percentile)
- **Throughput**: 1000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for date calculations and rule evaluation
- **Memory**: 32MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Calendar Service**: Business date and holiday calendar
- **Configuration Service**: Reconciliation schedule and trigger configuration
- **Position Service**: Position status and reconciliation history

**External Dependencies**:
- **Market Data Service**: Holiday calendar updates (daily)

**Data Dependencies**:
- Business holiday calendars
- Reconciliation schedule configuration
- Trigger condition definitions
- Counterparty reconciliation requirements

## 9. Configuration
**Configurable Thresholds**:
- `defaultReconciliationInterval`: integer - Default reconciliation interval (days) - Default: 1
- `scheduleIntervals`: object - Schedule-specific intervals (daily: 1, weekly: 7, monthly: 30)
- `enableHolidayAdjustment`: boolean - Enable holiday calendar adjustments - Default: true

**Evaluation Parameters**:
- `strictScheduling`: boolean - Enable strict schedule adherence - Default: true
- `allowTriggerOverride`: boolean - Allow triggers to override schedule - Default: true
- `maxReconciliationInterval`: integer - Maximum days between reconciliations - Default: 7

**Environment-Specific Settings**:
- Development: Relaxed scheduling, extended intervals
- Production: Strict scheduling, standard intervals

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required reconciliation data not accessible
- **INVALID_DATE**: Date format or value validation errors
- **CALENDAR_ERROR**: Holiday calendar service unavailable
- **CONFIGURATION_ERROR**: Invalid reconciliation configuration
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to true evaluation on data errors (conservative approach)
- Fallback to system date for missing currentDate
- Use default schedule for unknown reconciliation schedules
- Retry mechanism for calendar service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with position context
- Failed evaluations trigger manual review
- Calendar service errors escalated to operations team
