# isRetentionPeriodMet Criterion Specification

## 1. Component Overview
**Component Name**: isRetentionPeriodMet
**Component Type**: CyodaCriterion
**Business Domain**: Regulatory Compliance
**Purpose**: Evaluates whether the regulatory retention period for trade and reporting data has been met and data can be archived or purged
**Workflow Context**: Data lifecycle management and regulatory compliance workflows requiring retention period validation

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `entityId`: string - Unique entity identifier (trade, report, etc.)
- `entityType`: string - Type of entity (TRADE, REPORT, CONFIRMATION)
- `creationDate`: ISO-8601 timestamp - Entity creation date
- `currentDate`: ISO-8601 timestamp - Current date for retention calculation

**Optional Fields**:
- `retentionPeriodYears`: integer - Specific retention period in years
- `regulatoryJurisdiction`: string - Regulatory jurisdiction (US, EU, UK)
- `lastAccessDate`: ISO-8601 timestamp - Last access date for entity
- `legalHoldStatus`: boolean - Whether entity is under legal hold

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "retention-period" - Tags for retention period evaluation nodes
- `responseTimeoutMs`: 2000 - Maximum evaluation time (2 seconds)
- `context`: "retention-check" - Evaluation context identifier

**Evaluation Context**:
- Regulatory retention requirements by jurisdiction
- Entity type specific retention periods
- Legal hold and litigation hold management

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF entityId == null OR entityId.isEmpty() THEN
    RETURN false
END IF

IF legalHoldStatus == true THEN
    RETURN false  // Cannot archive during legal hold
END IF

effectiveRetentionYears = getRetentionPeriod(entityType, regulatoryJurisdiction)

IF retentionPeriodYears != null THEN
    effectiveRetentionYears = retentionPeriodYears
END IF

retentionEndDate = creationDate + (effectiveRetentionYears * 365 * 24 * 60 * 60 * 1000)

IF currentDate >= retentionEndDate THEN
    RETURN true
ELSE
    RETURN false
END IF
```

**Boolean Logic**:
- Primary evaluation calculates retention period based on entity type and jurisdiction
- Secondary evaluation checks for legal hold restrictions
- Tertiary evaluation compares current date to retention end date
- Entity identifier validation for context
- Legal hold override for litigation protection

**Calculation Methods**:
- Retention period calculation based on regulatory requirements
- Date arithmetic for retention end date calculation
- Legal hold status validation
- Jurisdiction-specific retention rule application

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Retention Met**: Current date >= retention end date
- **No Legal Hold**: legalHoldStatus is false or null
- **Valid Entity**: entityId is present and valid
- **Valid Dates**: creationDate and currentDate are valid

**Success Scenarios**:
- **Standard Retention**: Entity retention period completed
- **Extended Retention**: Custom retention period completed
- **Jurisdiction Retention**: Jurisdiction-specific retention completed
- **Archive Ready**: Entity ready for archival or purging

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Retention Pending**: Current date < retention end date
- **Legal Hold Active**: legalHoldStatus is true
- **Invalid Entity**: entityId is null or empty
- **Invalid Dates**: creationDate or currentDate invalid

**Failure Scenarios**:
- **Retention Active**: Entity still within retention period
- **Legal Protection**: Entity protected by legal hold
- **Invalid Entity**: Entity identifier not valid
- **Date Error**: Date calculation errors
- **Jurisdiction Restriction**: Jurisdiction requires extended retention

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Retention Date**: Current date exactly equals retention end date
- **Leap Year Calculation**: Retention period spanning leap years
- **Time Zone Differences**: Date calculations across time zones
- **Partial Year Retention**: Retention periods with partial years

**Special Scenarios**:
- **Litigation Hold**: Entity under active litigation
- **Regulatory Investigation**: Entity subject to regulatory investigation
- **Cross-Border Retention**: Multiple jurisdiction requirements
- **Emergency Retention**: Extended retention for emergency situations
- **System Migration**: Retention during system migration

**Data Absence Handling**:
- Missing entityId defaults to false evaluation
- Missing entityType defaults to TRADE
- Missing creationDate defaults to false evaluation
- Missing currentDate uses system date

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 250 milliseconds (95th percentile)
- **Throughput**: 3000 evaluations per second
- **Availability**: 99.9% uptime

**Resource Requirements**:
- **CPU**: Low intensity for date calculations
- **Memory**: 8MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Configuration Service**: Retention policy configuration by entity type and jurisdiction
- **Legal Hold Service**: Legal hold status tracking and management
- **Calendar Service**: Date calculation and business day validation

**External Dependencies**:
- **Regulatory Database**: Retention requirements by jurisdiction
- **Legal System**: Legal hold and litigation status

**Data Dependencies**:
- Retention policy configuration data
- Legal hold tracking data
- Regulatory requirement reference data
- Entity lifecycle metadata

## 9. Configuration
**Configurable Thresholds**:
- `defaultRetentionYears`: map - Default retention by entity type - Default: {TRADE: 7, REPORT: 5, CONFIRMATION: 3}
- `jurisdictionRetentionYears`: map - Retention by jurisdiction - Default: {US: 7, EU: 5, UK: 6}
- `enableLegalHoldCheck`: boolean - Enable legal hold validation - Default: true

**Evaluation Parameters**:
- `strictDateValidation`: boolean - Enable strict date validation - Default: true
- `allowPartialYears`: boolean - Allow partial year calculations - Default: false
- `useBusinessDays`: boolean - Use business days for calculations - Default: false

**Environment-Specific Settings**:
- Development: Shorter retention periods for testing
- Production: Full regulatory retention periods

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required date fields not accessible
- **INVALID_DATE**: Date format or value validation errors
- **CALCULATION_ERROR**: Date arithmetic failures
- **CONFIGURATION_ERROR**: Retention policy configuration failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on date errors (conservative approach)
- Fallback to default retention periods on configuration errors
- Skip legal hold check on service errors
- Retry mechanism for service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with entity context
- Failed evaluations trigger manual review
- Configuration errors escalated to compliance team
- Date calculation errors reported to development team
