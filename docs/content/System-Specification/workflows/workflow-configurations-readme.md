# DTCC Regulatory Reporting System - Workflow Configurations

## Overview

This directory contains the complete set of workflow configurations for the DTCC regulatory reporting system. Each workflow is designed to comply with the Cyoda WorkflowConfiguration.json schema version 1.0 and implements the business requirements identified in the system specification.

## Workflow Configurations

### 1. TradeConfirmationWorkflow.json
**Purpose**: Process incoming FpML trade confirmation messages through validation and conversion to Trade entities.
- **Initial State**: `received`
- **Terminal States**: `processed`, `rejected`, `archived`
- **Key Features**: FpML validation, trade entity creation, error handling with manual review

### 2. TradeWorkflow.json
**Purpose**: Manage the complete lifecycle of trade entities from confirmation through maturity.
- **Initial State**: `new`
- **Terminal States**: `cancelled`, `matured`, `rejected`, `archived`
- **Key Features**: Business rule validation, amendment/cancellation support, maturity processing

### 3. PositionWorkflow.json
**Purpose**: Calculate and maintain position data through aggregation of trades and regulatory reporting.
- **Initial State**: `calculating`
- **Terminal States**: None (continuous lifecycle)
- **Key Features**: Real-time position calculation, reporting threshold evaluation, reconciliation

### 4. RegulatoryReportWorkflow.json
**Purpose**: Generate, validate, and submit regulatory reports to DTCC GTR.
- **Initial State**: `generating`
- **Terminal States**: `completed`, `archived`
- **Key Features**: DTCC validation, scheduled submission, retry mechanisms, acknowledgment processing

### 5. CounterpartyWorkflow.json
**Purpose**: Manage counterparty master data lifecycle including validation and regulatory compliance.
- **Initial State**: `draft`
- **Terminal States**: `archived`
- **Key Features**: LEI validation, duplicate checking, approval workflow, suspension/reactivation

### 6. ProductWorkflow.json
**Purpose**: Manage product reference data lifecycle including taxonomy compliance and calculation parameters.
- **Initial State**: `draft`
- **Terminal States**: `archived`
- **Key Features**: Taxonomy compliance, product committee approval, deprecation with grace period

### 7. AmendmentWorkflow.json
**Purpose**: Process trade amendments with validation and impact assessment.
- **Initial State**: `pending`
- **Terminal States**: `completed`, `archived`
- **Key Features**: Impact assessment, trade application, conditional reporting

### 8. CancellationWorkflow.json
**Purpose**: Process trade cancellations with authorization and impact reversal.
- **Initial State**: `requested`
- **Terminal States**: `completed`, `archived`
- **Key Features**: Authorization validation, impact reversal, conditional reporting

### 9. ReconciliationResultWorkflow.json
**Purpose**: Execute position reconciliation processes and manage discrepancy resolution.
- **Initial State**: `initiated`
- **Terminal States**: `completed-clean`, `completed-with-resolution`, `archived`
- **Key Features**: Data gathering, comparison logic, discrepancy investigation, escalation

### 10. ProcessingBatchWorkflow.json
**Purpose**: Coordinate batch processing operations across multiple entities.
- **Initial State**: `scheduled`
- **Terminal States**: `completed`, `failed`, `archived`
- **Key Features**: Scheduled execution, progress monitoring, error handling, retry mechanisms

## Schema Compliance

All workflow configurations strictly comply with the Cyoda WorkflowConfiguration.json schema:

### Required Properties ✅
- `version`: "1.0" for all workflows
- `name`: Unique workflow identifier
- `initialState`: Valid starting state
- `states`: Complete state machine definition

### Optional Properties ✅
- `desc`: Detailed workflow description
- `active`: true (all workflows active)
- `processParams`: Time UUID-based process parameters

### State Naming ✅
All state names follow schema requirements:
- Start with a letter
- Contain only letters, numbers, underscores, and hyphens
- No spaces or special characters

### Criteria Types ✅
All criteria use supported types as specified:
- **Function**: External business logic evaluation
- **Group**: Logical combinations (AND/OR)
- **Simple**: Avoided as per requirements

### Execution Modes ✅
Processors use appropriate execution modes:
- **SYNC**: Immediate validation and quick operations
- **ASYNC_NEW_TX**: Long-running operations and external integrations
- **ASYNC_SAME_TX**: Monitoring and progress tracking

## Process Parameters

Each workflow includes a `processParams` block with time UUID identifiers:
- **Time UUIDs**: All process parameter IDs use time-based UUIDs (01932b4e-xxxx-xxxx-xxxx-xxxxxxxxxxxx format)
- **Parameter Types**: Categorized by function (validation, processing, reporting, etc.)
- **Descriptions**: Clear descriptions of each parameter's purpose

## Manual vs Automated Transitions

### Manual Transitions
- User approval workflows (counterparty activation, product approval)
- Error recovery and manual review processes
- Amendment and cancellation requests
- Escalation and investigation processes

### Automated Transitions
- System validation and processing
- Scheduled operations and time-based triggers
- Error detection and retry mechanisms
- State progression based on business logic

## Error Handling

All workflows implement complete error handling:
- **Failure States**: Specific failure states for different error types
- **Manual Review**: Manual intervention points for complex issues
- **Retry Mechanisms**: Automatic retry with configurable limits
- **Notification**: Alert systems for operational teams

## Use Case Coverage

The workflows support all 11 business use cases identified in Task 2:
- UC-001 to UC-003: Trade processing workflows
- UC-004 to UC-005: Position management workflows
- UC-006 to UC-007: Regulatory reporting workflows
- UC-008 to UC-009: Reference data management workflows
- UC-010 to UC-011: Exception handling and compliance monitoring

## Validation Results

### Schema Validation ✅
- All workflows validate against WorkflowConfiguration.json schema
- Required properties present in all configurations
- State naming conventions followed
- Transition definitions complete

### Business Logic Validation ✅
- Complete state machines with proper entry/exit points
- Error handling and recovery paths defined
- Manual intervention points identified
- Audit trail support through state transitions

### Performance Considerations ✅
- Appropriate execution modes for different operation types
- Timeout configurations for external dependencies
- Retry policies for transient failures
- Asynchronous processing for long-running operations

## Integration Points

The workflows integrate with:
- **Entity Models**: Trade, Position, RegulatoryReport, etc.
- **External Systems**: DTCC GTR, LEI databases, market data feeds
- **Processing Components**: Validation engines, calculation services
- **Notification Systems**: Alert and monitoring infrastructure

## Deployment Notes

1. **Configuration Validation**: Validate all JSON files against schema before deployment
2. **Function Implementation**: Ensure all referenced functions are implemented as CyodaCriterion/CyodaProcessor components
3. **Timeout Tuning**: Adjust timeout values based on actual system performance
4. **Monitoring Setup**: Configure monitoring for workflow state transitions and error conditions
5. **Testing**: Execute end-to-end testing for all use case scenarios

## Maintenance

- **Version Control**: All workflow changes should be version controlled
- **Impact Analysis**: Assess impact of workflow changes on running processes
- **Backward Compatibility**: Consider backward compatibility when modifying existing workflows
- **Documentation**: Update documentation when workflows are modified
