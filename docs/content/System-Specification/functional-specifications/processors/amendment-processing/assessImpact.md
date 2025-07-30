# assessImpact Processor Specification

## 1. Component Overview
**Component Name**: assessImpact
**Component Type**: CyodaProcessor
**Business Domain**: Amendment Processing
**Purpose**: Assesses the complete impact of trade amendments on positions, risk metrics, regulatory reporting, and downstream systems
**Workflow Context**: AmendmentWorkflow (impact assessment state)

## 2. Input Specifications
**Entity Type**: TradeAmendment
**Required Fields**:
- `amendmentId`: string - Unique amendment request identifier
- `originalTradeId`: string - Original trade identifier being amended
- `amendmentFields`: object - Fields being amended with new values
- `amendmentType`: string - Type of amendment (ECONOMIC, OPERATIONAL, LIFECYCLE)
- `effectiveDate`: ISO-8601 date - Date when amendment becomes effective
- `impactScope`: array - Areas to assess (POSITION, RISK, REGULATORY, COUNTERPARTY)

**Optional Fields**:
- `previousImpactAssessment`: object - Previous impact assessment for comparison
- `marketDataSnapshot`: object - Market data for valuation impact
- `riskParameters`: object - Risk calculation parameters
- `regulatoryContext`: object - Regulatory reporting context

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "impact-assessment" - Tags for impact assessment nodes
- `responseTimeoutMs`: 90000 - Maximum processing time (90 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789mno" - Process parameter reference

**Context Data**:
- Current position data
- Risk model parameters
- Regulatory reporting requirements
- Counterparty exposure limits

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeAmendment",
    "processingTimestamp": "2024-01-15T15:00:00Z",
    "impactAssessment": {
      "overallImpactLevel": "MODERATE",
      "positionImpact": {
        "notionalChange": 2000000.00,
        "marketValueChange": 1950000.00,
        "pnlImpact": -50000.00,
        "affectedPositions": 3
      },
      "riskImpact": {
        "var95Change": 15000.00,
        "expectedShortfallChange": 22500.00,
        "concentrationRiskChange": "INCREASED",
        "limitUtilizationChange": 2.5
      },
      "regulatoryImpact": {
        "reportingRequired": true,
        "reportingDeadline": "2024-01-16T23:59:59Z",
        "affectedReports": ["DTCC_GTR", "POSITION_REPORT"],
        "complianceStatus": "COMPLIANT"
      },
      "counterpartyImpact": {
        "exposureChange": 1950000.00,
        "limitBreachRisk": false,
        "consentRequired": true,
        "notificationRequired": true
      },
      "systemImpact": {
        "downstreamSystems": ["RISK_SYSTEM", "REPORTING_SYSTEM"],
        "dataReconciliation": "REQUIRED",
        "estimatedProcessingTime": "30 minutes"
      }
    },
    "recommendations": {
      "approvalLevel": "SENIOR_TRADER",
      "additionalChecks": ["COUNTERPARTY_CONSENT", "RISK_REVIEW"],
      "processingPriority": "HIGH"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "IMPACT_ASSESSMENT_FAILED",
  "errorMessage": "Impact assessment failed due to missing position data",
  "details": {
    "amendmentId": "AMD-001",
    "missingData": ["current_position", "risk_parameters"],
    "impactScope": ["POSITION", "RISK"]
  }
}
```

**Side Effects**:
- Updates amendment entity with impact assessment results
- Creates impact assessment audit trail
- Triggers risk limit validation if thresholds exceeded
- Publishes impact assessment events for downstream systems

## 4. Business Logic
**Processing Steps**:
1. Validate impact assessment request and retrieve amendment details
2. Gather current position and risk data for baseline
3. Calculate position impact from amended trade values
4. Assess risk metric changes and limit utilization
5. Evaluate regulatory reporting requirements and deadlines
6. Analyze counterparty exposure and consent requirements
7. Identify affected downstream systems and processes
8. Determine approval requirements based on impact level
9. Generate complete impact summary and recommendations
10. Update amendment with impact assessment results

**Business Rules**:
- **Impact Thresholds**: Different impact levels trigger different approval requirements
- **Risk Limits**: Impact assessment must validate against risk limits
- **Regulatory Deadlines**: Assessment must consider regulatory reporting deadlines
- **Counterparty Limits**: Exposure changes must respect counterparty limits
- **System Dependencies**: All affected systems must be identified

**Algorithms**:
- Position delta calculation for notional and market value changes
- Risk metric recalculation using updated trade parameters
- Regulatory impact analysis based on jurisdiction requirements
- Counterparty exposure calculation with limit checking

## 5. Validation Rules
**Pre-processing Validations**:
- **Amendment Data Completeness**: All required amendment fields must be present
- **Original Trade Validation**: Original trade must exist and be in amendable state
- **Market Data Availability**: Current market data must be available for valuation
- **Impact Scope Validation**: Requested impact scope must be valid

**Post-processing Validations**:
- **Impact Calculation Accuracy**: All impact calculations must be mathematically correct
- **Risk Limit Compliance**: Impact must not violate established risk limits
- **Regulatory Compliance**: Impact must maintain regulatory compliance
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
- Amendment status updated with assessment failure details
- Error notifications sent to risk management teams

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 90000 milliseconds (95th percentile)
- **Throughput**: 100 assessments per hour
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
- **PositionService**: Current position data retrieval
- **RiskService**: Risk calculation and limit validation
- **MarketDataService**: Current market data for valuation
- **RegulatoryService**: Regulatory reporting requirements
- **CounterpartyService**: Counterparty exposure and limits

**External Dependencies**:
- **Market Data Provider**: Real-time market data (SLA: 99.9%)
- **Risk Management System**: Risk parameters and limits (SLA: 99.5%)

**Data Dependencies**:
- Current position and trade data
- Market data for valuation
- Risk model parameters
- Regulatory reporting requirements

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
- **Production**: Full thresholds and impact assessment

## 10. Integration Points
**API Contracts**:
- **Input**: Amendment impact assessment request with scope and parameters
- **Output**: Comprehensive impact assessment with recommendations

**Data Exchange Formats**:
- **JSON**: Primary data exchange format for impact assessment
- **XML**: Alternative format for regulatory integration

**Event Publishing**:
- **ImpactAssessed**: Published when impact assessment completes successfully
- **ImpactAssessmentFailed**: Published when assessment fails
- **RiskLimitBreached**: Published when impact exceeds risk limits

**Event Consumption**:
- **AmendmentValidated**: Triggers impact assessment process
- **MarketDataUpdated**: Updates market data for impact calculations
