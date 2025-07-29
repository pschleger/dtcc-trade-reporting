# applyAmendment Processor Specification

## 1. Component Overview
**Component Name**: applyAmendment
**Component Type**: CyodaProcessor
**Business Domain**: Amendment Processing
**Purpose**: Applies validated and approved trade amendments to trade entities with complete audit trail and downstream system notifications
**Workflow Context**: AmendmentWorkflow (applying state)

## 2. Input Specifications
**Entity Type**: TradeAmendment
**Required Fields**:
- `amendmentId`: string - Unique amendment request identifier
- `originalTradeId`: string - Original trade identifier being amended
- `amendmentFields`: object - Approved field changes to apply
- `approvalReference`: string - Approval workflow reference
- `effectiveDate`: ISO-8601 date - Date when amendment becomes effective
- `appliedBy`: string - User or system applying the amendment

**Optional Fields**:
- `backupRequired`: boolean - Whether to create trade backup before amendment
- `notificationList`: array - List of systems/users to notify
- `rollbackPlan`: object - Rollback plan in case of failure
- `validationOverride`: boolean - Override validation for emergency amendments

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "amendment-application" - Tags for amendment application nodes
- `responseTimeoutMs`: 60000 - Maximum processing time (60 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789pqr" - Process parameter reference

**Context Data**:
- Original trade data for backup
- Amendment approval details
- Audit trail requirements
- Downstream system configurations

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeAmendment",
    "processingTimestamp": "2024-01-15T15:30:00Z",
    "amendmentResults": {
      "amendmentApplied": true,
      "originalTradeBackup": "TRADE_BACKUP_2024_001",
      "amendedTradeId": "TRD-001",
      "fieldsChanged": ["notionalAmount", "maturityDate"],
      "effectiveTimestamp": "2024-01-15T15:30:00Z"
    },
    "auditTrail": {
      "amendmentAuditId": "AMD_AUDIT_2024_001",
      "originalValues": {
        "notionalAmount": 50000000.00,
        "maturityDate": "2024-12-15"
      },
      "newValues": {
        "notionalAmount": 52000000.00,
        "maturityDate": "2025-01-15"
      },
      "approvalReference": "APPROVAL_2024_001",
      "appliedBy": "trader001"
    },
    "downstreamNotifications": {
      "systemsNotified": ["RISK_SYSTEM", "POSITION_SYSTEM", "REPORTING_SYSTEM"],
      "notificationsSent": 3,
      "notificationStatus": "SUCCESS"
    },
    "positionImpact": {
      "positionsAffected": 2,
      "recalculationTriggered": true,
      "recalculationJobId": "RECALC_2024_001"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "AMENDMENT_APPLICATION_FAILED",
  "errorMessage": "Amendment application failed due to trade lock conflict",
  "details": {
    "amendmentId": "AMD-001",
    "originalTradeId": "TRD-001",
    "conflictReason": "Trade currently locked by another process",
    "lockOwner": "POSITION_CALCULATION",
    "retryRecommended": true
  }
}
```

**Side Effects**:
- Updates original trade entity with amended values
- Creates backup of original trade data
- Generates comprehensive audit trail
- Triggers position recalculation for affected positions
- Publishes amendment applied events to downstream systems

## 4. Business Logic
**Processing Steps**:
1. Validate amendment application request and approval status
2. Acquire exclusive lock on original trade entity
3. Create backup of original trade data
4. Validate current trade state against amendment requirements
5. Apply amendment fields to trade entity
6. Validate amended trade for data consistency
7. Update trade entity with amended values and metadata
8. Create comprehensive audit trail entry
9. Trigger downstream system notifications and recalculations
10. Release trade lock and publish completion events

**Business Rules**:
- **Approval Verification**: Amendment must have valid approval before application
- **Trade Lock Management**: Exclusive lock required during amendment application
- **Backup Requirements**: Original trade data must be backed up before changes
- **Audit Trail**: Complete audit trail required for all amendments
- **Effective Date**: Amendment effective date must be valid business date

**Algorithms**:
- Trade locking mechanism to prevent concurrent modifications
- Field-level amendment application with validation
- Audit trail generation with before/after value tracking
- Downstream notification routing based on amendment impact

## 5. Validation Rules
**Pre-processing Validations**:
- **Amendment Approval**: Amendment must have valid approval reference
- **Trade Availability**: Original trade must be available and not locked
- **Effective Date**: Amendment effective date must be valid
- **Field Consistency**: Amendment fields must be consistent with trade type

**Post-processing Validations**:
- **Trade Integrity**: Amended trade must maintain data integrity
- **Business Rule Compliance**: Amended trade must comply with business rules
- **Audit Trail Completeness**: Audit trail must capture all changes
- **Downstream Notification**: All required systems must be notified

**Data Quality Checks**:
- **Field Value Validation**: All amended field values must be valid
- **Cross-field Consistency**: Related fields must remain consistent
- **Reference Data Validation**: All reference data must be current

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Amendment application validation failures
- **LOCK_CONFLICT_ERROR**: Trade locking conflicts
- **SYSTEM_ERROR**: Technical system failures
- **NOTIFICATION_ERROR**: Downstream notification failures
- **TIMEOUT_ERROR**: Amendment application timeout exceeded

**Error Recovery**:
- Automatic retry for transient lock conflicts
- Rollback mechanism for partial application failures
- Manual intervention procedures for critical failures

**Error Propagation**:
- Detailed error information provided to calling workflows
- Amendment status updated with failure details
- Error notifications sent to operations teams

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 60000 milliseconds (95th percentile)
- **Throughput**: 150 amendments per hour
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Moderate computational requirements for data processing
- **Memory**: 256MB for trade data and audit trail processing
- **I/O**: Database access for trade updates and audit trail creation

**Scalability**:
- Horizontal scaling through amendment application node distribution
- Database optimization for high-volume trade updates
- Efficient locking mechanisms to minimize contention

## 8. Dependencies
**Internal Dependencies**:
- **TradeService**: Trade data retrieval and updates
- **LockingService**: Trade entity locking and unlocking
- **AuditService**: Audit trail creation and management
- **NotificationService**: Downstream system notifications
- **PositionService**: Position recalculation triggering

**External Dependencies**:
- **Downstream Systems**: Risk, Position, and Reporting systems (SLA: 99.5%)
- **Database**: Trade data storage and retrieval (SLA: 99.9%)

**Data Dependencies**:
- Original trade data
- Amendment approval data
- Audit trail templates
- Downstream system configurations

## 9. Configuration Parameters
**Required Configuration**:
- `amendmentTimeoutMs`: integer - Maximum application time - Default: 60000
- `lockTimeoutMs`: integer - Trade lock timeout - Default: 30000
- `enableBackup`: boolean - Enable trade backup - Default: true
- `auditDetailLevel`: string - Audit trail detail level - Default: "FULL"

**Optional Configuration**:
- `enableRollback`: boolean - Enable automatic rollback - Default: true
- `notificationRetries`: integer - Notification retry attempts - Default: 3
- `validateAfterApplication`: boolean - Validate after application - Default: true

**Environment-Specific Configuration**:
- **Development**: Reduced timeouts and simplified validation
- **Production**: Full timeouts and comprehensive validation

## 10. Integration Points
**API Contracts**:
- **Input**: Amendment application request with approval and effective date
- **Output**: Amendment application results with audit trail and notifications

**Data Exchange Formats**:
- **JSON**: Primary data exchange format for amendment application
- **XML**: Alternative format for regulatory integration

**Event Publishing**:
- **AmendmentApplied**: Published when amendment application completes successfully
- **AmendmentApplicationFailed**: Published when application fails
- **TradeUpdated**: Published when trade entity is successfully updated

**Event Consumption**:
- **AmendmentApproved**: Triggers amendment application process
- **TradeUnlocked**: Enables amendment application when trade becomes available
