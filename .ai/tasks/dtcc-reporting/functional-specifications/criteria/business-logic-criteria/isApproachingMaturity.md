# isApproachingMaturity Criterion Specification

## 1. Component Overview
**Component Name**: isApproachingMaturity
**Component Type**: CyodaCriterion
**Business Domain**: Trade Management
**Purpose**: Evaluates whether a trade is approaching its maturity date and requires maturity processing
**Workflow Context**: TradeWorkflow (active state transitions to maturing state)

## 2. Input Parameters
**Entity Type**: Trade
**Required Fields**:
- `maturityDate`: ISO-8601 date - Trade maturity date
- `productType`: string - Type of financial product
- `tradeStatus`: string - Current trade status
- `currentDate`: ISO-8601 date - Current business date for evaluation
- `notionalAmount`: decimal - Trade notional amount

**Optional Fields**:
- `earlyTerminationDate`: ISO-8601 date - Early termination date if applicable
- `rolloverFlag`: boolean - Indicates if trade can be rolled over
- `maturityProcessingOverride`: boolean - Manual override for maturity processing
- `holidayCalendar`: string - Holiday calendar for business date calculations

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "maturity" - Tags for maturity evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time (5 seconds)
- `context`: "maturity-check" - Evaluation context identifier

**Evaluation Context**:
- Business calendar for date calculations
- Product-specific maturity processing rules
- Maturity notification lead time configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
businessDaysToMaturity = calculateBusinessDays(currentDate, maturityDate)
leadTimeDays = getLeadTimeForProduct(productType)

IF tradeStatus == "active" AND
   businessDaysToMaturity <= leadTimeDays AND
   businessDaysToMaturity > 0 AND
   NOT maturityProcessingOverride THEN
    RETURN true
ELSE IF earlyTerminationDate != null AND
        calculateBusinessDays(currentDate, earlyTerminationDate) <= leadTimeDays THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on business days to maturity
- Secondary evaluation considers early termination scenarios
- Product-specific lead time requirements
- Override mechanism for manual control

**Calculation Methods**:
- Business day calculation excluding weekends and holidays
- Product-specific lead time lookup from configuration
- Early termination date evaluation when applicable

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Active Trade**: tradeStatus equals "active"
- **Maturity Proximity**: Business days to maturity <= configured lead time
- **Future Maturity**: Maturity date is in the future (not past due)
- **No Override**: maturityProcessingOverride is false or not set

**Success Scenarios**:
- **Standard Maturity**: Trade approaching maturity within lead time window
- **Early Termination**: Trade with early termination date approaching
- **Product-Specific**: Different lead times based on product complexity
- **Holiday Adjustment**: Maturity processing adjusted for holiday calendars

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Inactive Trade**: tradeStatus is not "active"
- **Distant Maturity**: Business days to maturity > configured lead time
- **Past Maturity**: Maturity date has already passed
- **Manual Override**: maturityProcessingOverride is true

**Failure Scenarios**:
- **Too Early**: Trade maturity too far in the future
- **Already Matured**: Trade has passed maturity date
- **Cancelled Trade**: Trade status prevents maturity processing
- **Override Active**: Manual override prevents automatic processing

## 6. Edge Cases
**Boundary Conditions**:
- **Same Day Maturity**: Maturity date equals current date
- **Weekend Maturity**: Maturity falls on weekend or holiday
- **Missing Maturity Date**: Trade without defined maturity date
- **Invalid Dates**: Malformed or inconsistent date values

**Special Scenarios**:
- **Perpetual Trades**: Trades without maturity dates
- **Callable Products**: Products with multiple potential maturity dates
- **Cross-Currency**: Trades with different holiday calendars
- **Emergency Processing**: Override for urgent maturity processing

**Data Absence Handling**:
- Missing maturityDate defaults to false evaluation
- Missing productType uses default lead time
- Missing currentDate uses system date

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 1 second (95th percentile)
- **Throughput**: 1000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for date calculations
- **Memory**: 32MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Calendar Service**: Business date and holiday calendar
- **Configuration Service**: Product-specific lead time settings

**External Dependencies**:
- **Market Data Service**: Holiday calendar updates (daily)

**Data Dependencies**:
- Business holiday calendars
- Product-specific maturity processing rules
- Lead time configuration by product type

## 9. Configuration
**Configurable Thresholds**:
- `defaultLeadTimeDays`: integer - Default lead time for maturity processing - Default: 5
- `productLeadTimes`: object - Product-specific lead time overrides
- `enableHolidayAdjustment`: boolean - Enable holiday calendar adjustments - Default: true

**Evaluation Parameters**:
- `strictDateValidation`: boolean - Enable strict date validation - Default: true
- `allowPastMaturity`: boolean - Allow evaluation of past maturity dates - Default: false

**Environment-Specific Settings**:
- Development: Extended lead times for testing
- Production: Standard regulatory lead times

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required date fields not accessible
- **INVALID_DATE**: Date format or value validation errors
- **CALENDAR_ERROR**: Holiday calendar service unavailable
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on data errors
- Fallback to system date for missing currentDate
- Use default lead time for unknown product types
- Retry mechanism for calendar service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with trade context
- Failed evaluations trigger manual review
- Calendar service errors escalated to operations team
