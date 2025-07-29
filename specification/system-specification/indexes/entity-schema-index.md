# Entity Schema Index

## Overview

This index provides a comprehensive catalog of all entity schemas in the DTCC Regulatory Reporting System, organized by category with descriptions, relationships, and cross-references to related workflows and specifications.

---

## 📊 Entity Categories

### Core Business Entities
Primary entities representing the main business objects in the system.

| Entity | Schema Location | Description | Related Workflows | Key Relationships |
|--------|----------------|-------------|-------------------|-------------------|
| **Trade** | `../schema/entity/trade.json` | OTC derivatives trade representation | Trade Processing, Regulatory Reporting | → TradeConfirmation, → Position, → RegulatoryReport |
| **Position** | `../schema/entity/position.json` | Aggregated position data | Position Management, Position Reporting | ← Trade, → PositionReport |
| **TradeConfirmation** | `../schema/entity/trade-confirmation.json` | Trade confirmation and matching | Trade Confirmation, Amendment Processing | ← Trade, → Amendment |
| **RegulatoryReport** | `../schema/entity/regulatory-report.json` | DTCC GTR regulatory reports | Regulatory Reporting, Report Submission | ← Trade, ← Position |

### Reference Data Entities
Entities providing reference and master data for business processing.

| Entity | Schema Location | Description | Related Workflows | Key Relationships |
|--------|----------------|-------------|-------------------|-------------------|
| **Counterparty** | `../schema/entity/counterparty.json` | Trading counterparty information | Counterparty Management, LEI Validation | → Trade, → Position |
| **Product** | `../schema/entity/product.json` | Financial product definitions | Product Management, Reference Data | → Trade |
| **Currency** | `../schema/entity/currency.json` | Currency reference data | Reference Data Management | → Trade, → Position |
| **LegalEntity** | `../schema/entity/legal-entity.json` | Legal entity information with LEI | LEI Validation, Entity Management | → Counterparty |

### Processing Control Entities
Entities managing workflow state and processing control.

| Entity | Schema Location | Description | Related Workflows | Key Relationships |
|--------|----------------|-------------|-------------------|-------------------|
| **ProcessingStatus** | `../schema/entity/processing-status.json` | Entity processing state tracking | All Workflows | → All Business Entities |
| **WorkflowExecution** | `../schema/entity/workflow-execution.json` | Workflow execution tracking | Workflow Management | → ProcessingStatus |
| **ValidationResult** | `../schema/entity/validation-result.json` | Validation outcome tracking | Data Quality, Validation | → All Business Entities |
| **ErrorRecord** | `../schema/entity/error-record.json` | Error and exception tracking | Error Handling, Monitoring | → All Entities |

### Audit and Compliance Entities
Entities supporting audit trails and regulatory compliance.

| Entity | Schema Location | Description | Related Workflows | Key Relationships |
|--------|----------------|-------------|-------------------|-------------------|
| **AuditTrail** | `../schema/entity/audit-trail.json` | Complete audit trail records | Audit Management, Compliance | → All Business Entities |
| **ComplianceCheck** | `../schema/entity/compliance-check.json` | Regulatory compliance validation | Compliance Validation | → Trade, → RegulatoryReport |
| **ReportingObligation** | `../schema/entity/reporting-obligation.json` | Regulatory reporting requirements | Regulatory Reporting | → Trade, → RegulatoryReport |

---

## 🔗 Entity Relationships

### Primary Entity Flow
```
FpML Input → Trade → TradeConfirmation → Position → RegulatoryReport → DTCC GTR
```

### Reference Data Dependencies
```
LegalEntity → Counterparty → Trade
Product → Trade
Currency → Trade/Position
```

### Processing Control Flow
```
Entity → ProcessingStatus → WorkflowExecution → ValidationResult
```

### Audit and Compliance Flow
```
All Entities → AuditTrail → ComplianceCheck → ReportingObligation
```

---

## 📋 Schema Documentation Cross-References

### Detailed Entity Documentation
- **[Entity Overview](../entities/entity-overview.md)** - Complete entity landscape and categorization
- **[Entity Relationship Diagram](../entities/entity-relationship-diagram.mmd)** - Visual entity relationships
- **[Schema Documentation](../entities/schema-documentation.md)** - Detailed schema specifications
- **[Schema Versioning Strategy](../entities/schema-versioning-strategy.md)** - Entity evolution and versioning

### Schema Validation
- **[Entity Validation Summary](../entities/VALIDATION_SUMMARY.md)** - Current schema validation status
- **[Schema Compliance Validation](../workflows/schema-compliance-validation.md)** - Schema compliance procedures

---

## ⚙️ Workflow Integration

### Entity-to-Workflow Mapping

#### Trade Entity Workflows
- **Trade Processing** - Initial trade creation and validation
- **Trade Confirmation** - Trade matching and confirmation
- **Amendment Processing** - Trade modification handling
- **Cancellation Processing** - Trade cancellation workflows

#### Position Entity Workflows  
- **Position Management** - Position calculation and maintenance
- **Position Reporting** - Regulatory position reporting
- **Reconciliation Processing** - Position reconciliation workflows

#### Regulatory Report Entity Workflows
- **Regulatory Reporting** - Report generation and submission
- **Report Submission** - DTCC GTR submission workflows
- **Error Handling** - Report error processing and resubmission

### Workflow Cross-References
- **[Workflow State Machines](../workflows/workflow-state-machines.md)** - Complete workflow definitions
- **[Use Case to Workflow Mapping](../workflows/use-case-workflow-mapping.md)** - Business use case traceability
- **[Workflow Dependencies](../workflows/workflow-dependencies.md)** - Inter-workflow relationships

---

## 🔧 Functional Component Integration

### Entity-Related Criteria
Criteria components that evaluate entity states and properties:

#### Business Logic Criteria
- **TradeValidationCriterion** - Trade business rule validation
- **PositionThresholdCriterion** - Position reporting threshold evaluation
- **CounterpartyEligibilityCriterion** - Counterparty eligibility assessment

#### Data Quality Criteria
- **EntityCompletenessCriterion** - Entity data completeness validation
- **ReferenceDataConsistencyCriterion** - Reference data consistency checks
- **DataQualityScoreCriterion** - Overall data quality assessment

### Entity-Related Processors
Processor components that transform and manage entities:

#### Trade Management Processors
- **TradeCreationProcessor** - Trade entity creation from FpML
- **TradeAmendmentProcessor** - Trade modification processing
- **TradeCancellationProcessor** - Trade cancellation handling

#### Position Management Processors
- **PositionCalculationProcessor** - Position aggregation and calculation
- **PositionReportingProcessor** - Position report generation
- **PositionReconciliationProcessor** - Position reconciliation processing

### Functional Specifications Cross-References
- **[Functional Specifications Index](functional-specifications-index.md)** - Complete component catalog
- **[Component Interaction Diagrams](../functional-specifications/component-interaction-diagrams.md)** - Component interaction patterns

---

## 📡 External Interface Integration

### Entity-Related External Interfaces

#### DTCC GTR Integration
- **Trade Reports** - Trade entity submission to DTCC GTR
- **Position Reports** - Position entity submission to DTCC GTR
- **Acknowledgments** - DTCC GTR response processing

#### Reference Data Integration
- **LEI Registry** - Legal entity validation for Counterparty entities
- **Market Data Services** - Currency and product reference data
- **Holiday Calendar** - Business date calculation for all entities

### External Interface Cross-References
- **[External Interfaces Index](external-interfaces-index.md)** - Complete interface catalog
- **[External Interface Specifications](../../external-interfaces/external-interface-specifications.md)** - Detailed interface specifications

---

## 📊 Performance and Requirements

### Entity-Specific Performance Requirements

#### Processing Volume Requirements
- **Trade Entities**: 100,000+ trades per day
- **Position Entities**: 50,000+ positions per day  
- **Regulatory Reports**: 25,000+ reports per day

#### Processing Time Requirements
- **Trade Creation**: < 5 seconds per trade
- **Position Calculation**: < 10 seconds per position
- **Report Generation**: < 30 seconds per report

### Requirements Cross-References
- **[Performance Requirements](../requirements/performance-requirements.md)** - Detailed performance specifications
- **[Timing Requirements and SLAs](../requirements/timing-requirements-slas.md)** - Regulatory timing requirements

---

## 🔍 Entity Search and Discovery

### Schema Location Patterns
```
../schema/entity/{entity-name}.json
../schema/model/{model-name}.json
../schema/processing/{processing-component}.json
```

### Entity Naming Conventions
- **PascalCase** for entity names (e.g., TradeConfirmation)
- **camelCase** for property names (e.g., tradeDate)
- **UPPER_CASE** for enumeration values (e.g., TRADE_STATUS_CONFIRMED)

### Schema Discovery Tools
- **[Naming Conventions](../standards/naming-conventions.md)** - Entity naming standards
- **[Schema Directory](../../schema/)** - Complete schema repository

---

## 📝 Maintenance and Updates

### Schema Evolution Process
1. **Schema Modification** - Update entity schema definitions
2. **Validation** - Run schema compliance validation
3. **Documentation Update** - Update entity documentation
4. **Cross-Reference Update** - Update related workflow and component references

### Version Control
- **Schema Versioning** - Semantic versioning for schema changes
- **Backward Compatibility** - Maintain compatibility where possible
- **Migration Guidance** - Provide migration paths for breaking changes

### Maintenance Cross-References
- **[Schema Versioning Strategy](../entities/schema-versioning-strategy.md)** - Entity evolution guidelines
- **[Workflow Validation Report](../workflows/VALIDATION_REPORT.md)** - Current validation status

---

*This entity schema index provides comprehensive coverage of all entities in the DTCC Regulatory Reporting System. For specific schema details, refer to the individual schema files or the detailed entity documentation.*
