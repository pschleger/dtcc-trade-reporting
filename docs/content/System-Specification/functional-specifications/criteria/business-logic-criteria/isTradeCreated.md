# isTradeCreated Criterion Specification

## 1. Component Overview
**Component Name**: isTradeCreated
**Component Type**: CyodaCriterion
**Business Domain**: Trade Management
**Purpose**: Evaluates whether a trade entity has been successfully created and persisted in the system following trade confirmation processing
**Workflow Context**: Trade confirmation and trade management workflows requiring validation of successful trade entity creation

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `entityStatus`: string - Current entity status (CREATED, PENDING, FAILED)
- `creationTimestamp`: ISO-8601 timestamp - Trade entity creation time
- `persistenceStatus`: string - Database persistence status

**Optional Fields**:
- `validationStatus`: string - Trade validation status
- `confirmationId`: string - Original confirmation message identifier
- `counterpartyId`: string - Counterparty identifier for trade
- `productId`: string - Product identifier for trade

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "trade-creation" - Tags for trade creation evaluation nodes
- `responseTimeoutMs`: 2000 - Maximum evaluation time (2 seconds)
- `context`: "trade-creation-check" - Evaluation context identifier

**Evaluation Context**:
- Trade entity lifecycle management
- Database persistence validation
- Trade confirmation processing status

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF tradeId == null OR tradeId.isEmpty() THEN
    RETURN false
END IF

IF entityStatus == "CREATED" AND
   persistenceStatus == "PERSISTED" AND
   creationTimestamp != null THEN
    
    IF validationStatus != null THEN
        IF validationStatus == "VALID" OR validationStatus == "PASSED" THEN
            RETURN true
        ELSE
            RETURN false
        END IF
    ELSE
        RETURN true
    END IF
ELSE
    RETURN false
END IF
```

**Boolean Logic**:
- Primary evaluation checks entity status and persistence
- Secondary evaluation validates trade identifier presence
- Tertiary evaluation considers validation status if available
- Creation timestamp validation for audit trail
- Comprehensive status verification for trade lifecycle

**Calculation Methods**:
- Trade identifier validation and format checking
- Entity status enumeration validation
- Persistence status verification
- Creation timestamp validation

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Entity Created**: entityStatus equals "CREATED"
- **Persistence Complete**: persistenceStatus equals "PERSISTED"
- **Valid Trade ID**: tradeId is present and valid
- **Creation Timestamp**: creationTimestamp is present and valid
- **Validation Passed**: validationStatus is "VALID" or "PASSED" (if present)

**Success Scenarios**:
- **Standard Creation**: Trade entity created and persisted successfully
- **Validated Creation**: Trade entity created with successful validation
- **Confirmed Creation**: Trade entity created from confirmation processing
- **Complete Creation**: All creation steps completed successfully

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Entity Not Created**: entityStatus not equal to "CREATED"
- **Persistence Failed**: persistenceStatus not equal to "PERSISTED"
- **Missing Trade ID**: tradeId is null or empty
- **Invalid Timestamp**: creationTimestamp is null or invalid
- **Validation Failed**: validationStatus indicates failure

**Failure Scenarios**:
- **Creation Pending**: Trade entity creation still in progress
- **Creation Failed**: Trade entity creation encountered errors
- **Persistence Failed**: Database persistence operation failed
- **Validation Failed**: Trade validation checks failed
- **Incomplete Data**: Required trade data missing or invalid

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Creation**: Entity created but not fully persisted
- **Validation Pending**: Entity created but validation in progress
- **Duplicate Creation**: Multiple creation attempts for same trade
- **Concurrent Creation**: Simultaneous creation operations

**Special Scenarios**:
- **Amendment Creation**: Trade created from amendment processing
- **Correction Creation**: Trade created from error correction
- **Bulk Creation**: Trade created as part of batch processing
- **Recovery Creation**: Trade created during system recovery
- **Migration Creation**: Trade created during data migration

**Data Absence Handling**:
- Missing tradeId defaults to false evaluation
- Missing entityStatus defaults to false evaluation
- Missing persistenceStatus defaults to false evaluation
- Missing creationTimestamp defaults to false evaluation

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 200 milliseconds (95th percentile)
- **Throughput**: 5000 evaluations per second
- **Availability**: 99.9% uptime

**Resource Requirements**:
- **CPU**: Low intensity for status validation
- **Memory**: 8MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Entity Service**: Trade entity management and status tracking
- **Persistence Service**: Database persistence status validation
- **Validation Service**: Trade validation status checking

**External Dependencies**:
- **Database**: Trade entity persistence verification
- **Audit Service**: Creation timestamp and audit trail validation

**Data Dependencies**:
- Trade entity metadata
- Persistence status tracking
- Validation result data
- Creation audit trail

## 9. Configuration
**Configurable Thresholds**:
- `requiredValidationStatus`: boolean - Whether validation status is required - Default: false
- `strictTimestampValidation`: boolean - Enable strict timestamp validation - Default: true
- `allowPendingValidation`: boolean - Allow pending validation status - Default: false

**Evaluation Parameters**:
- `validateTradeIdFormat`: boolean - Enable trade ID format validation - Default: true
- `checkPersistenceIntegrity`: boolean - Enable persistence integrity checks - Default: true
- `requireAuditTrail`: boolean - Require audit trail - Default: true

**Environment-Specific Settings**:
- Development: Relaxed validation for testing
- Production: Strict validation for operational integrity

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required trade fields not accessible
- **INVALID_STATUS**: Status field validation errors
- **PERSISTENCE_ERROR**: Persistence status check failures
- **VALIDATION_ERROR**: Validation status check failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on data errors
- Fallback to basic status check on service errors
- Skip optional validations on service failures
- Retry mechanism for service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with trade context
- Failed evaluations trigger manual review
- Service errors escalated to operations team
- Data integrity errors reported to development team
