# Naming Conventions and Design Principles

## Entity Naming Standards

### Entity Type Names
Entity types use PascalCase with descriptive, business-meaningful names that clearly indicate their purpose within the regulatory reporting domain.

**Naming Pattern**: `[BusinessConcept][EntityType]`

**Examples:**
- `TradeConfirmation` - Raw FpML trade confirmation messages
- `RegulatoryReport` - DTCC GTR submission records
- `CounterpartyMaster` - Legal entity reference data
- `PositionSnapshot` - Point-in-time position calculations

**Guidelines:**
- Use singular nouns (Trade, not Trades)
- Avoid technical abbreviations in favor of business terms
- Include entity type suffix when it adds clarity (Confirmation, Report, Master)
- Maximum 30 characters for entity type names

### Entity Identifier Patterns
Entity identifiers follow consistent patterns that support both human readability and system processing.

**Primary Key Format**: `{entityType}_{businessKey}_{version?}`

**Examples:**
- `TRADE_UTI_550e8400-e29b-41d4-a716-446655440000`
- `POSITION_CPTY001_PROD123_20241127`
- `REPORT_GTR_DAILY_20241127_001`

**Business Key Components:**
- **Trade entities**: Use UTI (Unique Trade Identifier) when available
- **Position entities**: Combine counterparty and product identifiers
- **Report entities**: Include report type and generation timestamp
- **Master data**: Use standard industry identifiers (LEI, ISIN, etc.)

### Attribute Naming Standards
Entity attributes use camelCase with descriptive names that clearly indicate data content and format.

**Temporal Attributes:**
- `createdTimestamp` - Entity creation time
- `lastModifiedTimestamp` - Last update time
- `effectiveDate` - Business effective date
- `expiryDate` - Business expiry date
- `reportingDate` - Regulatory reporting date

**Identifier Attributes:**
- `entityId` - Primary entity identifier
- `correlationId` - Cross-entity correlation
- `parentEntityId` - Hierarchical parent reference
- `externalSystemId` - Source system identifier

**Status Attributes:**
- `processingStatus` - Current processing state
- `validationStatus` - Data validation outcome
- `complianceStatus` - Regulatory compliance state
- `reconciliationStatus` - Data reconciliation state

**Financial Attributes:**
- `notionalAmount` - Trade notional value
- `marketValue` - Current market valuation
- `netExposure` - Net position exposure
- `riskMetrics` - Risk calculation results

## Workflow Design Principles

### State Naming Conventions
Workflow states use descriptive names that clearly indicate the business condition of the entity.

**State Naming Pattern**: `[Adjective/Verb][BusinessCondition]`

**Examples:**
- `PendingValidation` - Awaiting data validation
- `AwaitingConfirmation` - Waiting for counterparty confirmation
- `ReadyForReporting` - Eligible for regulatory submission
- `SubmittedToRegulator` - Report sent to DTCC GTR
- `ComplianceVerified` - Regulatory compliance confirmed

**Guidelines:**
- Use present participle (-ing) for ongoing processes
- Use past participle (-ed) for completed actions
- Avoid technical jargon in favor of business terminology
- Maximum 25 characters for state names

### Transition Naming Standards
Transitions describe the business action that moves an entity between states.

**Transition Pattern**: `[BusinessAction][Object?]`

**Examples:**
- `ValidateTradeData` - Perform trade data validation
- `ConfirmWithCounterparty` - Obtain counterparty confirmation
- `CalculatePosition` - Compute position values
- `GenerateReport` - Create regulatory report
- `SubmitToRegulator` - Send report to authority

### Criteria Naming Conventions
Criteria use descriptive names that clearly indicate the business condition being evaluated.

**Criteria Pattern**: `[BusinessCondition][Check/Validation]`

**Examples:**
- `TradeDataCompleteCheck` - Verify all required trade fields present
- `RegulatoryThresholdValidation` - Check reporting threshold compliance
- `CounterpartyEligibilityCheck` - Verify counterparty authorization
- `SubmissionDeadlineValidation` - Ensure regulatory deadline compliance

### Processor Naming Standards
Processors use action-oriented names that describe the business operation being performed.

**Processor Pattern**: `[BusinessAction]Processor`

**Examples:**
- `TradeValidationProcessor` - Validate incoming trade data
- `PositionCalculationProcessor` - Calculate position values
- `ReportGenerationProcessor` - Generate regulatory reports
- `NotificationProcessor` - Send business notifications

## Data Type Standards

### Temporal Data Types
- **Timestamps**: ISO 8601 format with timezone (2024-11-27T14:30:00Z)
- **Dates**: ISO 8601 date format (2024-11-27)
- **Durations**: ISO 8601 duration format (P1Y2M3DT4H5M6S)

### Identifier Data Types
- **UUIDs**: RFC 4122 format (550e8400-e29b-41d4-a716-446655440000)
- **LEIs**: ISO 17442 format (20 alphanumeric characters)
- **UTIs**: 52-character format per CFTC/ESMA standards
- **ISINs**: ISO 6166 format (12 alphanumeric characters)

### Financial Data Types
- **Amounts**: Decimal with explicit currency designation
- **Rates**: Decimal with basis point precision
- **Percentages**: Decimal between 0 and 1 (0.0525 for 5.25%)

### Status Enumerations
Standardized enumeration values for common status fields:

**Processing Status:**
- `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`, `CANCELLED`

**Validation Status:**
- `NOT_VALIDATED`, `VALID`, `INVALID`, `WARNING`, `REQUIRES_REVIEW`

**Compliance Status:**
- `COMPLIANT`, `NON_COMPLIANT`, `PENDING_REVIEW`, `EXEMPTED`

## JSON Schema Conventions

### Schema Structure
Entity schemas follow consistent structure patterns for maintainability and tooling support.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://dtcc-reporting.cyoda.io/schema/entity/{EntityType}.json",
  "title": "{EntityType}",
  "description": "Business description of entity purpose",
  "type": "object",
  "properties": {
    // Entity attributes
  },
  "required": ["entityId", "createdTimestamp"],
  "additionalProperties": false
}
```

### Property Definitions
- **Required fields**: Always include entityId and createdTimestamp
- **Optional fields**: Use explicit null handling where appropriate
- **Validation rules**: Include format, pattern, and range constraints
- **Documentation**: Provide clear descriptions for all properties

### Schema Versioning
- **Major versions**: Breaking changes to existing properties
- **Minor versions**: Addition of new optional properties
- **Patch versions**: Documentation or validation rule updates

## Error Handling Standards

### Error Code Format
Error codes follow hierarchical structure: `{Domain}_{Category}_{Specific}`

**Examples:**
- `TRADE_VALIDATION_MISSING_COUNTERPARTY`
- `POSITION_CALCULATION_INSUFFICIENT_DATA`
- `REPORT_SUBMISSION_DEADLINE_EXCEEDED`
- `REFERENCE_DATA_STALE_MARKET_DATA`

### Error Message Format
Error messages provide actionable information for both technical and business users.

**Message Structure:**
```
{BusinessContext}: {TechnicalDetail}. {RecommendedAction}
```

**Example:**
```
Trade validation failed: Missing required counterparty LEI. 
Please provide valid Legal Entity Identifier for counterparty.
```

## Documentation Standards

### Entity Documentation
Each entity type requires complete documentation including:

- **Business Purpose**: Clear description of entity role in business process
- **Lifecycle Overview**: Summary of entity states and transitions
- **Key Relationships**: Primary relationships with other entities
- **Regulatory Context**: Relevant regulatory requirements and constraints
- **Data Sources**: Systems and processes that create/update the entity

### Workflow Documentation
Workflow documentation includes:

- **Business Process**: High-level description of business process
- **State Definitions**: Clear definition of each workflow state
- **Transition Logic**: Business rules governing state transitions
- **Error Scenarios**: Common error conditions and recovery procedures
- **Performance Characteristics**: Expected processing times and volumes

### API Documentation
API documentation follows OpenAPI 3.0 standards with:

- **Endpoint Descriptions**: Clear purpose and usage guidelines
- **Request/Response Examples**: Realistic data examples
- **Error Responses**: Complete error scenario documentation
- **Authentication Requirements**: Security and authorization details
- **Rate Limiting**: Usage limits and throttling policies
