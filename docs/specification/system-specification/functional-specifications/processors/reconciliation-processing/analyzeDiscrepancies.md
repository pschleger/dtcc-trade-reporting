# analyzeDiscrepancies Processor Specification

## 1. Component Overview
**Component Name**: analyzeDiscrepancies
**Component Type**: CyodaProcessor
**Business Domain**: Reconciliation Processing
**Purpose**: Analyzes identified position discrepancies to determine root causes and categorize by severity and type
**Workflow Context**: ReconciliationWorkflow (analysis state)

## 2. Input Specifications
**Entity Type**: DiscrepancyAnalysis
**Required Fields**:
- `reconciliationId`: string - Reference to the parent reconciliation process
- `discrepancies`: array - List of identified discrepancies from position comparison
- `analysisDate`: ISO-8601 date - Business date for discrepancy analysis
- `analysisScope`: object - Scope of analysis (products, counterparties, time periods)
- `historicalData`: object - Historical discrepancy patterns for trend analysis

**Optional Fields**:
- `customAnalysisRules`: array - Custom rules for specific discrepancy types
- `priorityOverrides`: object - Manual priority overrides for specific discrepancies
- `excludedDiscrepancies`: array - Discrepancies to exclude from analysis
- `analysisMetadata`: object - Additional context for analysis

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "analysis" - Tags for analysis processing nodes
- `responseTimeoutMs`: 240000 - Maximum processing time (4 minutes)
- `processParamId`: "01932b4e-7890-7123-8456-423456789mno" - Process parameter reference

**Context Data**:
- Historical discrepancy patterns and root cause mappings
- Business rules for discrepancy categorization and prioritization
- Reference data for trade and position analysis

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "DiscrepancyAnalysis",
    "processingTimestamp": "2024-01-15T15:15:00Z",
    "analysisResults": {
      "totalDiscrepancies": 70,
      "categorizedDiscrepancies": {
        "timing": 25,
        "valuation": 20,
        "settlement": 15,
        "data_quality": 10
      },
      "severityDistribution": {
        "critical": 5,
        "high": 15,
        "medium": 30,
        "low": 20
      },
      "rootCauseAnalysis": {
        "system_lag": 30,
        "manual_error": 20,
        "market_data": 15,
        "process_timing": 5
      }
    },
    "nextActions": ["INVESTIGATE_CRITICAL", "RESOLVE_HIGH_PRIORITY"]
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "ANALYSIS_FAILED",
  "errorMessage": "Failed to analyze discrepancies",
  "details": {
    "reconciliationId": "REC-20240115-001",
    "failureReason": "Insufficient historical data",
    "retryable": true
  }
}
```

**Side Effects**:
- Discrepancy records updated with analysis results and categorization
- Root cause patterns identified and stored for future reference
- Priority assignments made for follow-up investigation and resolution
- Analysis metrics updated for reporting and trend monitoring

## 4. Business Logic
**Processing Steps**:
1. Retrieve and validate discrepancy data from comparison results
2. Apply categorization rules to classify discrepancies by type
3. Perform root cause analysis using historical patterns and business rules
4. Assign severity levels based on impact and materiality
5. Identify patterns and trends across discrepancies
6. Generate prioritized action items for resolution
7. Update discrepancy records with analysis results

**Business Rules**:
- **Severity Classification**: Based on monetary impact, regulatory significance, and business risk
- **Root Cause Mapping**: Systematic mapping of discrepancy patterns to known causes
- **Priority Assignment**: Critical and high-severity discrepancies receive immediate attention
- **Pattern Recognition**: Recurring patterns trigger process improvement initiatives
- **Escalation Triggers**: Certain discrepancy types automatically escalate to management

**Algorithms**:
- Machine learning pattern recognition for root cause identification
- Statistical clustering for discrepancy categorization
- Risk-based scoring for severity and priority assignment

## 5. Validation Rules
**Pre-processing Validations**:
- **Discrepancy Data**: All discrepancies must have complete comparison details
- **Historical Data**: Sufficient historical data must be available for pattern analysis
- **Analysis Scope**: Analysis scope must be clearly defined and achievable
- **Rule Consistency**: Analysis rules must be consistent and non-conflicting

**Post-processing Validations**:
- **Categorization Completeness**: All discrepancies must be properly categorized
- **Severity Assignment**: All discrepancies must have assigned severity levels
- **Action Generation**: Appropriate follow-up actions must be generated

**Data Quality Checks**:
- **Analysis Consistency**: Analysis results must be internally consistent
- **Pattern Validation**: Identified patterns must be statistically significant

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input data validation failures or incomplete discrepancy data
- **ANALYSIS_ERROR**: Root cause analysis algorithm failures
- **CATEGORIZATION_ERROR**: Discrepancy categorization failures
- **PATTERN_ERROR**: Pattern recognition algorithm failures
- **SYSTEM_ERROR**: Technical system failures or resource constraints

**Error Recovery**:
- **Retry Logic**: Automatic retry with simplified analysis for transient failures
- **Fallback Analysis**: Use basic categorization if advanced analysis fails
- **Manual Review**: Queue complex discrepancies for manual analysis

**Error Propagation**:
- **Workflow Notification**: Notify reconciliation workflow of analysis status
- **Alert Generation**: Generate alerts for analysis failures or critical findings
- **Audit Trail**: Record all analysis attempts and results

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 180 seconds (95th percentile) for standard discrepancy sets
- **Throughput**: 1,000 discrepancies analyzed per minute
- **Availability**: 99.9% uptime during reconciliation windows

**Resource Requirements**:
- **CPU**: High for pattern recognition and statistical analysis
- **Memory**: 1GB per concurrent analysis operation for large discrepancy sets
- **I/O**: Database access for historical data and analysis result storage

**Scalability**:
- **Horizontal Scaling**: Support parallel analysis processing across multiple nodes
- **Data Partitioning**: Partition large discrepancy sets for distributed analysis
- **Algorithm Optimization**: Optimize machine learning algorithms for performance

## 8. Dependencies
**Internal Dependencies**:
- **Discrepancy Repository**: Access to discrepancy data from position comparison
- **Historical Analysis Database**: Historical patterns and root cause data
- **Business Rules Engine**: Rules for categorization and severity assignment
- **Pattern Recognition Service**: Machine learning algorithms for pattern analysis

**External Dependencies**:
- **Market Data Service**: Reference data for valuation discrepancy analysis (99.9% SLA)
- **Trade Repository**: Trade data for timing and settlement analysis (99.5% SLA)

**Data Dependencies**:
- **Historical Patterns**: Historical discrepancy patterns and resolutions
- **Business Rules**: Current rules for discrepancy analysis and categorization

## 9. Configuration Parameters
**Required Configuration**:
- `severityThresholds`: object - Monetary thresholds for severity levels
- `analysisDepth`: string - Depth of analysis ("BASIC", "DETAILED", "COMPREHENSIVE") - Default: "DETAILED"
- `patternRecognitionEnabled`: boolean - Enable ML pattern recognition - Default: true
- `historicalLookbackDays`: integer - Days of historical data to analyze - Default: 90

**Optional Configuration**:
- `customCategorization`: object - Custom categorization rules
- `escalationThresholds`: object - Automatic escalation thresholds
- `trendAnalysisEnabled`: boolean - Enable trend analysis - Default: true
- `rootCauseConfidenceThreshold`: decimal - Minimum confidence for root cause - Default: 0.75

**Environment-Specific Configuration**:
- **Development**: Basic analysis with detailed logging
- **Production**: Comprehensive analysis with optimized performance

## 10. Integration Points
**API Contracts**:
- **Input**: DiscrepancyAnalysis entity with discrepancy data and analysis parameters
- **Output**: Analysis results with categorization, severity, and recommended actions

**Data Exchange Formats**:
- **Discrepancy Format**: Standardized discrepancy data with comparison details
- **Analysis Results**: Structured analysis results with categorization and priorities

**Event Publishing**:
- **DiscrepanciesAnalyzed**: Published when analysis completes with summary results
- **CriticalDiscrepanciesFound**: Published when critical discrepancies are identified
- **AnalysisCompleted**: Published when full analysis cycle completes

**Event Consumption**:
- **DiscrepanciesIdentified**: Triggers discrepancy analysis process
- **HistoricalDataUpdated**: Handles updates to historical patterns affecting analysis
