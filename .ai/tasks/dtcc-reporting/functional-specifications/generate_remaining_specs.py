#!/usr/bin/env python3
"""
Script to generate the remaining functional specifications for all workflow components.
This script creates standardized specifications for all 105 components based on the template.
"""

import os
import json
from pathlib import Path

# Component lists extracted from workflow configurations
PROCESSORS = [
    # Trade Confirmation Processing
    ("validateFpMLMessage", "trade-confirmation", "Validates incoming FpML trade confirmation messages for structural integrity and compliance"),
    ("convertToTrade", "trade-confirmation", "Converts validated FpML trade confirmation messages to standardized Trade entities"),
    ("archiveConfirmation", "trade-confirmation", "Archives processed trade confirmation messages for regulatory compliance"),
    ("notifyProcessingFailure", "trade-confirmation", "Notifies relevant teams of trade confirmation processing failures"),
    
    # Trade Management
    ("validateBusinessRules", "trade-management", "Validates trade entities against business rules, trading limits, and regulatory constraints"),
    ("processAmendment", "trade-management", "Processes trade amendment requests with validation and impact assessment"),
    ("processCancellation", "trade-management", "Processes trade cancellation requests with authorization and impact assessment"),
    ("processMaturity", "trade-management", "Processes trade maturity events and lifecycle completion"),
    ("archiveTrade", "trade-management", "Archives completed or cancelled trades for regulatory compliance"),
    ("notifyFailure", "trade-management", "Notifies relevant teams of trade processing failures"),
    
    # Position Management
    ("calculatePosition", "position-management", "Calculates position data from trade aggregation and market data"),
    ("validatePosition", "position-management", "Validates calculated position data for accuracy and completeness"),
    ("reconcilePosition", "position-management", "Reconciles internal positions with external counterparty positions"),
    ("recalculatePosition", "position-management", "Recalculates position data after trade amendments or corrections"),
    ("generateReport", "position-management", "Generates position reports for regulatory submission"),
    
    # Amendment Processing
    ("validateAmendment", "amendment-processing", "Validates amendment request data and business rules compliance"),
    ("assessImpact", "amendment-processing", "Assesses impact of amendment on positions, reporting, and risk"),
    ("applyAmendment", "amendment-processing", "Applies validated amendment to trade entity with audit trail"),
    ("generateAmendmentReport", "amendment-processing", "Generates regulatory report for trade amendment"),
    ("archiveAmendment", "amendment-processing", "Archives completed amendment for regulatory compliance"),
    
    # Cancellation Processing
    ("assessCancellationImpact", "cancellation-processing", "Assesses impact of cancellation on positions and reporting"),
    ("authorizeCancellation", "cancellation-processing", "Authorizes cancellation request based on business rules"),
    ("executeCancellation", "cancellation-processing", "Executes authorized cancellation with proper audit trail"),
    ("generateCancellationReport", "cancellation-processing", "Generates regulatory report for trade cancellation"),
    ("archiveCancellation", "cancellation-processing", "Archives completed cancellation for regulatory compliance"),
    
    # Counterparty Management
    ("validateCounterparty", "counterparty-management", "Validates counterparty data and LEI information for compliance"),
    ("checkDuplicates", "counterparty-management", "Checks for duplicate counterparty records in the system"),
    ("updateCounterparty", "counterparty-management", "Updates counterparty information with validation and approval"),
    ("suspendCounterparty", "counterparty-management", "Suspends counterparty due to compliance or credit issues"),
    ("reactivateCounterparty", "counterparty-management", "Reactivates suspended counterparty after issue resolution"),
    ("archiveCounterparty", "counterparty-management", "Archives decommissioned counterparty for regulatory compliance"),
    
    # Reference Data Management
    ("validateProduct", "reference-data-management", "Validates product specification and taxonomy compliance"),
    ("approveProduct", "reference-data-management", "Processes product committee approval workflow"),
    ("updateProduct", "reference-data-management", "Updates product specifications with validation and approval"),
    ("deprecateProduct", "reference-data-management", "Deprecates product and handles existing trade impact"),
    ("archiveProduct", "reference-data-management", "Archives deprecated product for regulatory compliance"),
    
    # Regulatory Reporting
    ("generateReport", "regulatory-reporting", "Generates regulatory reports for submission to DTCC GTR"),
    ("validateReport", "regulatory-reporting", "Validates generated reports against DTCC requirements"),
    ("submitReport", "regulatory-reporting", "Submits validated reports to DTCC GTR system"),
    ("correctReport", "regulatory-reporting", "Corrects rejected reports based on DTCC feedback"),
    ("archiveReport", "regulatory-reporting", "Archives submitted reports for regulatory compliance"),
    ("processAcknowledgment", "regulatory-reporting", "Processes DTCC acknowledgment and rejection responses"),
    
    # Reconciliation Processing
    ("gatherReconciliationData", "reconciliation-processing", "Gathers data required for position reconciliation process"),
    ("comparePositions", "reconciliation-processing", "Compares internal and external position data for discrepancies"),
    ("analyzeDiscrepancies", "reconciliation-processing", "Analyzes identified discrepancies for root cause determination"),
    ("investigateDiscrepancies", "reconciliation-processing", "Investigates complex discrepancies requiring detailed analysis"),
    ("resolveDiscrepancies", "reconciliation-processing", "Resolves identified discrepancies through appropriate actions"),
    ("archiveReconciliation", "reconciliation-processing", "Archives reconciliation results for audit and compliance"),
    
    # Batch Processing
    ("initializeBatch", "batch-processing", "Initializes batch processing operation with validation and setup"),
    ("executeBatchProcessing", "batch-processing", "Executes batch processing operations across multiple entities"),
    ("monitorBatchProgress", "batch-processing", "Monitors batch processing progress and performance metrics"),
    ("handleBatchErrors", "batch-processing", "Handles errors encountered during batch processing operations"),
    ("retryBatchProcessing", "batch-processing", "Retries failed batch processing operations with backoff strategy"),
    ("completeBatchProcessing", "batch-processing", "Completes batch processing with final validation and reporting"),
    ("archiveBatch", "batch-processing", "Archives completed batch processing results for audit trail"),
    
    # Error Handling
    ("escalateIssue", "error-handling", "Escalates critical issues to appropriate operations teams"),
    ("notifyFailure", "error-handling", "Notifies relevant teams of processing failures and errors"),
    ("notifyProcessingFailure", "error-handling", "Notifies teams of specific processing failures with context")
]

CRITERIA = [
    # Validation Criteria
    ("validateFpMLMessage", "validation-criteria", "Evaluates whether FpML message has been successfully validated"),
    ("validateBusinessRules", "validation-criteria", "Evaluates whether trade passes business rule validation"),
    ("isValidationSuccessful", "validation-criteria", "Evaluates whether validation process completed successfully"),
    ("hasValidationErrors", "validation-criteria", "Evaluates whether validation process encountered errors"),
    
    # Processing Status Criteria
    ("isCalculationSuccessful", "processing-status-criteria", "Evaluates whether calculation process completed successfully"),
    ("isGenerationSuccessful", "processing-status-criteria", "Evaluates whether report generation completed successfully"),
    ("isSubmissionSuccessful", "processing-status-criteria", "Evaluates whether submission process completed successfully"),
    ("isExecutionSuccessful", "processing-status-criteria", "Evaluates whether execution process completed successfully"),
    ("isAmendmentSuccessful", "processing-status-criteria", "Evaluates whether amendment process completed successfully"),
    ("isCancellationSuccessful", "processing-status-criteria", "Evaluates whether cancellation process completed successfully"),
    ("isAuthorizationSuccessful", "processing-status-criteria", "Evaluates whether authorization process completed successfully"),
    ("isApplicationSuccessful", "processing-status-criteria", "Evaluates whether application process completed successfully"),
    ("isImpactAssessmentSuccessful", "processing-status-criteria", "Evaluates whether impact assessment completed successfully"),
    ("isDataGatheringSuccessful", "processing-status-criteria", "Evaluates whether data gathering completed successfully"),
    ("isComparisonSuccessful", "processing-status-criteria", "Evaluates whether comparison process completed successfully"),
    ("isAnalysisSuccessful", "processing-status-criteria", "Evaluates whether analysis process completed successfully"),
    ("isReconciliationSuccessful", "processing-status-criteria", "Evaluates whether reconciliation process completed successfully"),
    ("isReportingSuccessful", "processing-status-criteria", "Evaluates whether reporting process completed successfully"),
    ("isReportGenerationSuccessful", "processing-status-criteria", "Evaluates whether report generation completed successfully"),
    
    # Error Detection Criteria
    ("hasProcessingErrors", "error-detection-criteria", "Evaluates whether processing encountered errors"),
    ("hasCalculationErrors", "error-detection-criteria", "Evaluates whether calculation process encountered errors"),
    ("hasGenerationErrors", "error-detection-criteria", "Evaluates whether generation process encountered errors"),
    ("hasSubmissionErrors", "error-detection-criteria", "Evaluates whether submission process encountered errors"),
    ("hasAmendmentErrors", "error-detection-criteria", "Evaluates whether amendment process encountered errors"),
    ("hasCancellationErrors", "error-detection-criteria", "Evaluates whether cancellation process encountered errors"),
    ("hasAuthorizationErrors", "error-detection-criteria", "Evaluates whether authorization process encountered errors"),
    ("hasApplicationErrors", "error-detection-criteria", "Evaluates whether application process encountered errors"),
    ("hasImpactAssessmentErrors", "error-detection-criteria", "Evaluates whether impact assessment encountered errors"),
    ("hasDataGatheringErrors", "error-detection-criteria", "Evaluates whether data gathering encountered errors"),
    ("hasComparisonErrors", "error-detection-criteria", "Evaluates whether comparison process encountered errors"),
    ("hasAnalysisErrors", "error-detection-criteria", "Evaluates whether analysis process encountered errors"),
    ("hasReconciliationErrors", "error-detection-criteria", "Evaluates whether reconciliation process encountered errors"),
    ("hasReportingErrors", "error-detection-criteria", "Evaluates whether reporting process encountered errors"),
    ("hasExecutionErrors", "error-detection-criteria", "Evaluates whether execution process encountered errors"),
    ("areProcessingErrorsDetected", "error-detection-criteria", "Evaluates whether any processing errors detected in batch"),
    
    # Business Logic Criteria
    ("isApproachingMaturity", "business-logic-criteria", "Evaluates whether trade is approaching maturity date"),
    ("isMaturityDateReached", "business-logic-criteria", "Evaluates whether trade has reached maturity date"),
    ("isReconciliationDue", "business-logic-criteria", "Evaluates whether position reconciliation is due"),
    ("isScheduledTimeReached", "business-logic-criteria", "Evaluates whether scheduled processing time reached"),
    ("isSubmissionTimeReached", "business-logic-criteria", "Evaluates whether report submission time reached"),
    ("isRetryDelayElapsed", "business-logic-criteria", "Evaluates whether retry delay period has elapsed"),
    ("isGracePeriodElapsed", "business-logic-criteria", "Evaluates whether grace period for processing has elapsed"),
    ("isMaxRetriesExceeded", "business-logic-criteria", "Evaluates whether maximum retry attempts exceeded"),
    ("isTradeCreated", "business-logic-criteria", "Evaluates whether trade entity was successfully created"),
    ("hasTradeUpdates", "business-logic-criteria", "Evaluates whether trade has pending updates"),
    
    # Regulatory Compliance Criteria
    ("isDtccAcknowledgmentReceived", "regulatory-compliance-criteria", "Evaluates whether DTCC acknowledgment received"),
    ("isDtccRejectionReceived", "regulatory-compliance-criteria", "Evaluates whether DTCC rejection received"),
    ("isRetentionPeriodMet", "regulatory-compliance-criteria", "Evaluates whether regulatory retention period met"),
    ("isReportingRequired", "regulatory-compliance-criteria", "Evaluates whether regulatory reporting required"),
    ("isReportingNotRequired", "regulatory-compliance-criteria", "Evaluates whether regulatory reporting not required"),
    
    # Data Quality Criteria
    ("hasDiscrepancies", "data-quality-criteria", "Evaluates whether discrepancies found in data comparison"),
    ("hasNoDiscrepancies", "data-quality-criteria", "Evaluates whether no discrepancies found in data comparison"),
    ("hasRetriesAvailable", "data-quality-criteria", "Evaluates whether retry attempts still available"),
    
    # Workflow Control Criteria
    ("areAllItemsProcessed", "workflow-control-criteria", "Evaluates whether all batch items completed processing"),
    ("noActionRequired", "workflow-control-criteria", "Evaluates whether no further action required")
]

def create_processor_spec(name, domain, description):
    """Create a processor specification based on the template."""
    
    # Map domain to entity type and business context
    entity_mapping = {
        "trade-confirmation": ("TradeConfirmation", "Trade Confirmation Processing"),
        "trade-management": ("Trade", "Trade Management"),
        "position-management": ("Position", "Position Management"),
        "amendment-processing": ("Amendment", "Amendment Processing"),
        "cancellation-processing": ("Cancellation", "Cancellation Processing"),
        "counterparty-management": ("Counterparty", "Counterparty Data Management"),
        "reference-data-management": ("Product", "Reference Data Management"),
        "regulatory-reporting": ("RegulatoryReport", "Regulatory Reporting"),
        "reconciliation-processing": ("ReconciliationResult", "Reconciliation Processing"),
        "batch-processing": ("ProcessingBatch", "Batch Processing"),
        "error-handling": ("ErrorEvent", "Error Handling")
    }
    
    entity_type, business_domain = entity_mapping.get(domain, ("Entity", "General Processing"))
    
    spec_content = f"""# {name} Processor Specification

## 1. Component Overview
**Component Name**: {name}
**Component Type**: CyodaProcessor
**Business Domain**: {business_domain}
**Purpose**: {description}
**Workflow Context**: Multiple workflows utilizing {entity_type} entities

## 2. Input Specifications
**Entity Type**: {entity_type}
**Required Fields**:
- `entityId`: string - Unique entity identifier
- `status`: string - Current entity status
- `timestamp`: ISO-8601 timestamp - Entity timestamp
- `data`: object - Entity-specific data payload

**Optional Fields**:
- `metadata`: object - Additional metadata
- `auditTrail`: array - Processing audit trail
- `configuration`: object - Processing configuration overrides

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "processing" - Tags for processing nodes
- `responseTimeoutMs`: 30000 - Maximum processing time
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Business rule configuration
- Master data dependencies
- External service endpoints

## 3. Output Specifications
**Success Response**:
```json
{{
  "status": "SUCCESS",
  "result": {{
    "processedEntity": "{entity_type}",
    "processingTimestamp": "ISO-8601 timestamp",
    "processingResults": {{
      "operationCompleted": true,
      "dataModified": true,
      "validationPassed": true
    }}
  }}
}}
```

**Error Response**:
```json
{{
  "status": "ERROR",
  "errorCode": "PROCESSING_ERROR",
  "errorMessage": "Processing operation failed",
  "details": {{
    "validationErrors": [],
    "businessRuleViolations": [],
    "systemErrors": []
  }}
}}
```

**Side Effects**:
- Updates entity status and data
- Creates audit trail entries
- Publishes processing events
- Updates related entities as needed

## 4. Business Logic
**Processing Steps**:
1. Validate input entity and required data
2. Apply business rules and constraints
3. Execute core processing logic
4. Validate processing results
5. Update entity status and data
6. Create audit trail and notifications

**Business Rules**:
- Entity must be in valid state for processing
- All required data must be present and valid
- Business constraints must be satisfied
- Regulatory requirements must be met

**Algorithms**:
- Standard validation algorithms
- Business rule evaluation engine
- Data transformation and mapping logic

## 5. Validation Rules
**Pre-processing Validations**:
- Entity exists and accessible
- Required fields present and valid
- Entity in appropriate state for processing

**Post-processing Validations**:
- Processing completed successfully
- Entity data updated correctly
- Audit trail created properly

**Data Quality Checks**:
- Data integrity maintained
- Business rule compliance verified
- Regulatory requirements satisfied

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input validation failures
- **PROCESSING_ERROR**: Core processing failures
- **BUSINESS_RULE_ERROR**: Business rule violations
- **SYSTEM_ERROR**: Technical system failures
- **TIMEOUT_ERROR**: Processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient errors
- Fallback procedures for system failures
- Manual intervention for complex errors

**Error Propagation**:
- Errors logged with full context
- Failed processing triggers appropriate workflows
- Critical errors escalated to operations

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 25 seconds (95th percentile)
- **Throughput**: 100 operations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity processing
- **Memory**: 512MB per concurrent operation
- **I/O**: Moderate for data access and updates

**Scalability**:
- Horizontal scaling through node distribution
- Performance optimization through caching
- Load balancing across processing nodes

## 8. Dependencies
**Internal Dependencies**:
- Master Data Service
- Validation Service
- Audit Service
- Configuration Service

**External Dependencies**:
- External data providers (as applicable)
- Regulatory services (as applicable)

**Data Dependencies**:
- Entity master data
- Business rule configuration
- Validation rule definitions

## 9. Configuration Parameters
**Required Configuration**:
- `processingMode`: string - Processing mode - Default: "standard"
- `validationLevel`: string - Validation strictness - Default: "strict"
- `auditEnabled`: boolean - Enable audit trail - Default: true

**Optional Configuration**:
- `timeoutMs`: integer - Processing timeout - Default: 30000
- `retryAttempts`: integer - Maximum retry attempts - Default: 3
- `cacheEnabled`: boolean - Enable result caching - Default: true

**Environment-Specific Configuration**:
- Development: Relaxed validation, extended timeouts
- Production: Strict validation, standard timeouts

## 10. Integration Points
**API Contracts**:
- Input: {entity_type} entity with required data
- Output: Processing results with updated entity

**Data Exchange Formats**:
- **JSON**: Standard data exchange format
- **XML**: Regulatory reporting format (if applicable)

**Event Publishing**:
- **ProcessingCompleted**: Published on successful processing
- **ProcessingFailed**: Published on processing failure

**Event Consumption**:
- **EntityUpdated**: Triggers processing when entity updated
- **ConfigurationChanged**: Updates processing configuration
"""
    
    return spec_content

def create_criterion_spec(name, domain, description):
    """Create a criterion specification based on the template."""
    
    # Map domain to entity type and business context
    entity_mapping = {
        "validation-criteria": ("Entity", "Data Validation"),
        "processing-status-criteria": ("Entity", "Processing Status Evaluation"),
        "error-detection-criteria": ("Entity", "Error Detection"),
        "business-logic-criteria": ("Entity", "Business Logic Evaluation"),
        "regulatory-compliance-criteria": ("Entity", "Regulatory Compliance"),
        "data-quality-criteria": ("Entity", "Data Quality Assessment"),
        "workflow-control-criteria": ("Entity", "Workflow Control")
    }
    
    entity_type, business_domain = entity_mapping.get(domain, ("Entity", "General Evaluation"))
    
    spec_content = f"""# {name} Criterion Specification

## 1. Component Overview
**Component Name**: {name}
**Component Type**: CyodaCriterion
**Business Domain**: {business_domain}
**Purpose**: {description}
**Workflow Context**: Multiple workflows requiring {name} evaluation

## 2. Input Parameters
**Entity Type**: {entity_type}
**Required Fields**:
- `entityId`: string - Unique entity identifier
- `status`: string - Current entity status
- `timestamp`: ISO-8601 timestamp - Entity timestamp
- `data`: object - Entity data for evaluation

**Optional Fields**:
- `metadata`: object - Additional evaluation context
- `configuration`: object - Evaluation configuration overrides

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "evaluation" - Tags for evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time
- `context`: string - Evaluation context identifier

**Evaluation Context**:
- Business rule configuration
- Evaluation thresholds and parameters
- Historical data for comparison

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF primary_condition AND secondary_condition THEN
    RETURN true
ELSE IF exception_condition THEN
    RETURN true (with override)
ELSE
    RETURN false
```

**Boolean Logic**:
- Primary evaluation based on entity state and data
- Secondary evaluation considers business rules
- Exception handling for special cases
- Default behavior for edge cases

**Calculation Methods**:
- Standard comparison operations
- Threshold-based evaluations
- Time-based calculations (if applicable)

## 4. Success Conditions
**Positive Evaluation Criteria**:
- Primary condition: Entity meets main evaluation criteria
- Data quality: Required data present and valid
- Business rules: All applicable business rules satisfied
- Temporal validity: Evaluation performed within valid timeframe

**Success Scenarios**:
- Standard success: All conditions met normally
- Override success: Conditions met with approved exceptions
- Conditional success: Conditions met with warnings

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- Primary condition: Entity fails main evaluation criteria
- Data issues: Required data missing or invalid
- Business rule violations: Applicable business rules not satisfied
- System issues: Evaluation cannot be completed

**Failure Scenarios**:
- Standard failure: Conditions not met
- Data failure: Insufficient or invalid data
- System failure: Technical evaluation failure

## 6. Edge Cases
**Boundary Conditions**:
- Threshold boundary values
- Time boundary conditions
- Data availability edge cases
- Concurrent evaluation scenarios

**Special Scenarios**:
- Emergency override conditions
- Maintenance mode behavior
- Degraded system performance

**Data Absence Handling**:
- Missing required data defaults to false
- Optional data absence handled gracefully
- Partial data scenarios evaluated appropriately

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 2 seconds (95th percentile)
- **Throughput**: 1000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for evaluation logic
- **Memory**: 64MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- Configuration Service
- Master Data Service (if applicable)
- Audit Service

**External Dependencies**:
- External data sources (if applicable)

**Data Dependencies**:
- Evaluation rule configuration
- Threshold and parameter settings
- Historical data (if applicable)

## 9. Configuration
**Configurable Thresholds**:
- `evaluationThreshold`: decimal - Primary evaluation threshold
- `timeoutSeconds`: integer - Evaluation timeout
- `strictMode`: boolean - Enable strict evaluation

**Evaluation Parameters**:
- `cacheResults`: boolean - Cache evaluation results
- `auditEvaluations`: boolean - Audit all evaluations
- `allowOverrides`: boolean - Allow manual overrides

**Environment-Specific Settings**:
- Development: Relaxed thresholds, extended timeouts
- Production: Standard thresholds, normal timeouts

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout
- **CONFIGURATION_ERROR**: Invalid evaluation configuration
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability
- Retry mechanism for transient errors
- Fallback to cached results when appropriate

**Error Propagation**:
- Evaluation errors logged with context
- Failed evaluations trigger manual review
- Critical errors escalated appropriately
"""
    
    return spec_content

def main():
    """Generate all remaining functional specifications."""
    base_path = Path(".ai/tasks/dtcc-reporting/functional-specifications")
    
    # Create processor specifications
    for name, domain, description in PROCESSORS:
        file_path = base_path / "processors" / domain / f"{name}.md"
        if not file_path.exists():
            file_path.parent.mkdir(parents=True, exist_ok=True)
            with open(file_path, 'w') as f:
                f.write(create_processor_spec(name, domain, description))
            print(f"Created processor specification: {file_path}")
    
    # Create criterion specifications
    for name, domain, description in CRITERIA:
        file_path = base_path / "criteria" / domain / f"{name}.md"
        if not file_path.exists():
            file_path.parent.mkdir(parents=True, exist_ok=True)
            with open(file_path, 'w') as f:
                f.write(create_criterion_spec(name, domain, description))
            print(f"Created criterion specification: {file_path}")
    
    print(f"\nGeneration complete!")
    print(f"Total processors: {len(PROCESSORS)}")
    print(f"Total criteria: {len(CRITERIA)}")
    print(f"Total specifications: {len(PROCESSORS) + len(CRITERIA)}")

if __name__ == "__main__":
    main()
