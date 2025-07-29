# hasTradeUpdates Criterion Specification

## 1. Component Overview
**Component Name**: hasTradeUpdates
**Component Type**: CyodaCriterion
**Business Domain**: Trade Management
**Purpose**: Evaluates whether a trade entity has pending updates, amendments, or modifications that require processing
**Workflow Context**: Trade management and amendment processing workflows requiring detection of pending trade changes

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `lastModifiedTime`: ISO-8601 timestamp - Last modification timestamp
- `updateStatus`: string - Current update status (PENDING, PROCESSING, COMPLETED)
- `versionNumber`: integer - Current trade version number

**Optional Fields**:
- `pendingAmendments`: array - List of pending amendment identifiers
- `pendingCorrections`: array - List of pending correction identifiers
- `updateType`: string - Type of pending update (AMENDMENT, CORRECTION, CANCELLATION)
- `updatePriority`: string - Priority level of pending updates

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "trade-updates" - Tags for trade update evaluation nodes
- `responseTimeoutMs`: 2500 - Maximum evaluation time (2.5 seconds)
- `context`: "trade-update-check" - Evaluation context identifier

**Evaluation Context**:
- Trade lifecycle management
- Amendment and correction tracking
- Update priority and scheduling

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF tradeId == null OR tradeId.isEmpty() THEN
    RETURN false
END IF

hasPendingUpdates = false

IF updateStatus == "PENDING" OR updateStatus == "PROCESSING" THEN
    hasPendingUpdates = true
END IF

IF pendingAmendments != null AND pendingAmendments.size() > 0 THEN
    hasPendingUpdates = true
END IF

IF pendingCorrections != null AND pendingCorrections.size() > 0 THEN
    hasPendingUpdates = true
END IF

IF updateType != null AND 
   (updateType == "AMENDMENT" OR updateType == "CORRECTION" OR updateType == "CANCELLATION") THEN
    hasPendingUpdates = true
END IF

RETURN hasPendingUpdates
```

**Boolean Logic**:
- Primary evaluation checks update status for pending or processing states
- Secondary evaluation examines pending amendment and correction lists
- Tertiary evaluation considers update type indicators
- Comprehensive check across multiple update indicators
- Trade identifier validation for context

**Calculation Methods**:
- Update status enumeration validation
- Pending list size calculation and validation
- Update type classification and validation
- Version number comparison for change detection

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Pending Status**: updateStatus equals "PENDING" or "PROCESSING"
- **Pending Amendments**: pendingAmendments list contains items
- **Pending Corrections**: pendingCorrections list contains items
- **Update Type Set**: updateType indicates pending changes
- **Valid Trade ID**: tradeId is present and valid

**Success Scenarios**:
- **Amendment Pending**: Trade has pending amendment requests
- **Correction Pending**: Trade has pending correction requests
- **Cancellation Pending**: Trade has pending cancellation request
- **Multiple Updates**: Trade has multiple types of pending updates
- **Processing Updates**: Trade updates currently being processed

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Updates**: updateStatus equals "COMPLETED" or "NONE"
- **Empty Lists**: pendingAmendments and pendingCorrections are empty
- **No Update Type**: updateType is null or indicates no changes
- **Invalid Trade ID**: tradeId is null or empty
- **Current Version**: No version changes detected

**Failure Scenarios**:
- **Up to Date**: Trade has no pending updates
- **Completed Updates**: All updates have been processed
- **Clean State**: Trade in stable state with no changes
- **Invalid Trade**: Trade identifier not valid
- **No Changes**: No modifications detected

## 6. Edge Cases
**Boundary Conditions**:
- **Empty Update Lists**: Pending lists exist but are empty
- **Null Update Fields**: Update fields are null vs empty
- **Version Conflicts**: Version number inconsistencies
- **Concurrent Updates**: Multiple simultaneous update operations

**Special Scenarios**:
- **Partial Updates**: Some updates completed, others pending
- **Failed Updates**: Updates that failed and need retry
- **Bulk Updates**: Updates as part of batch processing
- **Emergency Updates**: High priority urgent updates
- **System Recovery**: Updates pending after system restart

**Data Absence Handling**:
- Missing tradeId defaults to false evaluation
- Missing updateStatus defaults to "NONE"
- Missing pendingAmendments defaults to empty list
- Missing pendingCorrections defaults to empty list

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 300 milliseconds (95th percentile)
- **Throughput**: 4000 evaluations per second
- **Availability**: 99.9% uptime

**Resource Requirements**:
- **CPU**: Low intensity for list processing and status checks
- **Memory**: 12MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Trade Service**: Trade entity management and status tracking
- **Amendment Service**: Amendment tracking and status management
- **Correction Service**: Correction tracking and status management

**External Dependencies**:
- **Database**: Trade update status and pending lists
- **Audit Service**: Update tracking and version management

**Data Dependencies**:
- Trade entity metadata
- Amendment tracking data
- Correction tracking data
- Update status information

## 9. Configuration
**Configurable Thresholds**:
- `includeProcessingStatus`: boolean - Include PROCESSING status as pending - Default: true
- `checkAmendmentList`: boolean - Check pending amendments list - Default: true
- `checkCorrectionList`: boolean - Check pending corrections list - Default: true

**Evaluation Parameters**:
- `validateTradeIdFormat`: boolean - Enable trade ID format validation - Default: true
- `requireUpdateType`: boolean - Require update type for positive evaluation - Default: false
- `checkVersionChanges`: boolean - Check version number changes - Default: false

**Environment-Specific Settings**:
- Development: Include all update types for testing
- Production: Standard update detection for operational processing

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required trade fields not accessible
- **INVALID_STATUS**: Update status field validation errors
- **LIST_ERROR**: Pending list processing failures
- **SERVICE_ERROR**: Trade service access failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on data errors
- Fallback to basic status check on service errors
- Skip list checks on list processing errors
- Retry mechanism for service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with trade context
- Failed evaluations trigger manual review
- Service errors escalated to operations team
- Data integrity errors reported to development team
