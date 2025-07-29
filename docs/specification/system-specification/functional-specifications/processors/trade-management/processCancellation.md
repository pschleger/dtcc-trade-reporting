# processCancellation Processor Specification

## 1. Component Overview
**Component Name**: processCancellation
**Component Type**: CyodaProcessor
**Business Domain**: Trade Management
**Purpose**: Processes trade cancellation requests with authorization, impact assessment, and regulatory compliance
**Workflow Context**: TradeWorkflow (cancelling state)

## 2. Input Specifications
**Entity Type**: Trade
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `cancellationRequest`: object - Cancellation details and justification
- `cancellationType`: string - Type of cancellation (full, partial, etc.)
- `requestTimestamp`: ISO-8601 timestamp - Cancellation request time
- `requestedBy`: string - User or system requesting cancellation

**Optional Fields**:
- `businessJustification`: string - Business reason for cancellation
- `urgencyLevel`: string - Cancellation urgency classification
- `approvalOverrides`: array - Pre-approved authorization overrides

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "cancellation" - Tags for cancellation processing nodes
- `responseTimeoutMs`: 45000 - Maximum processing time (45 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-223456789abe" - Process parameter reference

**Context Data**:
- Cancellation authorization matrix
- Impact assessment rules
- Regulatory reporting requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Trade",
    "processingTimestamp": "2024-01-15T11:30:00Z",
    "cancellationResults": {
      "cancellationApplied": true,
      "originalStatus": "active",
      "cancelledStatus": "cancelled",
      "cancellationTimestamp": "2024-01-15T11:30:00Z",
      "impactAssessment": {
        "positionImpact": "significant",
        "riskImpact": "moderate",
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
  "errorCode": "CANCELLATION_ERROR",
  "errorMessage": "Trade cancellation processing failed",
  "details": {
    "validationErrors": ["Cancellation exceeds authorization limits"],
    "businessRuleViolations": ["Trade cannot be cancelled after settlement"],
    "systemErrors": ["Position service unavailable"]
  }
}
```

**Side Effects**:
- Updates Trade entity status to cancelled
- Creates cancellation audit trail
- Triggers position recalculation
- Generates regulatory cancellation report
- Publishes TradeCancelled event

## 4. Business Logic
**Processing Steps**:
1. Validate cancellation request against business rules
2. Check authorization limits and approval requirements
3. Assess impact on positions, risk, and reporting
4. Apply cancellation to trade entity
5. Update related entities and calculations
6. Generate regulatory cancellation notifications
7. Create comprehensive audit trail

**Business Rules**:
- **Authorization Limits**: Cancellations must be within authorized limits
- **Timing Restrictions**: Cancellations subject to settlement timing rules
- **Regulatory Compliance**: Cancellations must comply with regulatory requirements
- **Impact Thresholds**: Significant cancellations require additional approvals
- **Market Restrictions**: Cancellations subject to market timing restrictions

**Algorithms**:
- Impact assessment using position and risk calculation engines
- Authorization validation using role-based permission matrix
- Regulatory requirement determination using rule engine

## 5. Validation Rules
**Pre-processing Validations**:
- **Cancellation Validity**: Cancellation request contains valid justification
- **Trade Status**: Trade in cancellable status
- **Authorization**: Requestor has appropriate authorization

**Post-processing Validations**:
- **Status Update**: Trade status updated correctly to cancelled
- **Impact Assessment**: Impact assessment completed successfully
- **Regulatory Compliance**: Cancellation meets regulatory requirements

**Data Quality Checks**:
- **Cancellation Completeness**: All required cancellation fields present
- **Justification Validation**: Business justification adequate and valid
- **Regulatory Compliance**: Cancellation meets regulatory requirements

## 6. Error Handling
**Error Categories**:
- **CANCELLATION_ERROR**: Cancellation processing logic failures
- **VALIDATION_ERROR**: Cancellation validation failures
- **AUTHORIZATION_ERROR**: Insufficient authorization for cancellation
- **SYSTEM_ERROR**: External system or calculation failures
- **TIMEOUT_ERROR**: Cancellation processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient system errors (max 3 retries)
- Rollback mechanism for partial cancellation failures
- Manual review queue for authorization failures

**Error Propagation**:
- Cancellation errors trigger transition to cancellation-failed state
- Error details stored for manual review and reprocessing
- Critical errors escalated to trading operations team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 35 seconds (95th percentile)
- **Throughput**: 15 cancellations per second
- **Availability**: 99.9% uptime during trading hours

**Resource Requirements**:
- **CPU**: High intensity for impact assessment and calculations
- **Memory**: 1GB per concurrent cancellation
- **I/O**: High for trade data access and updates

**Scalability**:
- Horizontal scaling through cancellation processing nodes
- Performance varies with cancellation complexity and impact scope
- Parallel processing for independent cancellation components

## 8. Dependencies
**Internal Dependencies**:
- **Authorization Service**: Cancellation authorization and approval
- **Position Service**: Position impact assessment and recalculation
- **Risk Service**: Risk impact assessment
- **Regulatory Service**: Regulatory compliance validation

**External Dependencies**:
- **Market Data Service**: Current market data for impact assessment (SLA: 99.5% availability, 2s response)
- **Regulatory Reporting Service**: Cancellation reporting requirements (SLA: 99.9% availability, 5s response)

**Data Dependencies**:
- Trade master data with complete economics
- Cancellation authorization matrix
- Position and risk calculation parameters
- Regulatory reporting rules and templates

## 9. Configuration Parameters
**Required Configuration**:
- `authorizationRequired`: boolean - Require authorization validation - Default: true
- `impactAssessmentEnabled`: boolean - Enable impact assessment - Default: true
- `regulatoryReportingEnabled`: boolean - Enable regulatory reporting - Default: true

**Optional Configuration**:
- `maxCancellationValue`: decimal - Maximum cancellation value - Default: 100000000
- `autoApprovalThreshold`: decimal - Auto-approval threshold - Default: 1000000
- `auditTrailEnabled`: boolean - Enable detailed audit trail - Default: true

**Environment-Specific Configuration**:
- Development: Relaxed authorization, mock impact assessment
- Production: Full authorization, live impact assessment

## 10. Integration Points
**API Contracts**:
- Input: Trade entity with cancellation request details
- Output: Cancellation results with impact assessment

**Data Exchange Formats**:
- **JSON**: Cancellation request and results
- **XML**: Regulatory cancellation reporting format

**Event Publishing**:
- **TradeCancelled**: Published on successful cancellation with details
- **CancellationFailed**: Published on cancellation failure with error details
- **ImpactAssessmentCompleted**: Published with impact assessment results

**Event Consumption**:
- **CancellationRequested**: Triggers cancellation processing
- **AuthorizationUpdated**: Updates authorization configuration
- **RegulatoryRulesUpdated**: Updates regulatory compliance rules
