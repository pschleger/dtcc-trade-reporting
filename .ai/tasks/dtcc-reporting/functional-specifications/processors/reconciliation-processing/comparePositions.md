# comparePositions Processor Specification

## 1. Component Overview
**Component Name**: comparePositions
**Component Type**: CyodaProcessor
**Business Domain**: Reconciliation Processing
**Purpose**: Compares internal position data with external counterparty or regulatory positions to identify discrepancies
**Workflow Context**: ReconciliationWorkflow (comparison state)

## 2. Input Specifications
**Entity Type**: PositionComparison
**Required Fields**:
- `reconciliationId`: string - Unique identifier for the reconciliation process
- `internalPositions`: array - Internal position data for comparison
- `externalPositions`: array - External position data from counterparty or regulator
- `comparisonDate`: ISO-8601 date - Business date for position comparison
- `comparisonScope`: object - Scope definition (products, counterparties, portfolios)
- `toleranceSettings`: object - Tolerance thresholds for comparison

**Optional Fields**:
- `previousComparison`: object - Reference to previous comparison for trend analysis
- `comparisonRules`: array - Custom comparison rules for specific scenarios
- `excludedPositions`: array - Positions to exclude from comparison
- `comparisonMetadata`: object - Additional metadata for comparison context

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "reconciliation" - Tags for reconciliation processing nodes
- `responseTimeoutMs`: 180000 - Maximum processing time (3 minutes)
- `processParamId`: "01932b4e-7890-7123-8456-423456789jkl" - Process parameter reference

**Context Data**:
- Position comparison algorithms and tolerance rules
- Product master data for position matching
- Counterparty mapping and identification rules

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "PositionComparison",
    "processingTimestamp": "2024-01-15T14:45:00Z",
    "comparisonResults": {
      "totalPositionsCompared": 1250,
      "matchedPositions": 1180,
      "discrepancyCount": 70,
      "discrepancyRate": 0.056,
      "comparisonSummary": {
        "quantityDiscrepancies": 45,
        "valueDiscrepancies": 25,
        "missingPositions": 15,
        "extraPositions": 10
      }
    },
    "nextActions": ["ANALYZE_DISCREPANCIES"]
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "COMPARISON_FAILED",
  "errorMessage": "Failed to compare position data",
  "details": {
    "reconciliationId": "REC-20240115-001",
    "failureReason": "Data format mismatch",
    "retryable": true
  }
}
```

**Side Effects**:
- Comparison results stored in reconciliation database
- Discrepancy records created for identified differences
- Comparison metrics updated for reporting and monitoring
- Alerts generated for significant discrepancy rates

## 4. Business Logic
**Processing Steps**:
1. Validate and normalize internal and external position data
2. Apply position matching rules to align comparable positions
3. Execute comparison algorithms for quantity, value, and attributes
4. Apply tolerance thresholds to determine significant discrepancies
5. Categorize discrepancies by type and severity
6. Generate comparison summary and statistics
7. Create discrepancy records for further analysis

**Business Rules**:
- **Position Matching**: Positions matched by product, counterparty, and portfolio
- **Tolerance Application**: Discrepancies within tolerance are considered matches
- **Significance Threshold**: Only material discrepancies trigger follow-up actions
- **Data Normalization**: All position data normalized to common format before comparison
- **Exclusion Rules**: Certain position types may be excluded from comparison

**Algorithms**:
- Fuzzy matching algorithm for position alignment
- Statistical analysis for discrepancy significance
- Tolerance band calculation based on position size and volatility

## 5. Validation Rules
**Pre-processing Validations**:
- **Data Completeness**: Both internal and external position datasets must be complete
- **Date Consistency**: All positions must be for the same business date
- **Format Validation**: Position data must conform to expected schema
- **Scope Validation**: Comparison scope must be clearly defined and valid

**Post-processing Validations**:
- **Result Completeness**: All positions must be accounted for in comparison results
- **Discrepancy Validation**: Identified discrepancies must be properly categorized
- **Statistical Validation**: Comparison statistics must be mathematically consistent

**Data Quality Checks**:
- **Position Integrity**: Position data must pass basic integrity checks
- **Matching Quality**: Position matching must achieve minimum quality threshold

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input data validation failures or format issues
- **MATCHING_ERROR**: Position matching algorithm failures
- **CALCULATION_ERROR**: Comparison calculation failures
- **TOLERANCE_ERROR**: Tolerance threshold application failures
- **SYSTEM_ERROR**: Technical system failures or resource constraints

**Error Recovery**:
- **Retry Logic**: Automatic retry with adjusted parameters for transient failures
- **Partial Processing**: Continue with available data if some positions fail
- **Manual Intervention**: Queue complex matching issues for manual review

**Error Propagation**:
- **Workflow Notification**: Notify reconciliation workflow of comparison status
- **Alert Generation**: Generate alerts for comparison failures or high discrepancy rates
- **Audit Trail**: Record all comparison attempts and results

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 120 seconds (95th percentile) for standard position sets
- **Throughput**: 10,000 positions compared per minute
- **Availability**: 99.9% uptime during reconciliation windows

**Resource Requirements**:
- **CPU**: High for complex matching and comparison algorithms
- **Memory**: 2GB per concurrent comparison operation for large position sets
- **I/O**: Intensive database access for position data retrieval and result storage

**Scalability**:
- **Horizontal Scaling**: Support parallel comparison processing across multiple nodes
- **Data Partitioning**: Partition large position sets for distributed processing
- **Performance Optimization**: Optimize algorithms for large-scale position comparison

## 8. Dependencies
**Internal Dependencies**:
- **Position Repository**: Access to internal position data
- **Reconciliation Database**: Storage for comparison results and discrepancies
- **Product Master Data**: Product information for position matching
- **Tolerance Configuration**: Tolerance thresholds and comparison rules

**External Dependencies**:
- **External Data Feeds**: Counterparty or regulatory position data (99.5% SLA)
- **Market Data Service**: Reference data for position valuation (99.9% SLA)

**Data Dependencies**:
- **Position Schemas**: Standard schemas for position data formats
- **Matching Rules**: Business rules for position matching and comparison

## 9. Configuration Parameters
**Required Configuration**:
- `defaultTolerancePercent`: decimal - Default tolerance percentage - Default: 0.01
- `matchingAlgorithm`: string - Position matching algorithm - Default: "FUZZY_MATCH"
- `maxDiscrepancyRate`: decimal - Maximum acceptable discrepancy rate - Default: 0.10
- `comparisonDetailLevel`: string - Level of comparison detail - Default: "DETAILED"

**Optional Configuration**:
- `parallelProcessingEnabled`: boolean - Enable parallel processing - Default: true
- `maxConcurrentComparisons`: integer - Maximum concurrent comparisons - Default: 5
- `statisticalAnalysisEnabled`: boolean - Enable statistical analysis - Default: true
- `trendAnalysisEnabled`: boolean - Enable trend analysis - Default: false

**Environment-Specific Configuration**:
- **Development**: Relaxed tolerances with detailed logging
- **Production**: Strict tolerances with optimized performance settings

## 10. Integration Points
**API Contracts**:
- **Input**: PositionComparison entity with internal and external position data
- **Output**: Comparison results with discrepancy details and summary statistics

**Data Exchange Formats**:
- **Position Format**: Standardized position data format (JSON/XML)
- **Comparison Results**: Structured comparison results with discrepancy details

**Event Publishing**:
- **PositionsCompared**: Published when comparison completes with summary results
- **DiscrepanciesIdentified**: Published when significant discrepancies are found
- **ComparisonFailed**: Published when comparison fails with error details

**Event Consumption**:
- **ReconciliationInitiated**: Triggers position comparison process
- **PositionDataUpdated**: Handles updates to position data requiring recomparison
