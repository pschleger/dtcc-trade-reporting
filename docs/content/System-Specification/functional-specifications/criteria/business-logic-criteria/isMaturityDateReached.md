# isMaturityDateReached Criterion Specification

## 1. Component Overview
**Component Name**: isMaturityDateReached
**Component Type**: CyodaCriterion
**Business Domain**: Business Logic Evaluation
**Purpose**: Evaluates whether a trade has reached its maturity date and requires immediate maturity processing
**Workflow Context**: TradeWorkflow (active state transitions to matured state)

## 2. Input Parameters
**Entity Type**: Trade
**Required Fields**:
- `maturityDate`: ISO-8601 date - Trade maturity date
- `currentDate`: ISO-8601 date - Current business date for evaluation
- `tradeStatus`: string - Current trade status
- `productType`: string - Type of financial product

**Optional Fields**:
- `earlyTerminationDate`: ISO-8601 date - Early termination date if applicable
- `maturityProcessingOverride`: boolean - Manual override for maturity processing
- `holidayCalendar`: string - Holiday calendar for business date calculations
- `gracePeriodDays`: integer - Grace period for maturity processing

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "maturity" - Tags for maturity evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "maturity-date-check" - Evaluation context identifier

**Evaluation Context**:
- Business calendar for date calculations
- Product-specific maturity rules
- Grace period configuration

## 3. Evaluation Logic
**Decision Algorithm**:
```
businessDaysFromMaturity = calculateBusinessDays(maturityDate, currentDate)
gracePeriod = getGracePeriodForProduct(productType)

IF tradeStatus == "active" AND
   businessDaysFromMaturity >= 0 AND
   businessDaysFromMaturity <= gracePeriod AND
   NOT maturityProcessingOverride THEN
    RETURN true
ELSE IF earlyTerminationDate != null AND
        calculateBusinessDays(earlyTerminationDate, currentDate) >= 0 THEN
    RETURN true
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on maturity date comparison with current date
- Secondary evaluation considers early termination scenarios
- Grace period evaluation for processing flexibility
- Override mechanism for manual control

**Calculation Methods**:
- Business day calculation excluding weekends and holidays
- Grace period lookup from product configuration
- Early termination date evaluation when applicable

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Active Trade**: tradeStatus equals "active"
- **Maturity Reached**: Current date at or after maturity date
- **Within Grace Period**: Current date within grace period after maturity
- **No Override**: maturityProcessingOverride is false or not set

**Success Scenarios**:
- **Exact Maturity**: Current date equals maturity date
- **Past Maturity**: Current date after maturity date within grace period
- **Early Termination**: Trade with early termination date reached
- **Holiday Adjustment**: Maturity processing adjusted for holiday calendars

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Inactive Trade**: tradeStatus is not "active"
- **Future Maturity**: Maturity date is in the future
- **Grace Period Exceeded**: Current date beyond grace period after maturity
- **Manual Override**: maturityProcessingOverride is true

**Failure Scenarios**:
- **Premature Check**: Maturity date not yet reached
- **Expired Grace**: Grace period for maturity processing exceeded
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
- Missing currentDate uses system date
- Missing tradeStatus defaults to false evaluation

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 1 second (95th percentile)
- **Throughput**: 2000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for date calculations
- **Memory**: 16MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Calendar Service**: Business date and holiday calendar
- **Configuration Service**: Product-specific grace period settings

**External Dependencies**:
- **Market Data Service**: Holiday calendar updates (daily)

**Data Dependencies**:
- Business holiday calendars
- Product-specific maturity processing rules
- Grace period configuration by product type

## 9. Configuration
**Configurable Thresholds**:
- `defaultGracePeriodDays`: integer - Default grace period for maturity processing - Default: 1
- `productGracePeriods`: object - Product-specific grace period overrides
- `enableHolidayAdjustment`: boolean - Enable holiday calendar adjustments - Default: true

**Evaluation Parameters**:
- `strictDateValidation`: boolean - Enable strict date validation - Default: true
- `allowPastMaturity`: boolean - Allow evaluation of past maturity dates - Default: true
- `maxGracePeriodDays`: integer - Maximum allowable grace period - Default: 5

**Environment-Specific Settings**:
- Development: Extended grace periods for testing
- Production: Standard regulatory grace periods

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required date fields not accessible
- **INVALID_DATE**: Date format or value validation errors
- **CALENDAR_ERROR**: Holiday calendar service unavailable
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on data errors
- Fallback to system date for missing currentDate
- Use default grace period for unknown product types
- Retry mechanism for calendar service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with trade context
- Failed evaluations trigger manual review
- Calendar service errors escalated to operations team
