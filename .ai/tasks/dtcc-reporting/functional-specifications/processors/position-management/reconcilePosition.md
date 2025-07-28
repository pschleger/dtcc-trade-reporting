# reconcilePosition Processor Specification

## 1. Component Overview
**Component Name**: reconcilePosition
**Component Type**: CyodaProcessor
**Business Domain**: Position Management
**Purpose**: Reconciles internal positions with external counterparty positions to identify and resolve discrepancies
**Workflow Context**: PositionWorkflow (reconciling state)

## 2. Input Specifications
**Entity Type**: Position
**Required Fields**:
- `positionId`: string - Unique position identifier
- `internalPosition`: object - Internal position data
- `externalPositions`: array - External counterparty position data
- `reconciliationDate`: ISO-8601 date - Business date for reconciliation
- `counterpartyId`: string - Counterparty LEI identifier

**Optional Fields**:
- `toleranceThresholds`: object - Reconciliation tolerance settings
- `previousReconciliation`: object - Previous reconciliation results
- `reconciliationScope`: object - Specific reconciliation scope and filters

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "reconciliation" - Tags for reconciliation processing nodes
- `responseTimeoutMs`: 90000 - Maximum processing time (90 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789abe" - Process parameter reference

**Context Data**:
- Reconciliation tolerance configuration
- Discrepancy analysis rules
- Counterparty data mapping

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Position",
    "processingTimestamp": "2024-01-15T17:00:00Z",
    "reconciliationResults": {
      "reconciliationCompleted": true,
      "discrepanciesFound": 3,
      "discrepanciesResolved": 1,
      "matchRate": 0.92,
      "totalVariance": 125000.00
    },
    "discrepancyDetails": {
      "notionalVariances": 2,
      "dateDiscrepancies": 1,
      "statusMismatches": 0,
      "significantDiscrepancies": 1
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "RECONCILIATION_ERROR",
  "errorMessage": "Position reconciliation failed",
  "details": {
    "dataErrors": ["External position data unavailable"],
    "comparisonErrors": ["Position format mismatch"],
    "systemErrors": ["Reconciliation service timeout"],
    "validationErrors": ["Tolerance thresholds invalid"]
  }
}
```

**Side Effects**:
- Updates Position entity with reconciliation status
- Creates discrepancy records for investigation
- Generates reconciliation report
- Publishes PositionReconciled event

## 4. Business Logic
**Processing Steps**:
1. Validate internal and external position data
2. Normalize position data formats for comparison
3. Compare positions across multiple dimensions
4. Identify discrepancies and calculate variances
5. Apply tolerance thresholds to determine significance
6. Generate detailed reconciliation report
7. Create action items for discrepancy resolution

**Business Rules**:
- **Tolerance Thresholds**: Discrepancies within tolerance are acceptable
- **Significance Criteria**: Large discrepancies require immediate investigation
- **Data Currency**: Position data must be current as of reconciliation date
- **Completeness**: All expected positions must be included in reconciliation
- **Audit Trail**: Complete reconciliation audit trail required

**Algorithms**:
- Multi-dimensional position comparison (notional, market value, risk metrics)
- Variance calculation with statistical analysis
- Tolerance-based significance determination
- Automated discrepancy categorization

## 5. Validation Rules
**Pre-processing Validations**:
- **Data Availability**: Internal and external position data accessible
- **Data Currency**: Position data current as of reconciliation date
- **Format Consistency**: Position data in comparable formats

**Post-processing Validations**:
- **Reconciliation Completeness**: All positions compared successfully
- **Discrepancy Identification**: All discrepancies properly identified
- **Report Generation**: Reconciliation report generated correctly

**Data Quality Checks**:
- **Data Integrity**: Position data not corrupted or incomplete
- **Temporal Consistency**: Position timestamps consistent
- **Counterparty Validation**: External position sources validated

## 6. Error Handling
**Error Categories**:
- **RECONCILIATION_ERROR**: Reconciliation logic or comparison failures
- **DATA_ERROR**: Position data availability or quality issues
- **FORMAT_ERROR**: Position data format or structure issues
- **SYSTEM_ERROR**: External system or service failures
- **TIMEOUT_ERROR**: Reconciliation processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient data access errors (max 3 retries)
- Partial reconciliation for available data sources
- Manual review queue for complex reconciliation failures

**Error Propagation**:
- Reconciliation errors trigger transition to reconciliation-failed state
- Error details stored for manual review and retry
- Critical errors escalated to position management team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 75 seconds (95th percentile)
- **Throughput**: 5 reconciliations per minute
- **Availability**: 99.9% uptime during reconciliation windows

**Resource Requirements**:
- **CPU**: Very high intensity for position comparison and analysis
- **Memory**: 4GB per concurrent reconciliation
- **I/O**: Very high for external data access and comparison

**Scalability**:
- Horizontal scaling through reconciliation processing nodes
- Performance varies significantly with position volume and complexity
- Parallel processing for independent position comparisons

## 8. Dependencies
**Internal Dependencies**:
- **Position Service**: Internal position data access
- **Comparison Service**: Position comparison and variance calculation
- **Reporting Service**: Reconciliation report generation
- **Discrepancy Service**: Discrepancy tracking and management

**External Dependencies**:
- **Counterparty Systems**: External position data sources (SLA varies by counterparty)
- **Market Data Service**: Reference data for position validation (SLA: 99.5% availability, 3s response)

**Data Dependencies**:
- Internal position data with complete metrics
- External counterparty position feeds
- Reconciliation tolerance configuration
- Position comparison rules and algorithms

## 9. Configuration Parameters
**Required Configuration**:
- `defaultTolerancePercent`: decimal - Default tolerance threshold - Default: 0.01
- `significanceThreshold`: decimal - Significance threshold for discrepancies - Default: 10000.00
- `maxDiscrepancyCount`: integer - Maximum discrepancies to process - Default: 1000

**Optional Configuration**:
- `enableAutomaticResolution`: boolean - Enable automatic discrepancy resolution - Default: false
- `parallelComparison`: boolean - Enable parallel position comparison - Default: true
- `detailedReporting`: boolean - Enable detailed reconciliation reporting - Default: true

**Environment-Specific Configuration**:
- Development: Relaxed tolerances, mock external data
- Production: Strict tolerances, live external data

## 10. Integration Points
**API Contracts**:
- Input: Position entity with internal and external position data
- Output: Reconciliation results with discrepancy details

**Data Exchange Formats**:
- **JSON**: Position data and reconciliation results
- **CSV**: Detailed reconciliation report
- **XML**: External position data format

**Event Publishing**:
- **PositionReconciled**: Published on successful reconciliation with results
- **ReconciliationFailed**: Published on reconciliation failure with error details
- **DiscrepancyIdentified**: Published when significant discrepancies found

**Event Consumption**:
- **PositionValidated**: Triggers position reconciliation process
- **ExternalDataReceived**: Updates external position data
- **ToleranceUpdated**: Updates reconciliation tolerance configuration
