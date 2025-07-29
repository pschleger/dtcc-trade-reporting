# DTCC Regulatory Reporting System

A proof-of-concept project exploring how far one can build a DTCC regulatory trade reporting system entirely with AI — from requirements and specifications to implementation and testing. The human acts primarily as an orchestrator, intervening only as a last resort.

---

## 🚀 System Overview

- **Entity-Driven Processing**: Transform trades through defined entity lifecycles using Cyoda workflows
- **Event-Driven Architecture**: React to business events with appropriate state machine transitions
- **Regulatory Compliance**: Meet DTCC GTR reporting requirements and deadlines
- **Scalable Processing**: Handle high-volume trade processing with SLA compliance
- **Comprehensive Monitoring**: Track system performance and business metrics with full audit trails

### Key Capabilities

- Process **FpML trade confirmations** into structured Trade entities
- Maintain **position calculations** and reconciliation workflows
- Generate and submit **regulatory reports** to DTCC GTR
- Manage **master data** (counterparties, products, legal entities)
- Handle **trade lifecycle events** (amendments, cancellations, maturities)
- Provide **exception handling** and manual intervention workflows

---

## 🛠️ Getting Started

> ☕ **Java 21 Required**  
> Make sure Java 21 is installed and set as the active version.

### 1. Clone the Project

```bash
git clone <your-repository-URL>
cd dtcc-regulatory-reporting
```

### 2. 🧰 Run Workflow Import Tool

Import the DTCC regulatory reporting workflows into Cyoda:

#### Option 1: Run via Gradle (recommended for local development)
```bash
./gradlew runApp -PmainClass=com.java_template.common.tool.WorkflowImportTool
```

#### Option 2: Build and Run JAR (recommended for CI or scripting)
```bash
./gradlew bootJarWorkflowImport
java -jar build/libs/dtcc-regulatory-reporting-1.0-SNAPSHOT-workflow-import.jar
```

### 3. ▶️ Run the Application

#### Option 1: Run via Gradle
```bash
./gradlew runApp
```

#### Option 2: Run Manually After Build
```bash
./gradlew build
java -jar build/libs/dtcc-regulatory-reporting-1.0-SNAPSHOT.jar
```

> Access the app: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🏗️ System Architecture

### Core Business Entities

#### 1. Master Data Entities
- **Counterparty**: Legal entity information with KYC/AML workflows
- **Product**: OTC derivatives product definitions with validation workflows  
- **LegalEntity**: Organizational structure with LEI validation workflows

#### 2. Trade Processing Entities
- **TradeConfirmation**: FpML message processing with validation workflows
- **Trade**: Core trade representation with lifecycle management workflows
- **Position**: Aggregated position calculations with reconciliation workflows

#### 3. Regulatory Entities
- **RegulatoryReport**: DTCC GTR reports with generation and submission workflows
- **ReportingObligation**: Compliance tracking with monitoring workflows
- **SubmissionStatus**: Report delivery tracking with retry workflows
- **AuditTrail**: Compliance history with archival workflows

### Workflow-Driven Processing

All business logic is implemented through **Cyoda workflows** using finite state machines:

- **Processors**: Implement `CyodaProcessor` interface for business logic execution
- **Criteria**: Implement `CyodaCriterion` interface for decision-making logic
- **State Machines**: Define entity lifecycles with transitions and validations
- **Event Processing**: React to business events through workflow orchestration

---

## 🧩 Project Structure

### `application/entity/`
Domain entities implementing `CyodaEntity`:

- `counterparty/` – Counterparty entity and workflow
- `trade/` – Trade entity and lifecycle workflow  
- `position/` – Position entity and calculation workflow
- `regulatory_report/` – Regulatory report entity and submission workflow
- `trade_confirmation/` – FpML confirmation processing workflow

### `application/processor/`
Workflow processors implementing `CyodaProcessor`:

- `trade_confirmation/` – FpML validation and conversion processors
- `trade_management/` – Trade lifecycle processors
- `position_management/` – Position calculation processors
- `regulatory_reporting/` – Report generation and submission processors
- `master_data/` – Reference data management processors

### `application/criterion/`
Workflow criteria implementing `CyodaCriterion`:

- `validation/` – Data validation criteria
- `business_rules/` – Regulatory compliance criteria
- `timing/` – SLA and deadline criteria
- `exception_handling/` – Error condition criteria

### `common/`
Cyoda integration framework (rarely modified):

- `service/EntityService.java` – Primary interface for Cyoda interactions
- `workflow/` – Core workflow processing architecture
- `serializer/` – Type-safe entity processing APIs
- `repository/` – Cyoda REST/gRPC integration layer

---

## 📋 Business Use Cases

### Primary Workflows

1. **Trade Confirmation Processing**: Validate and convert FpML messages to Trade entities
2. **Trade Lifecycle Management**: Handle amendments, cancellations, and maturities
3. **Position Calculation**: Aggregate trades into positions with real-time updates
4. **Regulatory Report Generation**: Create DTCC GTR compliant reports
5. **Report Submission**: Submit reports to DTCC GTR with acknowledgment tracking
6. **Master Data Management**: Maintain counterparty and product reference data
7. **Exception Handling**: Process validation failures and business rule violations

### Regulatory Compliance

- **DTCC GTR Requirements**: Full compliance with reporting obligations
- **FpML Standards**: Support for FpML 5.x trade confirmation formats
- **Audit Trails**: Immutable audit logs for all entity state changes
- **SLA Compliance**: Meet regulatory timing requirements for report submission

---

## 🔄 Workflow Configuration

Entity workflows are defined in JSON configuration files:

```
application/entity/$entity_name/Workflow.json
```

Each workflow implements a **finite state machine** with:
- **States**: Business states with defined transitions
- **Processors**: Business logic execution components  
- **Criteria**: Decision-making logic for state transitions
- **Manual Transitions**: Support for manual intervention workflows

Example workflow structure for Trade entity:
- `received` → `validated` → `enriched` → `position_updated` → `reported`

---

## 📊 API Reference

### EntityService Methods

| Method | Description |
|--------|-------------|
| `getItem(model, version, technicalId)` | Retrieve entity by ID |
| `getItemsByCondition(model, version, condition)` | Query entities with conditions |
| `addItem(model, version, entity)` | Create new entity |
| `updateItem(model, version, technicalId, entity)` | Update existing entity |

Use `import static com.java_template.common.config.Config.ENTITY_VERSION` for versioning.

---

## 📚 Documentation

### Comprehensive Specifications
- **[Master Specification](specification/system-specification/master-specification.md)** - Complete system overview
- **[System Architecture](specification/system-specification/architecture/system-architecture.md)** - Technical architecture details
- **[Entity Overview](specification/system-specification/entities/entity-overview.md)** - Business entity definitions
- **[Workflow State Machines](specification/system-specification/workflows/workflow-state-machines.md)** - Complete workflow designs
- **[Implementation Guide](specification/system-specification/implementation-guide.md)** - Technical implementation guidance

### Business Requirements
- **[Business Use Cases](specification/system-specification/business/business-use-cases.md)** - Detailed business scenarios
- **[Functional Specifications](specification/system-specification/functional-specifications/)** - Component specifications
- **[External Interfaces](specification/external-interfaces/)** - Integration specifications



---

*Built on the Cyoda EDBMS platform for entity-driven, workflow-orchestrated regulatory compliance.*
