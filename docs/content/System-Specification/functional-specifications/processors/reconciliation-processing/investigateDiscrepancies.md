# investigateDiscrepancies Processor Specification

## 1. Component Overview
**Component Name**: investigateDiscrepancies
**Component Type**: CyodaProcessor
**Business Domain**: Reconciliation Processing
**Purpose**: Conducts detailed investigation of complex or critical discrepancies requiring deep analysis and research
**Workflow Context**: ReconciliationWorkflow (investigation state)

## 2. Input Specifications
**Entity Type**: DiscrepancyInvestigation
**Required Fields**:
- `investigationId`: string - Unique identifier for the investigation
- `discrepancyIds`: array - List of discrepancy IDs requiring investigation
- `investigationPriority`: string - Priority level ("CRITICAL", "HIGH", "MEDIUM")
- `investigationScope`: object - Scope and depth of investigation required
- `assignedInvestigator`: string - Identifier of assigned investigator or team

**Optional Fields**:
- `investigationDeadline`: ISO-8601 timestamp - Deadline for investigation completion
- `relatedInvestigations`: array - References to related ongoing investigations
- `investigationNotes`: string - Initial notes or context for investigation
- `escalationCriteria`: object - Criteria for escalating investigation

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "investigation" - Tags for investigation processing nodes
- `responseTimeoutMs`: 300000 - Maximum processing time (5 minutes)
- `processParamId`: "01932b4e-7890-7123-8456-423456789pqr" - Process parameter reference

**Context Data**:
- Investigation procedures and methodologies
- Access to detailed trade and position history
- External data sources for complete analysis

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "DiscrepancyInvestigation",
    "processingTimestamp": "2024-01-15T16:00:00Z",
    "investigationResults": {
      "investigationStatus": "COMPLETED",
      "findingsCount": 3,
      "rootCauseIdentified": true,
      "resolutionRecommendations": [
        "Adjust settlement timing process",
        "Update counterparty data mapping",
        "Implement additional validation checks"
      ],
      "evidenceCollected": {
        "tradeRecords": 15,
        "systemLogs": 8,
        "externalConfirmations": 3
      }
    },
    "nextActions": ["IMPLEMENT_RESOLUTION", "UPDATE_PROCEDURES"]
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "INVESTIGATION_FAILED",
  "errorMessage": "Failed to complete discrepancy investigation",
  "details": {
    "investigationId": "INV-20240115-001",
    "failureReason": "Required data sources unavailable",
    "retryable": true
  }
}
```

**Side Effects**:
- Investigation records created with detailed findings and evidence
- Root cause documentation updated with new insights
- Process improvement recommendations generated
- Investigation status tracked for compliance and audit

## 4. Business Logic
**Processing Steps**:
1. Validate investigation parameters and access required data sources
2. Gather complete evidence from multiple systems and sources
3. Perform detailed analysis of trade lifecycle and position movements
4. Cross-reference with external confirmations and counterparty data
5. Identify root causes and contributing factors
6. Document findings with supporting evidence
7. Generate resolution recommendations and process improvements

**Business Rules**:
- **Investigation Depth**: Investigation depth varies by discrepancy severity and complexity
- **Evidence Requirements**: All findings must be supported by verifiable evidence
- **Timeline Compliance**: Critical investigations must be completed within regulatory timeframes
- **Documentation Standards**: All investigations must follow standardized documentation procedures
- **Escalation Triggers**: Unresolved investigations escalate to senior management

**Algorithms**:
- Timeline reconstruction for trade and position events
- Pattern matching across similar historical discrepancies
- Statistical analysis for anomaly detection and validation

## 5. Validation Rules
**Pre-processing Validations**:
- **Investigation Scope**: Investigation scope must be clearly defined and achievable
- **Data Availability**: Required data sources must be accessible for investigation
- **Priority Validation**: Investigation priority must align with discrepancy severity
- **Resource Allocation**: Sufficient resources must be available for investigation depth

**Post-processing Validations**:
- **Finding Completeness**: All investigation objectives must be addressed
- **Evidence Validation**: All evidence must be verified and documented
- **Recommendation Quality**: Recommendations must be actionable and specific

**Data Quality Checks**:
- **Evidence Integrity**: All collected evidence must pass integrity checks
- **Finding Consistency**: Investigation findings must be internally consistent

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Investigation parameter validation failures
- **DATA_ACCESS_ERROR**: Required data sources unavailable or inaccessible
- **ANALYSIS_ERROR**: Investigation analysis algorithm failures
- **EVIDENCE_ERROR**: Evidence collection or validation failures
- **SYSTEM_ERROR**: Technical system failures or resource constraints

**Error Recovery**:
- **Retry Logic**: Automatic retry with alternative data sources for transient failures
- **Partial Investigation**: Continue with available evidence if some sources fail
- **Manual Escalation**: Escalate to senior investigators for complex issues

**Error Propagation**:
- **Workflow Notification**: Notify reconciliation workflow of investigation status
- **Management Alert**: Alert management for failed critical investigations
- **Audit Trail**: Record all investigation attempts and outcomes

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 240 seconds (95th percentile) for standard investigations
- **Investigation Completion**: 4 hours for critical discrepancies, 24 hours for others
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High for complex analysis and pattern matching algorithms
- **Memory**: 2GB per concurrent investigation for complete data analysis
- **I/O**: Intensive access to multiple data sources and systems

**Scalability**:
- **Parallel Processing**: Support multiple concurrent investigations
- **Resource Management**: Dynamic resource allocation based on investigation complexity
- **Performance Monitoring**: Track investigation times and resource utilization

## 8. Dependencies
**Internal Dependencies**:
- **Trade Repository**: Complete trade history and lifecycle data
- **Position Database**: Detailed position movements and calculations
- **Audit Trail System**: System logs and transaction history
- **Investigation Database**: Investigation procedures and historical findings

**External Dependencies**:
- **Counterparty Systems**: External confirmations and position data (99.0% SLA)
- **Market Data Providers**: Historical market data for valuation analysis (99.5% SLA)
- **Regulatory Databases**: Regulatory reference data and requirements (99.9% SLA)

**Data Dependencies**:
- **Investigation Procedures**: Standardized investigation methodologies
- **Historical Patterns**: Historical discrepancy patterns and resolutions

## 9. Configuration Parameters
**Required Configuration**:
- `investigationDepthLevels`: object - Configuration for different investigation depths
- `evidenceRetentionDays`: integer - Days to retain investigation evidence - Default: 2555 (7 years)
- `maxConcurrentInvestigations`: integer - Maximum concurrent investigations - Default: 10
- `escalationTimeoutHours`: integer - Hours before automatic escalation - Default: 24

**Optional Configuration**:
- `automaticEvidenceCollection`: boolean - Enable automatic evidence gathering - Default: true
- `externalDataSourcesEnabled`: boolean - Enable external data source access - Default: true
- `patternMatchingEnabled`: boolean - Enable historical pattern matching - Default: true
- `investigationTemplates`: object - Templates for different investigation types

**Environment-Specific Configuration**:
- **Development**: Limited data sources with detailed logging
- **Production**: Full data source access with complete investigation capabilities

## 10. Integration Points
**API Contracts**:
- **Input**: DiscrepancyInvestigation entity with investigation parameters and scope
- **Output**: Investigation results with findings, evidence, and recommendations

**Data Exchange Formats**:
- **Investigation Format**: Standardized investigation request and response format
- **Evidence Format**: Structured evidence collection with metadata and validation

**Event Publishing**:
- **InvestigationStarted**: Published when investigation begins with scope and timeline
- **InvestigationCompleted**: Published when investigation completes with findings
- **CriticalFindingsIdentified**: Published when critical issues are discovered

**Event Consumption**:
- **DiscrepancyEscalated**: Triggers detailed investigation for escalated discrepancies
- **InvestigationRequested**: Handles manual investigation requests from operations teams
