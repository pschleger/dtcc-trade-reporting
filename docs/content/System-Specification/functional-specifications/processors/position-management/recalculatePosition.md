# recalculatePosition Processor Specification

## 1. Component Overview
**Component Name**: recalculatePosition
**Component Type**: CyodaProcessor
**Business Domain**: Position Management
**Purpose**: Recalculates position data after trade amendments, corrections, or market data updates for accurate regulatory reporting
**Workflow Context**: PositionWorkflow (recalculating state), AmendmentWorkflow (position update state)

## 2. Input Specifications
**Entity Type**: Position
**Required Fields**:
- `positionId`: string - Unique position identifier to recalculate
- `counterpartyId`: string - Counterparty LEI identifier
- `productType`: string - Financial product type
- `currency`: string - Position currency (ISO 4217)
- `recalculationDate`: ISO-8601 date - Position recalculation date
- `triggerEvent`: string - Event triggering recalculation (AMENDMENT, CORRECTION, MARKET_UPDATE)
- `affectedTrades`: array - List of trade IDs requiring position recalculation

**Optional Fields**:
- `previousPosition`: object - Previous position data for comparison
- `marketDataSnapshot`: object - Updated market data for valuation
- `riskParameters`: object - Updated risk calculation parameters
- `recalculationScope`: string - Scope of recalculation (FULL, INCREMENTAL)

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "recalculation" - Tags for recalculation processing nodes
- `responseTimeoutMs`: 90000 - Maximum processing time (90 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789def" - Process parameter reference

**Context Data**:
- Updated trade data for aggregation
- Current market data for valuation
- Risk model parameters
- Audit trail for recalculation history

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Position",
    "processingTimestamp": "2024-01-15T16:30:00Z",
    "recalculationResults": {
      "previousNotionalAmount": 50000000.00,
      "newNotionalAmount": 52000000.00,
      "previousMarketValue": 49750000.00,
      "newMarketValue": 51740000.00,
      "pnlImpact": 1990000.00,
      "riskMetrics": {
        "previousVar95": 125000.00,
        "newVar95": 130000.00,
        "riskImpact": 5000.00
      },
      "recalculationSummary": {
        "tradesAffected": 15,
        "calculationMethod": "INCREMENTAL",
        "triggerEvent": "AMENDMENT"
      }
    },
    "auditTrail": {
      "recalculationId": "RECALC-2024-001",
      "previousCalculationId": "CALC-2024-001",
      "recalculationReason": "Trade amendment impact"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "RECALCULATION_FAILED",
  "errorMessage": "Position recalculation failed due to missing market data",
  "details": {
    "positionId": "POS-001",
    "missingData": ["EUR/USD rate", "volatility surface"],
    "recalculationDate": "2024-01-15"
  }
}
```

**Side Effects**:
- Updates position entity with recalculated values
- Creates audit trail entry for recalculation
- Publishes position update event for downstream systems
- Updates risk metrics and exposure calculations

## 4. Business Logic
**Processing Steps**:
1. Validate recalculation request and position existence
2. Retrieve current position data and affected trade information
3. Gather updated market data and risk parameters
4. Determine recalculation scope (full vs incremental)
5. Perform position recalculation using updated inputs
6. Calculate impact analysis comparing previous and new values
7. Validate recalculated position for reasonableness
8. Update position entity with new calculated values
9. Create audit trail and impact summary
10. Publish position update events

**Business Rules**:
- **Recalculation Trigger**: Only specific events can trigger position recalculation
- **Market Data Currency**: Market data must be current within acceptable tolerance
- **Impact Threshold**: Significant changes require additional validation
- **Audit Requirements**: All recalculations must maintain audit trail
- **Risk Limits**: Recalculated positions must comply with risk limits

**Algorithms**:
- Incremental recalculation for trade-specific changes
- Full recalculation for market data or parameter updates
- Impact analysis using delta calculation methods
- Risk metric recalculation using updated position data

## 5. Validation Rules
**Pre-processing Validations**:
- **Position Existence**: Position must exist and be in valid state for recalculation
- **Trigger Event Validation**: Recalculation trigger must be valid business event
- **Market Data Availability**: Required market data must be available and current
- **Trade Data Integrity**: Affected trades must exist and be in consistent state

**Post-processing Validations**:
- **Calculation Reasonableness**: Recalculated values must be within reasonable bounds
- **Risk Limit Compliance**: New position must comply with established risk limits
- **Impact Analysis**: Position changes must be explainable by trigger events

**Data Quality Checks**:
- **Numerical Precision**: Calculations maintain required precision standards
- **Currency Consistency**: All monetary amounts use consistent currency
- **Date Consistency**: Calculation dates align with business calendar

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input data validation failures
- **MARKET_DATA_ERROR**: Market data unavailable or stale
- **CALCULATION_ERROR**: Mathematical calculation failures
- **SYSTEM_ERROR**: Technical system failures
- **TIMEOUT_ERROR**: Recalculation timeout exceeded

**Error Recovery**:
- Retry mechanism for transient market data failures
- Fallback to previous market data with appropriate warnings
- Partial recalculation for isolated calculation failures

**Error Propagation**:
- Detailed error information propagated to calling workflows
- Position state maintained in consistent state on failure
- Audit trail updated with failure information

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 90000 milliseconds (95th percentile)
- **Throughput**: 50 recalculations per minute
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High computational requirements for complex calculations
- **Memory**: 512MB for large position datasets and market data
- **I/O**: Database access for position and trade data retrieval

**Scalability**:
- Horizontal scaling through calculation node distribution
- Performance degradation graceful under high load
- Caching mechanisms for frequently accessed market data

## 8. Dependencies
**Internal Dependencies**:
- **PositionService**: Position data retrieval and updates
- **TradeService**: Trade data access for aggregation
- **MarketDataService**: Current market data and pricing
- **RiskService**: Risk calculation and validation
- **AuditService**: Audit trail creation and management

**External Dependencies**:
- **Market Data Provider**: Real-time market data feeds (SLA: 99.9%)
- **Risk Management System**: Risk parameters and limits (SLA: 99.5%)

**Data Dependencies**:
- Current and historical position data
- Trade transaction data
- Market data and pricing information
- Risk model parameters and limits

## 9. Configuration Parameters
**Required Configuration**:
- `recalculationTimeoutMs`: integer - Maximum recalculation time - Default: 90000
- `marketDataToleranceMinutes`: integer - Market data staleness tolerance - Default: 15
- `impactThresholdPercent`: decimal - Significant change threshold - Default: 5.0
- `riskLimitValidation`: boolean - Enable risk limit validation - Default: true

**Optional Configuration**:
- `enableIncrementalCalculation`: boolean - Enable incremental calculations - Default: true
- `auditDetailLevel`: string - Audit trail detail level - Default: "STANDARD"
- `cacheMarketData`: boolean - Enable market data caching - Default: true

**Environment-Specific Configuration**:
- **Development**: Reduced timeout and validation for testing
- **Production**: Full validation and extended timeout for accuracy

## 10. Integration Points
**API Contracts**:
- **Input**: Position recalculation request with trigger event details
- **Output**: Recalculated position with impact analysis and audit trail

**Data Exchange Formats**:
- **JSON**: Primary data exchange format for position and calculation data
- **XML**: Alternative format for regulatory reporting integration

**Event Publishing**:
- **PositionRecalculated**: Published when recalculation completes successfully
- **RecalculationFailed**: Published when recalculation fails with error details

**Event Consumption**:
- **TradeAmended**: Triggers position recalculation for affected positions
- **MarketDataUpdated**: Triggers recalculation for market-sensitive positions
