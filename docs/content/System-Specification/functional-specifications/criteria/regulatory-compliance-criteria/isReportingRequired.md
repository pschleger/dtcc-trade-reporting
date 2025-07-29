# isReportingRequired Criterion Specification

## 1. Component Overview
**Component Name**: isReportingRequired
**Component Type**: CyodaCriterion
**Business Domain**: Regulatory Compliance
**Purpose**: Evaluates whether regulatory reporting to DTCC GTR is required for a trade or transaction based on regulatory rules and thresholds
**Workflow Context**: Trade processing and regulatory reporting workflows requiring determination of reporting obligations

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `productType`: string - Financial product type (SWAP, FORWARD, OPTION)
- `notionalAmount`: decimal - Trade notional amount
- `counterpartyType`: string - Counterparty classification (FINANCIAL, NON_FINANCIAL)

**Optional Fields**:
- `jurisdiction`: string - Trading jurisdiction (US, EU, UK)
- `tradingDesk`: string - Trading desk or business unit
- `executionVenue`: string - Execution venue or platform
- `clearingStatus`: string - Clearing status (CLEARED, UNCLEARED)
- `reportingExemption`: boolean - Whether trade has reporting exemption

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "reporting-rules" - Tags for reporting rule evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "reporting-requirement-check" - Evaluation context identifier

**Evaluation Context**:
- Regulatory reporting thresholds by product type
- Jurisdiction-specific reporting requirements
- Counterparty classification rules

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF tradeId == null OR tradeId.isEmpty() THEN
    RETURN false
END IF

IF reportingExemption == true THEN
    RETURN false
END IF

reportingThreshold = getReportingThreshold(productType, jurisdiction)
isAboveThreshold = (notionalAmount >= reportingThreshold)

isReportableProduct = isProductReportable(productType)
isReportableCounterparty = isCounterpartyReportable(counterpartyType)

IF isReportableProduct AND 
   isReportableCounterparty AND 
   isAboveThreshold THEN
    
    IF clearingStatus == "UNCLEARED" THEN
        RETURN true  // Uncleared trades always reportable if above threshold
    ELSE IF clearingStatus == "CLEARED" THEN
        RETURN checkClearedReportingRules(productType, jurisdiction)
    ELSE
        RETURN true  // Default to reportable if clearing status unknown
    END IF
ELSE
    RETURN false
END IF
```

**Boolean Logic**:
- Primary evaluation checks reporting exemption status
- Secondary evaluation validates product type reportability
- Tertiary evaluation checks counterparty type requirements
- Quaternary evaluation applies notional amount thresholds
- Clearing status consideration for final determination

**Calculation Methods**:
- Reporting threshold lookup by product type and jurisdiction
- Product type classification for reporting requirements
- Counterparty type validation for reporting obligations
- Notional amount comparison against thresholds

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Reportable Product**: productType requires regulatory reporting
- **Reportable Counterparty**: counterpartyType triggers reporting obligation
- **Above Threshold**: notionalAmount >= reporting threshold
- **No Exemption**: reportingExemption is false or null
- **Valid Trade**: tradeId is present and valid

**Success Scenarios**:
- **Standard Reporting**: Trade meets all reporting criteria
- **Uncleared Reporting**: Uncleared trade above threshold
- **Cleared Reporting**: Cleared trade meeting specific rules
- **Cross-Border Reporting**: Trade subject to multiple jurisdictions

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Exempted Trade**: reportingExemption is true
- **Non-Reportable Product**: productType not subject to reporting
- **Non-Reportable Counterparty**: counterpartyType not triggering reporting
- **Below Threshold**: notionalAmount < reporting threshold
- **Invalid Trade**: tradeId is null or empty

**Failure Scenarios**:
- **Exempt Trade**: Trade has regulatory exemption
- **Small Trade**: Trade below reporting threshold
- **Non-Reportable Product**: Product type not covered by regulations
- **Internal Trade**: Counterparty type not requiring reporting
- **Invalid Data**: Trade data insufficient for determination

## 6. Edge Cases
**Boundary Conditions**:
- **Exact Threshold**: Notional amount exactly equals threshold
- **Multi-Currency**: Notional amounts in different currencies
- **Partial Trades**: Partial fills or amendments affecting notional
- **Cross-Jurisdiction**: Trades spanning multiple jurisdictions

**Special Scenarios**:
- **Emergency Exemption**: Temporary exemptions during market stress
- **Regulatory Change**: New regulations affecting reporting requirements
- **System Transition**: Reporting rule changes during system updates
- **Data Correction**: Reporting determination after trade corrections
- **Bulk Determination**: Batch processing of reporting requirements

**Data Absence Handling**:
- Missing tradeId defaults to false evaluation
- Missing productType defaults to false evaluation
- Missing notionalAmount defaults to false evaluation
- Missing counterpartyType defaults to FINANCIAL

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 500 milliseconds (95th percentile)
- **Throughput**: 2000 evaluations per second
- **Availability**: 99.95% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity for rule evaluation and threshold lookups
- **Memory**: 16MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Configuration Service**: Reporting threshold and rule configuration
- **Product Service**: Product type classification and validation
- **Counterparty Service**: Counterparty classification and validation

**External Dependencies**:
- **Regulatory Database**: Current reporting requirements and thresholds
- **Currency Service**: Currency conversion for multi-currency thresholds

**Data Dependencies**:
- Reporting threshold configuration data
- Product type classification data
- Counterparty classification data
- Regulatory rule reference data

## 9. Configuration
**Configurable Thresholds**:
- `reportingThresholds`: map - Thresholds by product and jurisdiction
- `reportableProducts`: list - Product types requiring reporting
- `reportableCounterparties`: list - Counterparty types triggering reporting

**Evaluation Parameters**:
- `enableExemptionCheck`: boolean - Enable exemption validation - Default: true
- `strictThresholdValidation`: boolean - Enable strict threshold validation - Default: true
- `allowMultiJurisdiction`: boolean - Allow multiple jurisdiction rules - Default: true

**Environment-Specific Settings**:
- Development: Relaxed thresholds for testing
- Production: Current regulatory thresholds and rules

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required trade fields not accessible
- **THRESHOLD_ERROR**: Threshold lookup failures
- **CLASSIFICATION_ERROR**: Product or counterparty classification failures
- **RULE_ERROR**: Reporting rule evaluation failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to true evaluation on rule errors (conservative approach)
- Fallback to basic threshold check on classification errors
- Use default thresholds on configuration errors
- Retry mechanism for service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with trade context
- Failed evaluations trigger manual review
- Rule errors escalated to compliance team
- Configuration errors reported to regulatory team
