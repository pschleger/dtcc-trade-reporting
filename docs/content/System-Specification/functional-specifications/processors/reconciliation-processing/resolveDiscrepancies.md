# resolveDiscrepancies Processor Specification

## 1. Component Overview
**Component Name**: resolveDiscrepancies
**Component Type**: CyodaProcessor
**Business Domain**: Reconciliation Processing
**Purpose**: Executes resolution actions for identified and investigated discrepancies to achieve position reconciliation
**Workflow Context**: ReconciliationWorkflow (resolution state)

## 2. Input Specifications
**Entity Type**: DiscrepancyResolution
**Required Fields**:
- `resolutionId`: string - Unique identifier for the resolution process
- `discrepancyIds`: array - List of discrepancy IDs to be resolved
- `resolutionActions`: array - Specific actions to be taken for resolution
- `resolutionPriority`: string - Priority level ("CRITICAL", "HIGH", "MEDIUM", "LOW")
- `approvalStatus`: string - Approval status for resolution actions

**Optional Fields**:
- `resolutionDeadline`: ISO-8601 timestamp - Deadline for resolution completion
- `impactAssessment`: object - Assessment of resolution impact on positions and risk
- `rollbackPlan`: object - Plan for rolling back resolution if issues arise
- `resolutionNotes`: string - Additional notes or context for resolution

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "resolution" - Tags for resolution processing nodes
- `responseTimeoutMs`: 180000 - Maximum processing time (3 minutes)
- `processParamId`: "01932b4e-7890-7123-8456-423456789stu" - Process parameter reference

**Context Data**:
- Resolution procedures and approval workflows
- Position adjustment mechanisms and validation rules
- Impact assessment tools and risk management controls

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "DiscrepancyResolution",
    "processingTimestamp": "2024-01-15T17:30:00Z",
    "resolutionResults": {
      "resolutionStatus": "COMPLETED",
      "resolvedDiscrepancies": 15,
      "adjustmentsMade": {
        "positionAdjustments": 8,
        "valuationCorrections": 5,
        "dataCorrections": 2
      },
      "impactSummary": {
        "netPositionChange": 1250000.00,
        "riskImpact": "MINIMAL",
        "regulatoryImpact": "NONE"
      }
    },
    "nextActions": ["VALIDATE_RESOLUTION", "UPDATE_REPORTING"]
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "RESOLUTION_FAILED",
  "errorMessage": "Failed to resolve discrepancies",
  "details": {
    "resolutionId": "RES-20240115-001",
    "failureReason": "Insufficient approval authority",
    "retryable": false
  }
}
```

**Side Effects**:
- Position data updated with resolution adjustments
- Trade records corrected where applicable
- Resolution audit trail created for compliance
- Risk metrics recalculated after position changes

## 4. Business Logic
**Processing Steps**:
1. Validate resolution actions and required approvals
2. Assess impact of proposed resolution on positions and risk
3. Execute approved resolution actions in sequence
4. Apply position adjustments and data corrections
5. Validate resolution effectiveness and completeness
6. Update affected systems and downstream processes
7. Generate resolution confirmation and audit trail

**Business Rules**:
- **Approval Requirements**: Resolution actions must have appropriate approval based on impact
- **Impact Limits**: Position adjustments must stay within predefined risk limits
- **Validation Requirements**: All resolutions must be validated before finalization
- **Audit Trail**: Complete audit trail required for all resolution actions
- **Rollback Capability**: All resolutions must be reversible if issues are discovered

**Algorithms**:
- Impact assessment calculation for position and risk changes
- Validation algorithms for resolution effectiveness
- Rollback sequence generation for resolution reversal

## 5. Validation Rules
**Pre-processing Validations**:
- **Approval Validation**: All resolution actions must have required approvals
- **Impact Assessment**: Resolution impact must be within acceptable limits
- **Action Feasibility**: All resolution actions must be technically feasible
- **Dependency Check**: Resolution dependencies must be satisfied

**Post-processing Validations**:
- **Resolution Completeness**: All targeted discrepancies must be addressed
- **Position Consistency**: Resulting positions must be internally consistent
- **Risk Compliance**: Post-resolution risk metrics must be within limits

**Data Quality Checks**:
- **Adjustment Accuracy**: All position adjustments must be mathematically correct
- **Data Integrity**: All data corrections must maintain referential integrity

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Resolution validation failures or insufficient approvals
- **ADJUSTMENT_ERROR**: Position adjustment calculation or application failures
- **APPROVAL_ERROR**: Approval workflow failures or authorization issues
- **IMPACT_ERROR**: Resolution impact exceeds acceptable limits
- **SYSTEM_ERROR**: Technical system failures or data consistency issues

**Error Recovery**:
- **Rollback Logic**: Automatic rollback of partial resolutions on failure
- **Approval Retry**: Retry approval workflow for transient authorization failures
- **Manual Intervention**: Escalate complex resolution issues to operations teams

**Error Propagation**:
- **Workflow Notification**: Notify reconciliation workflow of resolution status
- **Risk Alert**: Generate risk alerts for failed high-impact resolutions
- **Audit Notification**: Record all resolution attempts and outcomes

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 120 seconds (95th percentile) for standard resolutions
- **Resolution Completion**: 2 hours for critical discrepancies, 8 hours for others
- **Availability**: 99.95% uptime during business hours

**Resource Requirements**:
- **CPU**: Moderate for impact calculations and validation processing
- **Memory**: 1GB per concurrent resolution for complex position adjustments
- **I/O**: Database updates for position adjustments and audit trail

**Scalability**:
- **Parallel Processing**: Support multiple concurrent resolutions with dependency management
- **Resource Allocation**: Dynamic resource allocation based on resolution complexity
- **Performance Monitoring**: Track resolution times and success rates

## 8. Dependencies
**Internal Dependencies**:
- **Position Management System**: Position data updates and recalculation
- **Approval Workflow System**: Resolution approval and authorization
- **Risk Management System**: Impact assessment and risk limit validation
- **Audit System**: Resolution audit trail and compliance recording

**External Dependencies**:
- **Counterparty Systems**: Notification of position adjustments (99.0% SLA)
- **Regulatory Systems**: Reporting of material position changes (99.9% SLA)

**Data Dependencies**:
- **Resolution Procedures**: Standardized resolution procedures and workflows
- **Approval Matrix**: Authorization matrix for different resolution types and impacts

## 9. Configuration Parameters
**Required Configuration**:
- `maxPositionAdjustment`: decimal - Maximum allowed position adjustment - Default: 10000000.00
- `approvalRequired`: boolean - Require approval for all resolutions - Default: true
- `impactThresholds`: object - Thresholds for different impact levels
- `rollbackEnabled`: boolean - Enable automatic rollback on failure - Default: true

**Optional Configuration**:
- `parallelResolutionEnabled`: boolean - Enable parallel resolution processing - Default: false
- `validationLevel`: string - Level of validation ("BASIC", "STANDARD", "COMPREHENSIVE") - Default: "STANDARD"
- `auditDetailLevel`: string - Level of audit detail - Default: "DETAILED"
- `notificationEnabled`: boolean - Enable resolution notifications - Default: true

**Environment-Specific Configuration**:
- **Development**: Relaxed limits with detailed logging and validation
- **Production**: Strict limits with complete approval and audit requirements

## 10. Integration Points
**API Contracts**:
- **Input**: DiscrepancyResolution entity with resolution actions and approval status
- **Output**: Resolution results with adjustments made and impact summary

**Data Exchange Formats**:
- **Resolution Format**: Standardized resolution action format with approval details
- **Adjustment Format**: Position adjustment format with validation and audit information

**Event Publishing**:
- **ResolutionStarted**: Published when resolution process begins
- **ResolutionCompleted**: Published when resolution successfully completes
- **ResolutionFailed**: Published when resolution fails with error details and rollback status

**Event Consumption**:
- **DiscrepancyInvestigated**: Triggers resolution process for investigated discrepancies
- **ApprovalReceived**: Handles approval notifications for pending resolutions
