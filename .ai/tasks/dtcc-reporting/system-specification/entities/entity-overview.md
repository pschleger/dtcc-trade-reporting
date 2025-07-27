# DTCC Regulatory Reporting System - Entity Overview

## Entity Classification Framework

The DTCC Regulatory Reporting System organizes business entities into four primary categories based on their role in the trade processing and regulatory reporting lifecycle:

### 1. Master Data Entities
Foundational reference data that supports trade processing and regulatory reporting.

### 2. Transactional Data Entities  
Core business entities representing trades, positions, and related transaction data.

### 3. Reporting Data Entities
Entities specifically designed for regulatory compliance and audit requirements.

### 4. Processing Control Entities
Operational entities that coordinate and monitor system processing activities.

---

## Master Data Entities

### Counterparty
**Purpose**: Represents legal entities involved in OTC derivatives trading
**Key Attributes**:
- Legal Entity Identifier (LEI)
- Legal name and jurisdiction
- Regulatory classification (financial/non-financial)
- Contact information and authorized representatives
- Regulatory reporting obligations by jurisdiction

**Lifecycle States**: Draft → Validated → Active → Suspended → Archived
**Key Relationships**: Referenced by Trade, Position, RegulatoryReport

### ReferenceData
**Purpose**: Market reference data supporting trade valuation and processing
**Key Attributes**:
- Data type (currency, holiday calendar, market rates)
- Effective date ranges
- Source system and validation status
- Update frequency and last refresh timestamp

**Lifecycle States**: Pending → Validated → Active → Superseded → Archived
**Key Relationships**: Referenced by Trade, Position calculations

### Product
**Purpose**: OTC derivative product definitions and specifications
**Key Attributes**:
- Product taxonomy (asset class, product type, sub-type)
- Economic terms and calculation methodologies
- Regulatory classification and reporting requirements
- Supported currencies and markets

**Lifecycle States**: Draft → Approved → Active → Deprecated → Archived
**Key Relationships**: Referenced by Trade, Position

### LegalEntity
**Purpose**: Internal and external legal entity registry for regulatory reporting
**Key Attributes**:
- Entity hierarchy and ownership structure
- Regulatory registrations and licenses
- Reporting obligations and exemptions
- Authorized trading relationships

**Lifecycle States**: Registered → Verified → Active → Suspended → Deregistered
**Key Relationships**: Referenced by Counterparty, RegulatoryReport

---

## Transactional Data Entities

### TradeConfirmation
**Purpose**: Raw FpML trade confirmation messages received from external systems
**Key Attributes**:
- Original FpML message content
- Message metadata (sender, timestamp, message ID)
- Validation status and error details
- Processing correlation identifiers

**Lifecycle States**: Received → Validated → Processed → Archived → Failed
**Key Relationships**: Spawns Trade entity upon successful processing

### Trade
**Purpose**: Processed and validated trade records derived from FpML confirmations
**Key Attributes**:
- Trade identification (UTI, USI, internal ID)
- Economic terms (notional, rates, dates, currencies)
- Counterparty information and roles
- Trade type and lifecycle events
- Regulatory reporting requirements

**Lifecycle States**: New → Validated → Confirmed → Amended → Cancelled → Matured
**Key Relationships**: Derived from TradeConfirmation, aggregated into Position

### Position
**Purpose**: Aggregated view of trades representing net exposure by counterparty and product
**Key Attributes**:
- Position identification and hierarchy
- Net exposure calculations (notional, market value, risk metrics)
- Contributing trade references
- Last calculation timestamp and methodology
- Regulatory reporting thresholds and status

**Lifecycle States**: Calculating → Calculated → Reporting → Reported → Reconciled
**Key Relationships**: Aggregates multiple Trade entities, generates RegulatoryReport

### Amendment
**Purpose**: Trade modification records tracking changes to existing trades
**Key Attributes**:
- Original trade reference
- Amendment type and effective date
- Changed field details (before/after values)
- Amendment reason and authorization
- Impact on positions and reporting

**Lifecycle States**: Pending → Validated → Applied → Reported → Archived
**Key Relationships**: References original Trade, may trigger Position recalculation

### Cancellation
**Purpose**: Trade cancellation records for lifecycle management
**Key Attributes**:
- Original trade reference
- Cancellation reason and effective date
- Authorization details
- Impact assessment on positions
- Regulatory notification requirements

**Lifecycle States**: Requested → Validated → Executed → Reported → Archived
**Key Relationships**: References original Trade, triggers Position adjustment

---

## Reporting Data Entities

### RegulatoryReport
**Purpose**: DTCC GTR submission records for regulatory compliance
**Key Attributes**:
- Report identification and type
- Regulatory regime and submission deadline
- Report content and format (XML, JSON)
- Submission status and acknowledgments
- Related positions and trades

**Lifecycle States**: Generated → Validated → Submitted → Acknowledged → Rejected → Resubmitted
**Key Relationships**: References Position, Trade entities

### ReportingObligation
**Purpose**: Tracking of regulatory reporting requirements and deadlines
**Key Attributes**:
- Obligation type and regulatory regime
- Applicable entities and thresholds
- Reporting frequency and deadlines
- Compliance status and exceptions
- Escalation procedures

**Lifecycle States**: Identified → Scheduled → Due → Completed → Overdue → Waived
**Key Relationships**: Triggers RegulatoryReport generation

### SubmissionStatus
**Purpose**: Detailed tracking of report submission attempts and outcomes
**Key Attributes**:
- Submission attempt details
- Response codes and messages
- Retry logic and escalation triggers
- Performance metrics
- Error categorization and resolution

**Lifecycle States**: Pending → InProgress → Successful → Failed → Retrying → Abandoned
**Key Relationships**: References RegulatoryReport

### AuditTrail
**Purpose**: Complete immutable record of all entity state changes
**Key Attributes**:
- Entity reference and version
- State transition details
- User/system actor information
- Timestamp and transaction context
- Business justification

**Lifecycle States**: Created → Immutable (no further transitions)
**Key Relationships**: References all other entities

---

## Processing Control Entities

### ProcessingBatch
**Purpose**: Coordination of batch processing operations
**Key Attributes**:
- Batch identification and type
- Processing scope and parameters
- Start/end timestamps
- Success/failure metrics
- Error summary and resolution status

**Lifecycle States**: Scheduled → Running → Completed → Failed → Retrying
**Key Relationships**: May reference multiple entity types

### ValidationResult
**Purpose**: Detailed outcomes of data validation processes
**Key Attributes**:
- Validation rule set and version
- Entity reference and validation scope
- Validation outcome (pass/fail/warning)
- Error details and severity
- Remediation recommendations

**Lifecycle States**: Pending → Completed → Reviewed → Resolved
**Key Relationships**: References validated entities

### ReconciliationResult
**Purpose**: Outcomes of position reconciliation and data consistency checks
**Key Attributes**:
- Reconciliation type and scope
- Comparison methodology
- Discrepancy identification and quantification
- Resolution status and actions taken
- Impact assessment

**Lifecycle States**: Initiated → InProgress → Completed → Reviewed → Resolved
**Key Relationships**: References Position, Trade entities

---

## Entity Relationship Patterns

### Hierarchical Relationships
- **Counterparty** → **LegalEntity** (ownership hierarchy)
- **Product** → **Product** (product taxonomy)
- **Position** → **Position** (position aggregation levels)

### Temporal Relationships
- **Trade** → **Amendment** → **Amendment** (trade lifecycle)
- **Position** → **Position** (position history over time)
- **RegulatoryReport** → **SubmissionStatus** (submission attempts)

### Aggregation Relationships
- **TradeConfirmation** → **Trade** (message to business entity)
- **Trade** → **Position** (trade aggregation)
- **Position** → **RegulatoryReport** (reporting aggregation)

### Reference Relationships
- All transactional entities reference master data entities
- All entities generate **AuditTrail** records
- Processing entities coordinate operations across multiple business entities
