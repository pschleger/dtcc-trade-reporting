# Business Entity Schema Documentation

## Overview

This document provides comprehensive documentation for all business entity schemas in the DTCC Regulatory Reporting System. Each schema is designed to support the complete lifecycle of OTC derivatives trade processing, position management, and regulatory reporting.

## Schema Design Principles

### 1. Workflow State Management
All business entities include comprehensive workflow state management through the `BusinessEntityMetadata` schema:
- Current state tracking with timestamps
- State transition history with actor information
- Support for manual and automated transitions
- Compliance with Cyoda WorkflowConfiguration.json schema

### 2. Data Integrity and Validation
- Required field validation for critical business data
- Format validation using JSON Schema patterns
- Cross-reference validation between related entities
- Regulatory compliance validation rules

### 3. Audit and Compliance
- Complete audit trail for all entity changes
- Immutable audit records with digital signatures
- Regulatory reporting requirements tracking
- Data retention and archival support

### 4. Performance and Scalability
- Optimized schema structure for high-volume processing
- Efficient indexing support through business keys
- Minimal required fields to reduce processing overhead
- Extensible design for future requirements

## Master Data Entities

### Counterparty
**Purpose**: Legal entities involved in OTC derivatives trading
**Key Business Rules**:
- LEI is mandatory and must be valid (20-character format)
- Regulatory classification determines reporting obligations
- Contact information must include primary contact and address
- Risk limits are optional but recommended for active counterparties

**Workflow States**: `draft` → `validated` → `active` → `suspended` → `archived`

**Critical Fields**:
- `lei`: Legal Entity Identifier (required for regulatory reporting)
- `legalName`: Official legal name (required for all communications)
- `jurisdiction`: Primary jurisdiction (affects regulatory treatment)
- `regulatoryClassification`: Determines reporting obligations

### Product
**Purpose**: OTC derivative product definitions and specifications
**Key Business Rules**:
- Asset class and product type are mandatory for regulatory classification
- Economic terms must include notional currency and supported currencies
- Regulatory classification determines clearing and reporting requirements
- Market data requirements specify valuation dependencies

**Workflow States**: `draft` → `approved` → `active` → `deprecated` → `archived`

**Critical Fields**:
- `taxonomy.assetClass`: Required for regulatory reporting
- `economicTerms.notionalCurrency`: Primary currency for the product
- `regulatoryClassification.clearingEligible`: Affects trade processing
- `regulatoryClassification.reportingRequired`: Determines reporting obligations

### ReferenceData
**Purpose**: Market reference data supporting trade valuation and processing
**Key Business Rules**:
- Data type determines validation rules and usage patterns
- Effective and expiry dates control data lifecycle
- Validation status must be current for production use
- Update frequency determines refresh schedules

**Workflow States**: `pending` → `validated` → `active` → `superseded` → `archived`

**Critical Fields**:
- `dataType`: Determines validation and usage rules
- `effectiveDate`: Controls when data becomes usable
- `validationStatus`: Must be "VALIDATED" for production use
- `sourceSystem`: Required for data lineage tracking

### LegalEntity
**Purpose**: Internal and external legal entity registry for regulatory reporting
**Key Business Rules**:
- Entity hierarchy supports complex organizational structures
- Regulatory registrations determine reporting capabilities
- Business activities affect regulatory treatment
- Contact information required for operational purposes

**Workflow States**: `registered` → `verified` → `active` → `suspended` → `deregistered`

**Critical Fields**:
- `entityName`: Official legal name
- `jurisdiction`: Affects regulatory treatment
- `regulatoryRegistrations`: Determines reporting capabilities
- `reportingObligations`: Specifies regulatory requirements

## Transactional Data Entities

### TradeConfirmation
**Purpose**: Raw FpML trade confirmation messages received from external systems
**Key Business Rules**:
- Message integrity verified through SHA-256 hash
- FpML version determines parsing rules
- Validation must pass before processing
- Duplicate detection prevents reprocessing

**Workflow States**: `received` → `validated` → `processed` → `archived` → `failed`

**Critical Fields**:
- `messageId`: Unique identifier for deduplication
- `fpmlContent`: Raw message content (base64 encoded)
- `messageHash`: Integrity verification
- `validationStatus`: Controls processing flow

### Trade
**Purpose**: Processed and validated trade records derived from FpML confirmations
**Key Business Rules**:
- UTI is mandatory for regulatory reporting
- Counterparty information must reference valid Counterparty entities
- Economic terms determine valuation and risk calculations
- Regulatory reporting requirements vary by jurisdiction

**Workflow States**: `new` → `validated` → `confirmed` → `amended` → `cancelled` → `matured`

**Critical Fields**:
- `uti`: Unique Trade Identifier (regulatory requirement)
- `counterparties`: Must reference valid Counterparty entities
- `economicTerms.notionalAmount`: Required for risk calculations
- `regulatoryReporting`: Determines compliance obligations

### Position
**Purpose**: Aggregated view of trades representing net exposure by counterparty and product
**Key Business Rules**:
- Calculation method determines aggregation logic
- Risk metrics updated based on market data changes
- Regulatory thresholds trigger reporting obligations
- Reconciliation required with counterparty positions

**Workflow States**: `calculating` → `calculated` → `reporting` → `reported` → `reconciled`

**Critical Fields**:
- `positionType`: Determines aggregation method
- `exposureMetrics`: Core risk and exposure data
- `calculationTimestamp`: Data freshness indicator
- `reportingObligations`: Compliance requirements

### Amendment
**Purpose**: Trade modification records tracking changes to existing trades
**Key Business Rules**:
- Must reference valid original trade
- Changed fields must be documented in detail
- Authorization required for all amendments
- Regulatory reporting may be required

**Workflow States**: `pending` → `validated` → `applied` → `reported` → `completed`

**Critical Fields**:
- `originalTradeId`: Reference to amended trade
- `changedFields`: Detailed change documentation
- `authorizationId`: Required for compliance
- `regulatoryReporting`: Compliance obligations

### Cancellation
**Purpose**: Trade cancellation records for lifecycle management
**Key Business Rules**:
- Must reference valid original trade
- Impact assessment required for risk management
- Authorization required for all cancellations
- Settlement may be required

**Workflow States**: `pending` → `validated` → `executed` → `settled` → `reported`

**Critical Fields**:
- `originalTradeId`: Reference to cancelled trade
- `impactAssessment`: Risk and financial impact
- `authorizationId`: Required for compliance
- `settlementInstructions`: Payment details if applicable

## Reporting Data Entities

### RegulatoryReport
**Purpose**: DTCC GTR submission records for regulatory compliance
**Key Business Rules**:
- Report content must pass validation before submission
- Submission attempts tracked for audit purposes
- Acknowledgment required from regulatory authority
- Performance metrics monitored for SLA compliance

**Workflow States**: `generated` → `validated` → `submitted` → `acknowledged` → `completed`

**Critical Fields**:
- `reportType`: Determines submission requirements
- `regulatoryRegime`: Affects validation rules
- `reportContent`: Actual report data
- `submissionStatus`: Current processing state

### ReportingObligation
**Purpose**: Tracking of regulatory reporting requirements and deadlines
**Key Business Rules**:
- Thresholds determine when reporting is required
- Deadlines calculated based on business day conventions
- Escalation procedures activated for missed deadlines
- Exemptions may apply based on entity characteristics

**Workflow States**: `identified` → `scheduled` → `due` → `completed` → `overdue`

**Critical Fields**:
- `obligationType`: Type of reporting required
- `applicableThresholds`: Trigger conditions
- `reportingFrequency`: Schedule requirements
- `nextDueDate`: Next compliance deadline

### SubmissionStatus
**Purpose**: Detailed tracking of report submission attempts and outcomes
**Key Business Rules**:
- Each submission attempt tracked separately
- Error details captured for troubleshooting
- Performance metrics monitored
- Retry logic applied for transient failures

**Workflow States**: `pending` → `in_progress` → `successful` → `failed`

**Critical Fields**:
- `reportId`: Reference to submitted report
- `submissionMethod`: How report was submitted
- `responseCode`: System response
- `submissionStatus`: Outcome of attempt

### AuditTrail
**Purpose**: Complete immutable record of all entity state changes
**Key Business Rules**:
- All entity changes must generate audit records
- Records are immutable once created
- Digital signature ensures integrity
- Retention period based on regulatory requirements

**Workflow States**: `created` → `immutable` (no further transitions)

**Critical Fields**:
- `entityType`: Type of audited entity
- `transactionType`: Nature of the change
- `changeDetails`: Specific changes made
- `immutableSignature`: Integrity protection

## Processing Control Entities

### ProcessingBatch
**Purpose**: Coordination of batch processing operations
**Key Business Rules**:
- Dependencies must be satisfied before execution
- Progress tracking enables monitoring
- Error handling supports recovery procedures
- Performance metrics enable optimization

**Workflow States**: `scheduled` → `running` → `completed` → `failed`

**Critical Fields**:
- `batchType`: Determines processing logic
- `processingScope`: What will be processed
- `progressTracking`: Current status
- `performanceMetrics`: Execution statistics

### ValidationResult
**Purpose**: Detailed outcomes of data validation processes
**Key Business Rules**:
- Rule set version determines validation logic
- Severity levels enable prioritization
- Remediation recommendations guide fixes
- Performance metrics enable optimization

**Workflow States**: `active` → `superseded` → `archived`

**Critical Fields**:
- `validationOutcome`: Overall result
- `validationRules`: Individual rule results
- `errorDetails`: Specific problems found
- `remediationRecommendations`: Suggested fixes

### ReconciliationResult
**Purpose**: Results of reconciliation processes between internal and external data
**Key Business Rules**:
- Matching criteria determine comparison logic
- Tolerance levels define acceptable differences
- Discrepancies require investigation
- Quality metrics enable process improvement

**Workflow States**: `completed_clean` → `completed_with_discrepancies` → `resolved`

**Critical Fields**:
- `reconciliationType`: Type of reconciliation
- `reconciliationResults`: Summary statistics
- `discrepancies`: Specific differences found
- `investigationStatus`: Resolution progress

## Cross-Entity Relationships

### Primary Processing Chain
```
TradeConfirmation → Trade → Position → RegulatoryReport
```

### Master Data Dependencies
```
Counterparty ← Trade
Product ← Trade
ReferenceData ← Position (for valuation)
LegalEntity ← Counterparty
```

### Lifecycle Management
```
Trade → Amendment (modifications)
Trade → Cancellation (terminations)
Position → ReconciliationResult (validation)
```

### Audit and Control
```
All Entities → AuditTrail (change tracking)
All Entities → ValidationResult (quality control)
Batch Operations → ProcessingBatch (coordination)
```

## Validation Rules Summary

### Mandatory Field Validation
- All entities require metadata with workflow state
- Business keys must be unique within entity type
- Cross-references must point to valid entities
- Timestamps must be in valid ISO 8601 format

### Format Validation
- LEI: 20-character alphanumeric format
- Currency codes: 3-character ISO 4217 format
- Country codes: 2-character ISO 3166 format
- UTI/USI: Maximum 52 characters

### Business Rule Validation
- Trade dates must be valid business days
- Notional amounts must be positive
- Regulatory thresholds must be consistent
- Workflow state transitions must be valid

### Regulatory Compliance Validation
- Reporting deadlines must be met
- Required fields for regulatory reports
- Counterparty classification consistency
- Product regulatory treatment alignment

## Performance Considerations

### Indexing Strategy
- Business keys should be indexed for fast lookup
- Workflow state fields for status queries
- Date fields for time-based queries
- Cross-reference fields for join operations

### Data Volume Management
- Audit trails can grow large - implement archival strategy
- Position calculations may be frequent - optimize for updates
- Regulatory reports are time-sensitive - prioritize processing
- Reference data changes infrequently - cache aggressively

### Concurrency Control
- Version fields enable optimistic locking
- Workflow states prevent concurrent modifications
- Audit trails provide change detection
- Business keys ensure uniqueness

## Error Handling Patterns

### Validation Errors
- Schema validation errors prevent entity creation
- Business rule violations generate warnings or errors
- Cross-reference failures block processing
- Format errors provide specific correction guidance

### Processing Errors
- Transient errors trigger retry mechanisms
- Permanent errors require manual intervention
- Batch errors may allow partial processing
- System errors generate alerts and notifications

### Recovery Procedures
- Failed entities can be reprocessed
- Audit trails enable change rollback
- Validation results guide error correction
- Escalation procedures ensure resolution

This documentation should be updated whenever schema changes are made to ensure accuracy and completeness.
