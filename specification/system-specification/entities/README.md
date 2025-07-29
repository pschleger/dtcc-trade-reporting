# Business Entity Schemas

## Overview

This directory contains comprehensive JSON Schema definitions for all business entities in the DTCC Regulatory Reporting System. These schemas support the complete lifecycle of OTC derivatives trade processing, position management, and regulatory reporting.

Entity Schema JSON files are located in the `src/main/resources/schema/business/` directory of the codebase.
DO NOT PUT ANY entity schema artefacts in this directory.

## Schema Organization

### Master Data Entities
Core reference data that supports all business operations:

- **[Counterparty.json](./Counterparty.json)** - Legal entities involved in OTC derivatives trading
- **[Product.json](./Product.json)** - OTC derivative product definitions and specifications  
- **[ReferenceData.json](./ReferenceData.json)** - Market reference data supporting trade valuation
- **[LegalEntity.json](./LegalEntity.json)** - Internal and external legal entity registry

### Transactional Data Entities
Entities representing business transactions and their lifecycle:

- **[TradeConfirmation.json](./TradeConfirmation.json)** - Raw FpML trade confirmation messages
- **[Trade.json](./Trade.json)** - Processed and validated trade records
- **[Position.json](./Position.json)** - Aggregated trade positions by counterparty and product
- **[Amendment.json](./Amendment.json)** - Trade modification records
- **[Cancellation.json](./Cancellation.json)** - Trade cancellation records

### Reporting Data Entities
Entities supporting regulatory compliance and reporting:

- **[RegulatoryReport.json](./RegulatoryReport.json)** - DTCC GTR submission records
- **[ReportingObligation.json](./ReportingObligation.json)** - Regulatory reporting requirements tracking
- **[SubmissionStatus.json](./SubmissionStatus.json)** - Detailed submission attempt tracking
- **[AuditTrail.json](./AuditTrail.json)** - Immutable audit records for all entity changes

### Processing Control Entities
Entities coordinating system operations and data quality:

- **[ProcessingBatch.json](./ProcessingBatch.json)** - Batch processing coordination
- **[ValidationResult.json](./ValidationResult.json)** - Data validation outcomes
- **[ReconciliationResult.json](./ReconciliationResult.json)** - Reconciliation process results

## Common Schema Components

### Shared Components (in ./common/)
- **[BusinessEntityMetadata.json](./common/BusinessEntityMetadata.json)** - Extended metadata with workflow state
- **[WorkflowState.json](./common/WorkflowState.json)** - Workflow state management fields
- **[MonetaryAmount.json](./common/MonetaryAmount.json)** - Standardized monetary values
- **[DateRange.json](./common/DateRange.json)** - Date range representation
- **[RegulatoryClassification.json](./common/RegulatoryClassification.json)** - Regulatory classification types

## Key Features

### Workflow State Management
All business entities include comprehensive workflow state management:
- Current state tracking with timestamps
- State transition history with actor information
- Support for manual and automated transitions
- Compliance with Cyoda WorkflowConfiguration.json schema

### Data Integrity
- Required field validation for critical business data
- Format validation using JSON Schema patterns
- Cross-reference validation between related entities
- Regulatory compliance validation rules

### Audit and Compliance
- Complete audit trail for all entity changes
- Immutable audit records with digital signatures
- Regulatory reporting requirements tracking
- Data retention and archival support

### Performance Optimization
- Efficient schema structure for high-volume processing
- Business key indexing support
- Minimal required fields to reduce overhead
- Extensible design for future requirements

## Entity Relationships

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

## Workflow State Machines

Each entity follows predefined workflow state machines:

### Trade Lifecycle
```
new → validating → validated → confirmed → active → [amended|cancelled|matured]
```

### Position Lifecycle  
```
calculating → calculated → reporting → reported → reconciled
```

### Regulatory Report Lifecycle
```
generating → generated → validated → submitted → acknowledged → completed
```

### Processing Batch Lifecycle
```
scheduled → running → [completed|failed] → archived
```

## Validation Rules

### Schema Validation
- JSON Schema Draft 2020-12 compliance
- Required field validation
- Format and pattern validation
- Type and constraint validation

### Business Rule Validation
- Cross-entity reference integrity
- Workflow state transition validity
- Business logic consistency
- Regulatory compliance requirements

### Data Quality Validation
- Completeness checks
- Accuracy validation
- Consistency verification
- Timeliness requirements

## Usage Examples

### Creating a New Trade Entity
```json
{
  "metadata": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "workflowState": {
      "state": "new",
      "stateTimestamp": "2024-01-15T10:00:00Z"
    },
    "businessKey": "TRADE-2024-001",
    "version": 1,
    "lastModifiedBy": "system",
    "lastModifiedDate": "2024-01-15T10:00:00Z"
  },
  "tradeId": "TRADE-2024-001",
  "uti": "1234567890123456789012345678901234567890123456789012",
  "tradeDate": "2024-01-15",
  "effectiveDate": "2024-01-17",
  "counterparties": {
    "partyA": {
      "counterpartyId": "CP-001",
      "lei": "213800QILIUD4ROSUO03",
      "role": "PAYER"
    },
    "partyB": {
      "counterpartyId": "CP-002", 
      "lei": "549300VBWWV6BYQOWM67",
      "role": "RECEIVER"
    }
  },
  "product": {
    "productId": "PROD-IRS-001",
    "assetClass": "INTEREST_RATE",
    "productType": "SWAP"
  },
  "economicTerms": {
    "notionalAmount": {
      "amount": 10000000,
      "currency": "USD"
    }
  },
  "clearing": {
    "cleared": false
  },
  "regulatoryReporting": {
    "reportingCounterparty": "213800QILIUD4ROSUO03",
    "reportingRegimes": ["DODD_FRANK"]
  },
  "tradeStatus": "NEW"
}
```

### Querying Entities by Workflow State
```javascript
// Find all trades in 'confirmed' state
db.trades.find({
  "metadata.workflowState.state": "confirmed"
});

// Find positions requiring reporting
db.positions.find({
  "reportingStatus": "PENDING_REPORTING"
});
```

## Performance Considerations

### Indexing Strategy
Recommended indexes for optimal performance:
```javascript
// Business key indexes
db.trades.createIndex({"metadata.businessKey": 1});
db.positions.createIndex({"metadata.businessKey": 1});

// Workflow state indexes  
db.trades.createIndex({"metadata.workflowState.state": 1});
db.positions.createIndex({"reportingStatus": 1});

// Cross-reference indexes
db.trades.createIndex({"counterparties.partyA.counterpartyId": 1});
db.trades.createIndex({"product.productId": 1});

// Date-based indexes
db.trades.createIndex({"tradeDate": 1});
db.positions.createIndex({"calculationTimestamp": 1});
```

### Query Optimization
- Use business keys for entity lookups
- Filter by workflow state for processing queries
- Use compound indexes for complex queries
- Implement proper pagination for large result sets

## Error Handling

### Schema Validation Errors
```json
{
  "error": "SCHEMA_VALIDATION_FAILED",
  "details": [
    {
      "field": "counterparties.partyA.lei",
      "message": "LEI format is invalid",
      "expectedPattern": "^[A-Z0-9]{18}[0-9]{2}$"
    }
  ]
}
```

### Business Rule Violations
```json
{
  "error": "BUSINESS_RULE_VIOLATION", 
  "details": [
    {
      "rule": "COUNTERPARTY_ACTIVE_CHECK",
      "message": "Referenced counterparty is not in active state",
      "counterpartyId": "CP-001",
      "currentState": "suspended"
    }
  ]
}
```

## Testing

### Schema Validation Testing
```bash
# Validate schema files
ajv validate -s Counterparty.json -d test-data/counterparty-valid.json
ajv validate -s Trade.json -d test-data/trade-valid.json
```

### Integration Testing
```javascript
// Test entity creation and workflow transitions
const trade = await createTrade(tradeData);
await transitionTrade(trade.id, 'validate');
assert.equal(trade.metadata.workflowState.state, 'validated');
```

## Documentation

- **[schema-documentation.md](./schema-documentation.md)** - Comprehensive field documentation and business rules
- **[schema-versioning-strategy.md](./schema-versioning-strategy.md)** - Schema evolution and migration strategy

## Support

For questions or issues with the business entity schemas:

1. Check the comprehensive documentation in `schema-documentation.md`
2. Review the versioning strategy in `schema-versioning-strategy.md`
3. Examine the example usage patterns above
4. Contact the development team for schema-related questions

## Contributing

When modifying schemas:

1. Follow the versioning strategy guidelines
2. Update documentation for any changes
3. Add appropriate validation rules
4. Test with representative data
5. Update this README if needed

All schema changes must be reviewed and approved through the established governance process.
