# validateBusinessRules Processor Specification

## 1. Component Overview
**Component Name**: validateBusinessRules
**Component Type**: CyodaProcessor
**Business Domain**: Trade Management
**Purpose**: Validates trade entities against complete business rules, trading limits, and regulatory constraints
**Workflow Context**: TradeWorkflow (validating state)

## 2. Input Specifications
**Entity Type**: Trade
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `counterpartyId`: string - Counterparty LEI identifier
- `productType`: string - Financial product type
- `notionalAmount`: decimal - Trade notional amount
- `currency`: string - Trade currency (ISO 4217)
- `tradeDate`: ISO-8601 date - Trade execution date
- `maturityDate`: ISO-8601 date - Trade maturity date

**Optional Fields**:
- `tradingBook`: string - Trading book assignment
- `trader`: string - Responsible trader identifier
- `settlementDate`: ISO-8601 date - Settlement date
- `riskMetrics`: object - Pre-calculated risk metrics

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "validation" - Tags for validation processing nodes
- `responseTimeoutMs`: 15000 - Maximum processing time (15 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-223456789abc" - Process parameter reference

**Context Data**:
- Trading limits and authorization matrix
- Product authorization lists
- Counterparty credit limits
- Regulatory rule configuration

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Trade",
    "processingTimestamp": "2024-01-15T11:00:00Z",
    "validationResults": {
      "businessRulesValid": true,
      "tradingLimitsValid": true,
      "productAuthorized": true,
      "counterpartyAuthorized": true,
      "riskWithinLimits": true
    },
    "riskAssessment": {
      "riskScore": 0.25,
      "creditUtilization": 0.15,
      "concentrationRisk": 0.10
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "VALIDATION_ERROR",
  "errorMessage": "Business rule validation failed",
  "details": {
    "ruleViolations": ["Notional exceeds counterparty limit"],
    "authorizationIssues": ["Product not authorized for trading book"],
    "riskViolations": ["Concentration limit exceeded"]
  }
}
```

**Side Effects**:
- Updates trade entity validation status
- Records validation results for audit trail
- Updates counterparty credit utilization
- Publishes ValidationCompleted event

## 4. Business Logic
**Processing Steps**:
1. Validate counterparty authorization and credit standing
2. Check product authorization for trading book
3. Verify notional amount within trading limits
4. Validate trade dates for business logic consistency
5. Calculate and assess risk metrics
6. Check concentration limits and portfolio constraints
7. Validate regulatory compliance requirements
8. Generate validation report

**Business Rules**:
- **Counterparty Limits**: Trade notional must not exceed counterparty credit limit
- **Product Authorization**: Product must be authorized for the assigned trading book
- **Trading Limits**: Trade size must be within trader and desk limits
- **Date Validation**: Trade date must be valid business date, maturity date must be future
- **Risk Limits**: Trade must not breach portfolio risk limits
- **Concentration Limits**: Trade must not create excessive concentration risk

**Algorithms**:
- Credit utilization calculation including pending trades
- Risk metric calculation using standard risk models
- Concentration risk assessment across counterparties and products

## 5. Validation Rules
**Pre-processing Validations**:
- **Required Data**: All mandatory trade fields present and valid
- **Data Format**: Dates, amounts, and identifiers in correct format
- **Reference Data**: Counterparty and product exist in master data

**Post-processing Validations**:
- **Validation Completeness**: All business rules evaluated
- **Risk Calculation**: Risk metrics calculated successfully
- **Limit Updates**: Credit utilization updated correctly

**Data Quality Checks**:
- **Data Consistency**: Trade data internally consistent
- **Market Data**: Currency and rate data current and valid
- **Master Data**: Reference data current and authorized

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Business rule or limit violations
- **DATA_ERROR**: Missing or invalid reference data
- **CALCULATION_ERROR**: Risk calculation failures
- **SYSTEM_ERROR**: External service or data access failures
- **TIMEOUT_ERROR**: Validation processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient data access errors (max 3 retries)
- Fallback to cached reference data for minor outages
- Manual review queue for complex validation failures

**Error Propagation**:
- Validation failures trigger transition to validation-failed state
- Error details stored for manual review and override
- Critical system errors escalated to operations team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 10 seconds (95th percentile)
- **Throughput**: 200 validations per second
- **Availability**: 99.9% uptime during trading hours

**Resource Requirements**:
- **CPU**: High intensity for risk calculations
- **Memory**: 512MB per concurrent validation
- **I/O**: High for reference data access

**Scalability**:
- Horizontal scaling through calculation node distribution
- Performance varies with trade complexity and risk calculations
- Caching of reference data improves performance significantly

## 8. Dependencies
**Internal Dependencies**:
- **Master Data Service**: Counterparty and product reference data
- **Risk Service**: Risk calculation and limit management
- **Trading Limits Service**: Trader and desk limit configuration
- **Credit Service**: Counterparty credit limits and utilization

**External Dependencies**:
- **Market Data Service**: Currency rates and market data (SLA: 99.5% availability, 1s response)
- **Regulatory Service**: Current regulatory requirements (daily updates)

**Data Dependencies**:
- Counterparty master data with credit limits
- Product authorization matrix
- Trading limits by trader and desk
- Risk model parameters and thresholds

## 9. Configuration Parameters
**Required Configuration**:
- `strictValidation`: boolean - Enable strict validation mode - Default: true
- `riskCalculationEnabled`: boolean - Enable risk metric calculation - Default: true
- `creditCheckEnabled`: boolean - Enable credit limit checking - Default: true

**Optional Configuration**:
- `concentrationThreshold`: decimal - Concentration risk threshold - Default: 0.10
- `riskScoreThreshold`: decimal - Maximum acceptable risk score - Default: 0.50
- `cacheExpiryMinutes`: integer - Reference data cache expiry - Default: 30

**Environment-Specific Configuration**:
- Development: Relaxed limits, mock risk calculations
- Production: Full validation, live risk calculations

## 10. Integration Points
**API Contracts**:
- Input: Trade entity with complete trade details
- Output: Validation results with risk assessment

**Data Exchange Formats**:
- **JSON**: Validation results and risk metrics
- **XML**: Regulatory compliance reporting format

**Event Publishing**:
- **ValidationCompleted**: Published on successful validation with results
- **ValidationFailed**: Published on validation failure with violation details
- **RiskAlert**: Published when risk thresholds exceeded

**Event Consumption**:
- **TradeCreated**: Triggers business rule validation
- **LimitsUpdated**: Updates cached limit information
- **RiskModelUpdated**: Updates risk calculation parameters
