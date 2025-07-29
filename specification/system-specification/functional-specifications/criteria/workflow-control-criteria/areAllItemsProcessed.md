# areAllItemsProcessed Criterion Specification

## 1. Component Overview
**Component Name**: areAllItemsProcessed
**Component Type**: CyodaCriterion
**Business Domain**: Workflow Control
**Purpose**: Evaluates whether all items in a batch processing operation have completed processing
**Workflow Context**: BatchProcessingWorkflow (processing state transitions to completion)

## 2. Input Parameters
**Entity Type**: ProcessingBatch
**Required Fields**:
- `batchId`: string - Unique batch identifier
- `totalItemCount`: integer - Total number of items in batch
- `processedItemCount`: integer - Number of items completed processing
- `processingStatus`: string - Current batch processing status
- `itemStatuses`: array - Individual item processing statuses

**Optional Fields**:
- `failedItemCount`: integer - Number of items that failed processing
- `skippedItemCount`: integer - Number of items skipped during processing
- `processingProgress`: decimal - Processing progress percentage (0.0-1.0)

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "batch-control" - Tags for batch control evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time (5 seconds)
- `context`: "batch-completion-check" - Evaluation context identifier

**Evaluation Context**:
- Batch completion criteria
- Item status definitions
- Processing tolerance thresholds

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF processedItemCount + failedItemCount + skippedItemCount >= totalItemCount AND
   processingStatus in ["COMPLETED", "FINISHED"] AND
   all itemStatuses in ["COMPLETED", "FAILED", "SKIPPED"] THEN
    RETURN true
ELSE IF processingStatus == "PROCESSING" AND
        processedItemCount < totalItemCount THEN
    RETURN false
ELSE IF processingStatus in ["FAILED", "CANCELLED"] THEN
    RETURN false
ELSE
    RETURN false (default to safe state)
```

**Boolean Logic**:
- Primary evaluation based on item count completion
- Secondary evaluation considers processing status
- Tertiary evaluation validates individual item statuses
- Default behavior for ambiguous processing states

**Calculation Methods**:
- Item count aggregation and comparison
- Processing status validation
- Individual item status verification

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Count Completion**: processedItemCount + failedItemCount + skippedItemCount >= totalItemCount
- **Status Completion**: processingStatus indicates completion
- **Item Status**: All items have final status (completed, failed, or skipped)
- **Progress Complete**: processingProgress equals 1.0 (if available)

**Success Scenarios**:
- **Full Success**: All items processed successfully
- **Partial Success**: Some items processed, others failed or skipped within tolerance
- **Completion with Failures**: Processing complete despite some item failures

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **Incomplete Processing**: processedItemCount < totalItemCount
- **Active Processing**: processingStatus indicates ongoing processing
- **Pending Items**: Some items still have "PENDING" or "PROCESSING" status
- **Batch Failure**: processingStatus indicates batch-level failure

**Failure Scenarios**:
- **Processing Ongoing**: Batch still actively processing items
- **Stalled Processing**: Processing stopped but not all items completed
- **Batch Cancelled**: Processing cancelled before completion
- **System Failure**: Batch processing failed due to system issues

## 6. Edge Cases
**Boundary Conditions**:
- **Zero Item Batch**: Batch with no items to process
- **Count Mismatches**: Discrepancies between expected and actual item counts
- **Status Inconsistencies**: Processing status doesn't match item statuses
- **Concurrent Updates**: Batch status changing during evaluation

**Special Scenarios**:
- **Partial Batch Processing**: Only subset of items processed by design
- **Dynamic Batch Size**: Batch size changes during processing
- **Retry Scenarios**: Items reprocessed after initial failures

**Data Absence Handling**:
- Missing totalItemCount prevents proper evaluation
- Missing processedItemCount defaults to 0
- Missing itemStatuses defaults to incomplete processing

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 2 seconds (95th percentile)
- **Throughput**: 1000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for count comparison and status evaluation
- **Memory**: 64MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **Batch Processing Service**: Access to batch status and item counts
- **Item Tracking Service**: Individual item status tracking
- **Configuration Service**: Batch completion criteria and thresholds

**External Dependencies**:
- **Monitoring Service**: Batch processing progress tracking

**Data Dependencies**:
- Batch processing status definitions
- Item status classification rules
- Completion criteria configuration

## 9. Configuration
**Configurable Thresholds**:
- `completionTolerancePercent`: decimal - Acceptable completion percentage - Default: 1.0
- `allowPartialCompletion`: boolean - Allow partial batch completion - Default: false
- `maxFailureRatePercent`: decimal - Maximum acceptable failure rate - Default: 0.05

**Evaluation Parameters**:
- `strictCounting`: boolean - Enable strict item count validation - Default: true
- `validateItemStatuses`: boolean - Validate individual item statuses - Default: true
- `requireProgressComplete`: boolean - Require 100% progress indication - Default: false

**Environment-Specific Settings**:
- Development: Relaxed completion criteria, allow partial completion
- Production: Strict completion criteria, require full completion

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required batch data not accessible
- **COUNT_MISMATCH**: Item count inconsistencies detected
- **STATUS_INCONSISTENCY**: Processing status conflicts with item statuses
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false on data unavailability (assume incomplete)
- Retry mechanism for transient data access errors (max 2 retries)
- Fallback to basic count comparison for complex scenarios

**Error Propagation**:
- Evaluation errors logged with batch context
- Failed evaluations trigger manual review
- Critical errors escalated to batch operations team
