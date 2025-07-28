# calculatePosition Processor Specification

## 1. Component Overview
**Component Name**: calculatePosition
**Component Type**: CyodaProcessor
**Business Domain**: Position Management
**Purpose**: Calculates position data from trade aggregation and market data for regulatory reporting
**Workflow Context**: PositionWorkflow (calculating state)

## 2. Input Specifications
**Entity Type**: Position
**Required Fields**:
- `positionId`: string - Unique position identifier
- `counterpartyId`: string - Counterparty LEI identifier
- `productType`: string - Financial product type
- `currency`: string - Position currency (ISO 4217)
- `calculationDate`: ISO-8601 date - Position calculation date
- `tradeReferences`: array - List of trade IDs for aggregation

**Optional Fields**:
- `previousPosition`: object - Previous position data for delta calculation
- `marketData`: object - Market data for valuation
- `riskParameters`: object - Risk calculation parameters

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "calculation" - Tags for calculation processing nodes
- `responseTimeoutMs`: 60000 - Maximum processing time (60 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789abc" - Process parameter reference

**Context Data**:
- Trade data for aggregation
- Market data for valuation
- Risk model parameters

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Position",
    "processingTimestamp": "2024-01-15T16:00:00Z",
    "calculationResults": {
      "notionalAmount": 50000000.00,
      "marketValue": 49750000.00,
      "unrealizedPnL": -250000.00,
      "riskMetrics": {
        "var95": 125000.00,
        "expectedShortfall": 187500.00
      }
    },
    "aggregationMetadata": {
      "tradesProcessed": 25,
      "calculationDuration": 45000,
      "dataQualityScore": 0.98
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "CALCULATION_ERROR",
  "errorMessage": "Position calculation failed",
  "details": {
    "calculationErrors": ["Missing trade data"],
    "dataQualityIssues": ["Inconsistent trade dates"],
    "systemErrors": ["Market data service unavailable"]
  }
}
```

**Side Effects**:
- Updates Position entity with calculated values
- Creates calculation audit trail
- Updates risk metrics and exposures
- Publishes PositionCalculated event

## 4. Business Logic
**Processing Steps**:
1. Retrieve all trades for the position scope
2. Validate trade data completeness and consistency
3. Apply trade filters and business date logic
4. Aggregate notional amounts by product and currency
5. Calculate market values using current market data
6. Compute risk metrics using approved risk models
7. Generate position summary and detailed breakdown
8. Validate calculation results for reasonableness

**Business Rules**:
- **Trade Inclusion**: Only include trades with status "active" or "matured"
- **Date Logic**: Use trade date for inclusion, settlement date for cash flows
- **Currency Handling**: Convert all amounts to position base currency
- **Risk Calculation**: Apply standard risk models for VaR and ES calculation
- **Data Quality**: Minimum 95% data quality score required for calculation

**Algorithms**:
- Trade aggregation using sum of notional amounts
- Market value calculation using mid-market prices
- VaR calculation using historical simulation method
- Currency conversion using spot rates

## 5. Validation Rules
**Pre-processing Validations**:
- **Trade Data**: All referenced trades exist and accessible
- **Market Data**: Required market data available and current
- **Date Consistency**: Calculation date valid and within business range

**Post-processing Validations**:
- **Calculation Completeness**: All required metrics calculated
- **Reasonableness**: Results within expected ranges
- **Data Consistency**: Calculated values internally consistent

**Data Quality Checks**:
- **Trade Completeness**: All expected trades included in calculation
- **Market Data Currency**: Market data timestamps within acceptable range
- **Risk Model Validity**: Risk parameters current and approved

## 6. Error Handling
**Error Categories**:
- **CALCULATION_ERROR**: Mathematical calculation failures
- **DATA_ERROR**: Missing or invalid input data
- **MARKET_DATA_ERROR**: Market data access or quality issues
- **VALIDATION_ERROR**: Post-calculation validation failures
- **TIMEOUT_ERROR**: Calculation processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient market data errors (max 3 retries)
- Fallback to previous day's market data for minor outages
- Partial calculation for non-critical components

**Error Propagation**:
- Calculation errors trigger transition to calculation-failed state
- Error details stored for manual review and correction
- Critical errors escalated to risk management team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 45 seconds (95th percentile)
- **Throughput**: 10 calculations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for aggregation and risk calculations
- **Memory**: 4GB per concurrent calculation
- **I/O**: Very high for trade data access and market data

**Scalability**:
- Horizontal scaling through calculation node distribution
- Performance varies significantly with trade volume
- Parallel processing for large position calculations

## 8. Dependencies
**Internal Dependencies**:
- **Trade Service**: Trade data access and aggregation
- **Market Data Service**: Current market prices and rates
- **Risk Service**: Risk model parameters and calculations
- **Currency Service**: Exchange rates for currency conversion

**External Dependencies**:
- **Market Data Provider**: Real-time market data (SLA: 99.5% availability, 2s response)
- **Risk Model Service**: Risk calculation engine (SLA: 99.9% availability, 5s response)

**Data Dependencies**:
- Trade master data with complete economics
- Market data with current prices and rates
- Risk model parameters and calibration data
- Currency exchange rates (intraday updates)

## 9. Configuration Parameters
**Required Configuration**:
- `baseCurrency`: string - Base currency for position - Default: "USD"
- `riskModelVersion`: string - Risk model version - Default: "v2.1"
- `dataQualityThreshold`: decimal - Minimum data quality - Default: 0.95

**Optional Configuration**:
- `enableRiskCalculation`: boolean - Enable risk metric calculation - Default: true
- `parallelProcessing`: boolean - Enable parallel processing - Default: true
- `marketDataCacheMinutes`: integer - Market data cache duration - Default: 15

**Environment-Specific Configuration**:
- Development: Smaller datasets, mock market data
- Production: Full datasets, live market data

## 10. Integration Points
**API Contracts**:
- Input: Position entity with trade references
- Output: Calculated position with market values and risk metrics

**Data Exchange Formats**:
- **JSON**: Position calculation results
- **CSV**: Detailed trade breakdown for audit
- **XML**: Risk reporting format

**Event Publishing**:
- **PositionCalculated**: Published on successful calculation with results
- **CalculationFailed**: Published on calculation failure with error details
- **RiskAlert**: Published when risk limits exceeded

**Event Consumption**:
- **TradeUpdated**: Triggers position recalculation
- **MarketDataUpdated**: Updates cached market data
- **RiskModelUpdated**: Updates risk calculation parameters
