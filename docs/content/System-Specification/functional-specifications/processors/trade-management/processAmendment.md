# processAmendment Processor Specification

## 1. Component Overview
**Component Name**: processAmendment
**Component Type**: CyodaProcessor
**Business Domain**: Trade Management
**Purpose**: Processes trade amendment requests with validation, impact assessment, and regulatory compliance
**Workflow Context**: TradeWorkflow (amending state)

## 2. Input Specifications
**Entity Type**: Trade
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `amendmentRequest`: object - Amendment details and changes
- `amendmentType`: string - Type of amendment (economic, operational, etc.)
- `requestTimestamp`: ISO-8601 timestamp - Amendment request time
- `requestedBy`: string - User or system requesting amendment

**Optional Fields**:
- `businessJustification`: string - Business reason for amendment
- `urgencyLevel`: string - Amendment urgency classification
- `approvalOverrides`: array - Pre-approved authorization overrides

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "amendment" - Tags for amendment processing nodes
- `responseTimeoutMs`: 45000 - Maximum processing time (45 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-223456789abd" - Process parameter reference

**Context Data**:
- Amendment authorization matrix
- Impact assessment rules
- Regulatory reporting requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Trade",
    "processingTimestamp": "2024-01-15T11:15:00Z",
    "amendmentResults": {
      "amendmentApplied": true,
      "originalValues": {
        "notionalAmount": 10000000,
        "maturityDate": "2029-01-20"
      },
      "amendedValues": {
        "notionalAmount": 12000000,
        "maturityDate": "2029-06-20"
      },
      "impactAssessment": {
        "positionImpact": "moderate",
        "riskImpact": "low",
        "reportingRequired": true
      }
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "AMENDMENT_ERROR",
  "errorMessage": "Trade amendment processing failed",
  "details": {
    "validationErrors": ["Amendment exceeds authorization limits"],
    "businessRuleViolations": ["Maturity date cannot be extended beyond 5 years"],
    "systemErrors": ["Position service unavailable"]
  }
}
```

**Side Effects**:
- Updates Trade entity with amended values
- Creates amendment audit trail
- Triggers position recalculation
- Generates regulatory amendment report
- Publishes TradeAmended event

## 4. Business Logic
**Processing Steps**:
1. Validate amendment request against business rules
2. Check authorization limits and approval requirements
3. Assess impact on positions, risk, and reporting
4. Apply amendment changes to trade entity
5. Update related entities and calculations
6. Generate regulatory amendment notifications
7. Create audit trail

**Business Rules**:
- **Authorization Limits**: Amendments must be within authorized limits
- **Business Rules**: All trade business rules must remain satisfied
- **Regulatory Compliance**: Amendments must comply with regulatory requirements
- **Impact Thresholds**: Significant amendments require additional approvals
- **Timing Restrictions**: Amendments subject to market timing restrictions

**Algorithms**:
- Impact assessment using position and risk calculation engines
- Authorization validation using role-based permission matrix
- Regulatory requirement determination using rule engine

## 5. Validation Rules
**Pre-processing Validations**:
- **Amendment Validity**: Amendment request contains valid changes
- **Trade Status**: Trade in amendable status
- **Authorization**: Requestor has appropriate authorization

**Post-processing Validations**:
- **Data Consistency**: Amended trade data internally consistent
- **Business Rules**: All business rules satisfied after amendment
- **Impact Assessment**: Impact assessment completed successfully

**Data Quality Checks**:
- **Amendment Completeness**: All required amendment fields present
- **Value Validation**: Amended values within acceptable ranges
- **Regulatory Compliance**: Amendment meets regulatory requirements

## 6. Error Handling
**Error Categories**:
- **AMENDMENT_ERROR**: Amendment processing logic failures
- **VALIDATION_ERROR**: Amendment validation failures
- **AUTHORIZATION_ERROR**: Insufficient authorization for amendment
- **SYSTEM_ERROR**: External system or calculation failures
- **TIMEOUT_ERROR**: Amendment processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient system errors (max 3 retries)
- Rollback mechanism for partial amendment failures
- Manual review queue for authorization failures

**Error Propagation**:
- Amendment errors trigger transition to amendment-failed state
- Error details stored for manual review and reprocessing
- Critical errors escalated to trading operations team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 35 seconds (95th percentile)
- **Throughput**: 20 amendments per second
- **Availability**: 99.9% uptime during trading hours

**Resource Requirements**:
- **CPU**: High intensity for impact assessment and calculations
- **Memory**: 1GB per concurrent amendment
- **I/O**: High for trade data access and updates

**Scalability**:
- Horizontal scaling through amendment processing nodes
- Performance varies with amendment complexity and impact scope
- Parallel processing for independent amendment components

## 8. Dependencies
**Internal Dependencies**:
- **Authorization Service**: Amendment authorization and approval
- **Position Service**: Position impact assessment and recalculation
- **Risk Service**: Risk impact assessment
- **Regulatory Service**: Regulatory compliance validation

**External Dependencies**:
- **Market Data Service**: Current market data for impact assessment (SLA: 99.5% availability, 2s response)
- **Regulatory Reporting Service**: Amendment reporting requirements (SLA: 99.9% availability, 5s response)

**Data Dependencies**:
- Trade master data with complete economics
- Amendment authorization matrix
- Position and risk calculation parameters
- Regulatory reporting rules and templates

## 9. Configuration Parameters
**Required Configuration**:
- `authorizationRequired`: boolean - Require authorization validation - Default: true
- `impactAssessmentEnabled`: boolean - Enable impact assessment - Default: true
- `regulatoryReportingEnabled`: boolean - Enable regulatory reporting - Default: true

**Optional Configuration**:
- `maxAmendmentValue`: decimal - Maximum amendment value - Default: 100000000
- `autoApprovalThreshold`: decimal - Auto-approval threshold - Default: 1000000
- `auditTrailEnabled`: boolean - Enable detailed audit trail - Default: true

**Environment-Specific Configuration**:
- Development: Relaxed authorization, mock impact assessment
- Production: Full authorization, live impact assessment

## 10. Integration Points
**API Contracts**:
- Input: Trade entity with amendment request details
- Output: Amendment results with impact assessment

**Data Exchange Formats**:
- **JSON**: Amendment request and results
- **XML**: Regulatory amendment reporting format

**Event Publishing**:
- **TradeAmended**: Published on successful amendment with details
- **AmendmentFailed**: Published on amendment failure with error details
- **ImpactAssessmentCompleted**: Published with impact assessment results

**Event Consumption**:
- **AmendmentRequested**: Triggers amendment processing
- **AuthorizationUpdated**: Updates authorization configuration
- **RegulatoryRulesUpdated**: Updates regulatory compliance rules
