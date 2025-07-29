# assessCancellationImpact Processor Specification

## 1. Component Overview
**Component Name**: assessCancellationImpact
**Component Type**: CyodaProcessor
**Business Domain**: Cancellation Processing
**Purpose**: Assesses the comprehensive impact of trade cancellations on positions, risk metrics, regulatory reporting, and counterparty relationships
**Workflow Context**: CancellationWorkflow (impact assessment state)

## 2. Input Specifications
**Entity Type**: TradeCancellation
**Required Fields**:
- `cancellationId`: string - Unique cancellation request identifier
- `tradeId`: string - Trade identifier to be cancelled
- `cancellationType`: string - Type of cancellation (FULL, PARTIAL, NOVATION)
- `cancellationReason`: string - Business reason for cancellation
- `effectiveDate`: ISO-8601 date - Date when cancellation becomes effective
- `impactScope`: array - Areas to assess (POSITION, RISK, REGULATORY, COUNTERPARTY)

**Optional Fields**:
- `partialAmount`: decimal - Amount for partial cancellation
- `replacementTrade`: object - Replacement trade details for novation
- `marketDataSnapshot`: object - Market data for valuation impact
- `urgencyLevel`: string - Cancellation urgency (NORMAL, HIGH, EMERGENCY)

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "cancellation-impact" - Tags for impact assessment nodes
- `responseTimeoutMs`: 90000 - Maximum processing time (90 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789yzab" - Process parameter reference

**Context Data**:
- Current position data
- Risk model parameters
- Regulatory reporting requirements
- Counterparty relationship data

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeCancellation",
    "processingTimestamp": "2024-01-15T14:00:00Z",
    "impactAssessment": {
      "overallImpactLevel": "HIGH",
      "positionImpact": {
        "notionalReduction": 50000000.00,
        "marketValueReduction": 49750000.00,
        "pnlImpact": 250000.00,
        "affectedPositions": 5,
        "positionElimination": true
      },
      "riskImpact": {
        "var95Reduction": 125000.00,
        "expectedShortfallReduction": 187500.00,
        "concentrationRiskChange": "DECREASED",
        "limitUtilizationChange": -5.2,
        "riskLimitCompliance": "IMPROVED"
      },
      "regulatoryImpact": {
        "reportingRequired": true,
        "reportingDeadline": "2024-01-16T23:59:59Z",
        "affectedReports": ["DTCC_GTR", "POSITION_REPORT"],
        "complianceStatus": "REQUIRES_NOTIFICATION"
      },
      "counterpartyImpact": {
        "exposureReduction": 49750000.00,
        "relationshipImpact": "MODERATE",
        "consentRequired": true,
        "notificationRequired": true,
        "creditLineImpact": "POSITIVE"
      },
      "operationalImpact": {
        "settlementImpact": "CANCELLATION_REQUIRED",
        "collateralImpact": "RELEASE_REQUIRED",
        "downstreamSystems": ["SETTLEMENT", "COLLATERAL", "RISK"],
        "estimatedProcessingTime": "4 hours"
      }
    },
    "recommendations": {
      "approvalLevel": "SENIOR_MANAGEMENT",
      "additionalChecks": ["COUNTERPARTY_CONSENT", "SETTLEMENT_REVIEW"],
      "processingPriority": "HIGH",
      "riskReview": "RECOMMENDED"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "CANCELLATION_IMPACT_ASSESSMENT_FAILED",
  "errorMessage": "Cancellation impact assessment failed due to missing position data",
  "details": {
    "cancellationId": "CANC-001",
    "tradeId": "TRD-001",
    "missingData": ["current_position", "settlement_details"],
    "impactScope": ["POSITION", "OPERATIONAL"]
  }
}
```

**Side Effects**:
- Updates cancellation entity with impact assessment results
- Creates impact assessment audit trail
- Triggers risk limit validation for impact scenarios
- Publishes impact assessment events for downstream systems

## 4. Business Logic
**Processing Steps**:
1. Validate cancellation impact assessment request
2. Retrieve trade data and current position information
3. Calculate position impact from trade cancellation
4. Assess risk metric changes and limit utilization impact
5. Evaluate regulatory reporting requirements and deadlines
6. Analyze counterparty exposure and relationship impact
7. Assess operational impact on settlement and collateral
8. Determine approval requirements based on impact level
9. Generate comprehensive impact summary and recommendations
10. Update cancellation with impact assessment results

**Business Rules**:
- **Impact Thresholds**: Different impact levels trigger different approval requirements
- **Risk Assessment**: Cancellation impact must be assessed against risk limits
- **Regulatory Requirements**: Assessment must consider regulatory notification requirements
- **Counterparty Relations**: Impact on counterparty relationships must be evaluated
- **Operational Impact**: Settlement and collateral impacts must be assessed

**Algorithms**:
- Position elimination calculation for full cancellations
- Partial position reduction calculation for partial cancellations
- Risk metric recalculation with cancelled trade removed
- Counterparty exposure impact analysis with relationship scoring

## 5. Validation Rules
**Pre-processing Validations**:
- **Cancellation Request Validity**: Request must be properly formatted and complete
- **Trade Existence**: Trade to be cancelled must exist and be cancellable
- **Market Data Availability**: Current market data must be available for valuation
- **Impact Scope Validation**: Requested impact scope must be valid

**Post-processing Validations**:
- **Impact Calculation Accuracy**: All impact calculations must be mathematically correct
- **Risk Assessment Validity**: Risk impact must be properly calculated
- **Regulatory Compliance**: Impact assessment must maintain regulatory compliance
- **Data Consistency**: All impact metrics must be internally consistent

**Data Quality Checks**:
- **Numerical Precision**: All calculations maintain required precision
- **Currency Consistency**: All monetary amounts use consistent currency
- **Date Validation**: All dates must be valid and consistent

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input data validation failures
- **CALCULATION_ERROR**: Impact calculation failures
- **DATA_UNAVAILABLE_ERROR**: Required data not accessible
- **SYSTEM_ERROR**: Technical system failures
- **TIMEOUT_ERROR**: Impact assessment timeout exceeded

**Error Recovery**:
- Retry mechanism for transient data access failures
- Partial impact assessment for available data
- Fallback calculations using historical data

**Error Propagation**:
- Detailed error information provided to calling workflows
- Cancellation status updated with assessment failure details
- Error notifications sent to risk management teams

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 90000 milliseconds (95th percentile)
- **Throughput**: 80 assessments per hour
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High computational requirements for complex calculations
- **Memory**: 512MB for large position datasets and calculations
- **I/O**: Database access for position, risk, and market data

**Scalability**:
- Horizontal scaling through assessment node distribution
- Performance optimization through data caching
- Parallel processing for multiple impact dimensions

## 8. Dependencies
**Internal Dependencies**:
- **TradeService**: Trade data retrieval for cancellation
- **PositionService**: Current position data retrieval
- **RiskService**: Risk calculation and limit validation
- **MarketDataService**: Current market data for valuation
- **CounterpartyService**: Counterparty relationship and exposure data

**External Dependencies**:
- **Market Data Provider**: Real-time market data (SLA: 99.9%)
- **Risk Management System**: Risk parameters and limits (SLA: 99.5%)

**Data Dependencies**:
- Current trade and position data
- Market data for valuation
- Risk model parameters
- Counterparty relationship data

## 9. Configuration Parameters
**Required Configuration**:
- `impactAssessmentTimeoutMs`: integer - Maximum assessment time - Default: 90000
- `riskLimitValidation`: boolean - Enable risk limit validation - Default: true
- `marketDataToleranceMinutes`: integer - Market data staleness tolerance - Default: 15
- `impactThresholds`: object - Impact level thresholds - Default: {"LOW": 100000, "MODERATE": 1000000, "HIGH": 10000000}

**Optional Configuration**:
- `enableParallelCalculation`: boolean - Enable parallel processing - Default: true
- `detailedImpactLogging`: boolean - Enable detailed impact logging - Default: false
- `cacheMarketData`: boolean - Enable market data caching - Default: true

**Environment-Specific Configuration**:
- **Development**: Reduced thresholds and simplified calculations
- **Production**: Full thresholds and comprehensive impact assessment

## 10. Integration Points
**API Contracts**:
- **Input**: Cancellation impact assessment request with scope and parameters
- **Output**: Comprehensive impact assessment with recommendations

**Data Exchange Formats**:
- **JSON**: Primary data exchange format for impact assessment
- **XML**: Alternative format for regulatory integration

**Event Publishing**:
- **CancellationImpactAssessed**: Published when impact assessment completes successfully
- **CancellationImpactAssessmentFailed**: Published when assessment fails
- **HighImpactCancellation**: Published when cancellation has high impact

**Event Consumption**:
- **CancellationRequested**: Triggers cancellation impact assessment
- **MarketDataUpdated**: Updates market data for impact calculations
