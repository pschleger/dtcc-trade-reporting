# executeCancellation Processor Specification

## 1. Component Overview
**Component Name**: executeCancellation
**Component Type**: CyodaProcessor
**Business Domain**: Cancellation Processing
**Purpose**: Executes authorized trade cancellations with complete audit trail, position updates, and downstream system notifications
**Workflow Context**: CancellationWorkflow (execution state)

## 2. Input Specifications
**Entity Type**: TradeCancellation
**Required Fields**:
- `cancellationId`: string - Unique cancellation request identifier
- `tradeId`: string - Trade identifier to be cancelled
- `authorizationReference`: string - Authorization reference from approval workflow
- `cancellationType`: string - Type of cancellation (FULL, PARTIAL, NOVATION)
- `effectiveDate`: ISO-8601 date - Date when cancellation becomes effective
- `executedBy`: string - User or system executing the cancellation

**Optional Fields**:
- `partialAmount`: decimal - Amount for partial cancellation
- `replacementTrade`: object - Replacement trade details for novation
- `backupRequired`: boolean - Whether to create trade backup before cancellation
- `notificationList`: array - List of systems/users to notify

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "cancellation-execution" - Tags for cancellation execution nodes
- `responseTimeoutMs`: 90000 - Maximum processing time (90 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789ghij" - Process parameter reference

**Context Data**:
- Original trade data for backup
- Authorization and approval details
- Position recalculation requirements
- Downstream system configurations

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeCancellation",
    "processingTimestamp": "2024-01-15T15:00:00Z",
    "cancellationResults": {
      "cancellationExecuted": true,
      "originalTradeBackup": "TRADE_BACKUP_2024_002",
      "cancelledTradeId": "TRD-001",
      "cancellationType": "FULL",
      "effectiveTimestamp": "2024-01-15T15:00:00Z"
    },
    "auditTrail": {
      "cancellationAuditId": "CANC_AUDIT_2024_001",
      "originalTradeData": {
        "notionalAmount": 50000000.00,
        "maturityDate": "2024-12-15",
        "tradeStatus": "ACTIVE"
      },
      "cancellationDetails": {
        "cancellationReason": "Counterparty request",
        "authorizationReference": "AUTH-CANC-2024-001",
        "executedBy": "trader001"
      }
    },
    "positionImpact": {
      "positionsAffected": 3,
      "recalculationTriggered": true,
      "recalculationJobId": "RECALC_2024_002",
      "positionUpdates": [
        {
          "positionId": "POS-001",
          "notionalChange": -50000000.00,
          "marketValueChange": -49750000.00
        }
      ]
    },
    "downstreamNotifications": {
      "systemsNotified": ["RISK_SYSTEM", "POSITION_SYSTEM", "SETTLEMENT_SYSTEM"],
      "notificationsSent": 3,
      "notificationStatus": "SUCCESS"
    },
    "regulatoryImpact": {
      "reportingRequired": true,
      "reportingDeadline": "2024-01-16T23:59:59Z",
      "reportingJobId": "RPT_CANC_2024_001"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "CANCELLATION_EXECUTION_FAILED",
  "errorMessage": "Cancellation execution failed due to trade lock conflict",
  "details": {
    "cancellationId": "CANC-001",
    "tradeId": "TRD-001",
    "conflictReason": "Trade currently locked by settlement process",
    "lockOwner": "SETTLEMENT_SYSTEM",
    "retryRecommended": true
  }
}
```

**Side Effects**:
- Updates or removes trade entity based on cancellation type
- Creates backup of original trade data
- Generates comprehensive audit trail
- Triggers position recalculation for affected positions
- Publishes cancellation events to downstream systems

## 4. Business Logic
**Processing Steps**:
1. Validate cancellation execution request and authorization
2. Acquire exclusive lock on trade entity
3. Create backup of original trade data
4. Validate current trade state against cancellation requirements
5. Execute cancellation based on type (full, partial, novation)
6. Update or remove trade entity as appropriate
7. Create comprehensive audit trail entry
8. Trigger position recalculation for affected positions
9. Notify downstream systems of cancellation
10. Release trade lock and publish completion events

**Business Rules**:
- **Authorization Verification**: Cancellation must have valid authorization before execution
- **Trade Lock Management**: Exclusive lock required during cancellation execution
- **Backup Requirements**: Original trade data must be backed up before cancellation
- **Audit Trail**: Complete audit trail required for all cancellations
- **Effective Date**: Cancellation effective date must be valid business date

**Algorithms**:
- Trade locking mechanism to prevent concurrent modifications
- Cancellation type-specific execution logic
- Position impact calculation for recalculation triggering
- Audit trail generation with complete change tracking

## 5. Validation Rules
**Pre-processing Validations**:
- **Authorization Validity**: Cancellation must have valid authorization reference
- **Trade Availability**: Trade must be available and not locked by other processes
- **Effective Date**: Cancellation effective date must be valid
- **Cancellation Type**: Cancellation type must be appropriate for trade

**Post-processing Validations**:
- **Cancellation Completeness**: Cancellation must be fully executed
- **Audit Trail Completeness**: Audit trail must capture all changes
- **Position Consistency**: Position updates must be consistent with cancellation
- **Downstream Notification**: All required systems must be notified

**Data Quality Checks**:
- **Data Integrity**: All cancellation data must maintain integrity
- **Backup Verification**: Trade backup must be complete and accessible
- **Notification Delivery**: All notifications must be successfully delivered

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Cancellation execution validation failures
- **LOCK_CONFLICT_ERROR**: Trade locking conflicts
- **SYSTEM_ERROR**: Technical system failures
- **NOTIFICATION_ERROR**: Downstream notification failures
- **TIMEOUT_ERROR**: Cancellation execution timeout exceeded

**Error Recovery**:
- Automatic retry for transient lock conflicts
- Rollback mechanism for partial execution failures
- Manual intervention procedures for critical failures

**Error Propagation**:
- Detailed error information provided to calling workflows
- Cancellation status updated with failure details
- Error notifications sent to operations teams

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 90000 milliseconds (95th percentile)
- **Throughput**: 100 cancellations per hour
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Moderate computational requirements for data processing
- **Memory**: 256MB for trade data and audit trail processing
- **I/O**: Database access for trade updates and audit trail creation

**Scalability**:
- Horizontal scaling through cancellation execution node distribution
- Database optimization for high-volume trade updates
- Efficient locking mechanisms to minimize contention

## 8. Dependencies
**Internal Dependencies**:
- **TradeService**: Trade data retrieval and updates
- **LockingService**: Trade entity locking and unlocking
- **AuditService**: Audit trail creation and management
- **PositionService**: Position recalculation triggering
- **NotificationService**: Downstream system notifications

**External Dependencies**:
- **Downstream Systems**: Risk, Position, and Settlement systems (SLA: 99.5%)
- **Database**: Trade data storage and retrieval (SLA: 99.9%)

**Data Dependencies**:
- Original trade data
- Authorization and approval data
- Position calculation requirements
- Downstream system configurations

## 9. Configuration Parameters
**Required Configuration**:
- `cancellationTimeoutMs`: integer - Maximum execution time - Default: 90000
- `lockTimeoutMs`: integer - Trade lock timeout - Default: 30000
- `enableBackup`: boolean - Enable trade backup - Default: true
- `auditDetailLevel`: string - Audit trail detail level - Default: "FULL"

**Optional Configuration**:
- `enableRollback`: boolean - Enable automatic rollback - Default: true
- `notificationRetries`: integer - Notification retry attempts - Default: 3
- `validateAfterExecution`: boolean - Validate after execution - Default: true

**Environment-Specific Configuration**:
- **Development**: Reduced timeouts and simplified validation
- **Production**: Full timeouts and comprehensive validation

## 10. Integration Points
**API Contracts**:
- **Input**: Cancellation execution request with authorization and effective date
- **Output**: Cancellation execution results with audit trail and impact details

**Data Exchange Formats**:
- **JSON**: Primary data exchange format for cancellation execution
- **XML**: Alternative format for regulatory integration

**Event Publishing**:
- **CancellationExecuted**: Published when cancellation execution completes successfully
- **CancellationExecutionFailed**: Published when execution fails
- **TradeUpdated**: Published when trade entity is successfully updated or removed

**Event Consumption**:
- **CancellationAuthorized**: Triggers cancellation execution process
- **TradeUnlocked**: Enables cancellation execution when trade becomes available
