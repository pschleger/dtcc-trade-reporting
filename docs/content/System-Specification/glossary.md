# DTCC Regulatory Reporting System - Glossary

## Overview

This glossary provides definitions for terms, acronyms, and concepts used throughout the DTCC Regulatory Reporting System specification. Terms are organized alphabetically with cross-references to related concepts and documentation.

---

## A

**Amendment**
A modification to an existing trade that changes one or more trade attributes such as notional amount, maturity date, or terms. Amendments must be reported to regulatory authorities within specified timeframes.
*See also: [Amendment Processing](/content/System-Specification/workflows/workflow-state-machines/#7-amendment-workflow), [Trade Processing](/content/System-Specification/business/business-use-cases/#1-trade-processing-use-cases)*

**API Gateway**
The entry point for all external API requests, providing authentication, authorization, rate limiting, and request routing to internal services.
*See also: [System Architecture](/content/System-Specification/architecture/system-architecture/), [External Interfaces](/content/System-Specification/external-interfaces/external-interface-specifications/)*

**Audit Trail**
A complete chronological record of all system activities, entity changes, and business transactions maintained for regulatory compliance and operational monitoring.
*See also: [Audit Management](/content/System-Specification/events/event-traceability-audit/), [Compliance](/content/System-Specification/requirements/timing-requirements-slas/)*

---

## B

**Batch Processing**
Processing mode where multiple entities or transactions are processed together in groups, typically used for high-volume operations and end-of-day processing.
*See also: [Batch Processing Workflow](/content/System-Specification/workflows/workflow-state-machines/#10-processingbatch-workflow), [Performance Requirements](/content/System-Specification/requirements/performance-requirements/)*

**Business Event**
An occurrence in the business domain that triggers system processing, such as trade confirmation receipt or position threshold breach.
*See also: [Event Catalog](/content/System-Specification/events/event-catalog/), [Business Events](/content/System-Specification/events/business-events/)*

**Business Rule**
A statement that defines or constrains business operations, implemented as criteria components in workflows to ensure compliance and consistency.
*See also: [Business Rules](/content/System-Specification/business/decision-points-business-rules/), [Criteria Components](/content/System-Specification/functional-specifications/)*

---

## C

**Cancellation**
The complete reversal of a previously confirmed trade, removing all associated obligations and positions.
*See also: [Cancellation Processing](workflows/workflow-state-machines.md#8-cancellation-workflow), [Trade Processing](business/business-use-cases.md#1-trade-processing-use-cases)*

**Counterparty**
The other party in a financial transaction or trade, identified by Legal Entity Identifier (LEI) and subject to eligibility and credit checks.
*See also: [Counterparty Management](/content/System-Specification/functional-specifications/processors/counterparty-management/), [LEI Validation](/content/System-Specification/external-interfaces/external-interface-specifications/#lei-registry)*

**Criteria Component**
A functional component that evaluates business logic, data quality, or processing conditions to determine workflow paths and decisions.
*See also: [Functional Specifications](/content/System-Specification/functional-specifications/), [Criteria Categories](/content/System-Specification/functional-specifications/criteria/)*

**Cyoda EDBMS**
Entity Database Management System - the underlying platform providing entity-driven architecture, workflow processing, and data management capabilities.
*See also: [Entity-Driven Architecture](/content/System-Specification/architecture/entity-driven-architecture/), [System Architecture](/content/System-Specification/architecture/system-architecture/)*

---

## D

**Data Quality**
The measure of data accuracy, completeness, consistency, and timeliness, monitored and maintained through automated validation and quality scoring.
*See also: [Data Quality Validation](functional-specifications/criteria/data-quality-criteria/), [Validation Criteria](functional-specifications/criteria/validation-criteria/)*

**DTCC GTR**
Depository Trust & Clearing Corporation Global Trade Repository - the regulatory repository for OTC derivatives trade reporting.
*See also: [DTCC Integration](external-interfaces/external-interface-specifications.md#dtcc-gtr), [Regulatory Reporting](functional-specifications/processors/regulatory-reporting/)*

---

## E

**Entity**
A business object with defined structure, lifecycle, and relationships, such as Trade, Position, or Counterparty, managed through the Cyoda EDBMS.
*See also: [Entity Overview](/content/System-Specification/entities/entity-overview/), [Entity Schema Index](/content/System-Specification/indexes/entity-schema-index/)*

**Entity-Driven Architecture**
An architectural pattern where business entities are the primary organizing principle, with workflows and processing organized around entity lifecycles.
*See also: [Entity-Driven Architecture](/content/System-Specification/architecture/entity-driven-architecture/), [System Architecture](/content/System-Specification/architecture/system-architecture/)*

**Event**
A significant occurrence in the system that triggers processing, such as business events, system events, or external events.
*See also: [Event Catalog](/content/System-Specification/events/event-catalog/), [Event-Driven Architecture](/content/System-Specification/events/event-driven-architecture/)*

**Event-Driven Architecture**
An architectural pattern where system components communicate through events, enabling loose coupling and scalable processing.
*See also: [Event-Driven Architecture](/content/System-Specification/events/event-driven-architecture/), [Event Processing](/content/System-Specification/events/event-architecture-patterns/)*

---

## F

**FpML**
Financial products Markup Language - an XML-based standard for representing financial derivatives and structured products.
*See also: [Trade Processing](/content/System-Specification/functional-specifications/processors/trade-management/), [External Interfaces](/content/System-Specification/external-interfaces/external-interface-specifications/)*

**Functional Component**
A reusable software component that implements specific business logic, either as a Criteria (decision-making) or Processor (data transformation) component.
*See also: [Functional Specifications](/content/System-Specification/functional-specifications/), [Component Architecture](/content/System-Specification/functional-specifications/component-interaction-diagrams/)*

---

## G

**GLEIF**
Global Legal Entity Identifier Foundation - the organization that maintains the global LEI registry for legal entity identification.
*See also: [LEI Registry](/content/System-Specification/external-interfaces/external-interface-specifications/#lei-registry), [LEI Validation](/content/System-Specification/functional-specifications/processors/reference-data-management/)*

**GTR**
Global Trade Repository - see DTCC GTR.

---

## L

**LEI**
Legal Entity Identifier - a 20-character alphanumeric code that uniquely identifies legal entities participating in financial transactions.
*See also: [LEI Validation](/content/System-Specification/external-interfaces/external-interface-specifications/#lei-registry), [Counterparty Management](/content/System-Specification/functional-specifications/processors/counterparty-management/)*

**Lifecycle Event**
A significant change in an entity's state or attributes, such as trade amendment, cancellation, or maturity.
*See also: [Trade Processing](/content/System-Specification/business/business-use-cases/#1-trade-processing-use-cases), [Entity Lifecycle](/content/System-Specification/entities/entity-overview/)*

---

## O

**OTC**
Over-The-Counter - financial transactions conducted directly between parties without going through a centralized exchange.
*See also: [Trade Processing](/content/System-Specification/functional-specifications/processors/trade-management/), [Regulatory Reporting](/content/System-Specification/business/business-use-cases/#3-regulatory-reporting-use-cases)*

---

## P

**Position**
An aggregated view of trades and exposures for a specific product, counterparty, or portfolio, calculated for risk management and regulatory reporting.
*See also: [Position Management](/content/System-Specification/functional-specifications/processors/position-management/), [Position Entity](/content/System-Specification/entities/entity-overview/)*

**Processor Component**
A functional component that performs data transformation, business process execution, or system integration operations.
*See also: [Functional Specifications](/content/System-Specification/functional-specifications/), [Processor Categories](/content/System-Specification/functional-specifications/processors/)*

---

## R

**Reconciliation**
The process of comparing and aligning data from different sources to identify and resolve discrepancies.
*See also: [Position Reconciliation](/content/System-Specification/business/business-use-cases/#2-position-management-use-cases), [Reconciliation Processing](/content/System-Specification/functional-specifications/processors/reconciliation-processing/)*

**Reference Data**
Master data that provides context and validation for business transactions, including counterparty information, product specifications, and market data.
*See also: [Reference Data Management](/content/System-Specification/functional-specifications/processors/reference-data-management/), [Reference Data Entities](/content/System-Specification/entities/entity-overview/)*

**Regulatory Report**
A structured report submitted to regulatory authorities containing trade, position, or transaction information as required by regulations.
*See also: [Regulatory Reporting](/content/System-Specification/functional-specifications/processors/regulatory-reporting/), [DTCC GTR](/content/System-Specification/external-interfaces/external-interface-specifications/#dtcc-gtr)*

**Reporting Obligation**
A regulatory requirement to report specific transactions or positions to authorities within defined timeframes and formats.
*See also: [Regulatory Compliance](/content/System-Specification/business/decision-points-business-rules/), [Timing Requirements](/content/System-Specification/requirements/timing-requirements-slas/)*

---

## S

**SLA**
Service Level Agreement - a commitment to specific performance metrics such as response time, availability, or throughput.
*See also: [Performance Requirements](/content/System-Specification/requirements/performance-requirements/), [Timing Requirements](/content/System-Specification/requirements/timing-requirements-slas/)*

**State Machine**
A model that defines the possible states of an entity and the valid transitions between states, used to control workflow processing.
*See also: [Workflow State Machines](/content/System-Specification/workflows/workflow-state-machines/), [Entity Lifecycle](/content/System-Specification/entities/entity-overview/)*

**Swimlane Diagram**
A visual representation showing the flow of processes across different systems or organizational boundaries.
*See also: [Swimlane Diagrams](/content/System-Specification/events/), [Process Flows](/content/System-Specification/events/)*

---

## T

**T+1**
Trade date plus one business day - the standard timeframe for regulatory reporting of OTC derivatives transactions.
*See also: [Timing Requirements](/content/System-Specification/requirements/timing-requirements-slas/), [Regulatory Compliance](/content/System-Specification/business/decision-points-business-rules/)*

**Trade**
A financial transaction between counterparties involving the exchange of financial instruments or obligations.
*See also: [Trade Entity](/content/System-Specification/entities/entity-overview/), [Trade Processing](/content/System-Specification/functional-specifications/processors/trade-management/)*

**Trade Confirmation**
The process of verifying and matching trade details between counterparties to ensure agreement on transaction terms.
*See also: [Trade Confirmation](/content/System-Specification/functional-specifications/processors/trade-confirmation/), [Trade Processing](/content/System-Specification/business/business-use-cases/#1-trade-processing-use-cases)*

---

## V

**Validation**
The process of checking data, business rules, or system states to ensure compliance with requirements and constraints.
*See also: [Validation Criteria](/content/System-Specification/functional-specifications/criteria/validation-criteria/), [Data Quality](/content/System-Specification/functional-specifications/criteria/data-quality-criteria/)*

---

## W

**Workflow**
A defined sequence of processing steps that transform entities through their lifecycle states, implemented using criteria and processor components.
*See also: [Workflow State Machines](/content/System-Specification/workflows/workflow-state-machines/), [Workflow Index](/content/System-Specification/indexes/workflow-index/)*

**Workflow Engine**
The system component responsible for executing workflows, managing state transitions, and coordinating component execution.
*See also: [System Architecture](/content/System-Specification/architecture/system-architecture/), [Workflow Management](/content/System-Specification/workflows/workflow-dependencies/)*

---

## Acronyms and Abbreviations

| Acronym | Full Form | Definition |
|---------|-----------|------------|
| **API** | Application Programming Interface | A set of protocols and tools for building software applications |
| **CQRS** | Command Query Responsibility Segregation | An architectural pattern separating read and write operations |
| **CSV** | Comma-Separated Values | A file format for tabular data |
| **DTCC** | Depository Trust & Clearing Corporation | A financial services company providing clearing and settlement |
| **EDBMS** | Entity Database Management System | Cyoda's entity-driven database platform |
| **FpML** | Financial products Markup Language | XML standard for financial derivatives |
| **GLEIF** | Global Legal Entity Identifier Foundation | Organization maintaining the global LEI system |
| **GTR** | Global Trade Repository | Repository for regulatory trade reporting |
| **HTTP** | Hypertext Transfer Protocol | Protocol for web communication |
| **HTTPS** | HTTP Secure | Secure version of HTTP using TLS/SSL |
| **JSON** | JavaScript Object Notation | Lightweight data interchange format |
| **JWT** | JSON Web Token | Standard for securely transmitting information |
| **LEI** | Legal Entity Identifier | 20-character code identifying legal entities |
| **mTLS** | Mutual Transport Layer Security | Two-way authentication using TLS |
| **OAuth** | Open Authorization | Standard for access delegation |
| **OTC** | Over-The-Counter | Direct trading between parties |
| **PKI** | Public Key Infrastructure | Framework for managing digital certificates |
| **REST** | Representational State Transfer | Architectural style for web services |
| **SLA** | Service Level Agreement | Performance commitment |
| **SNMP** | Simple Network Management Protocol | Protocol for network management |
| **TLS** | Transport Layer Security | Cryptographic protocol for secure communication |
| **UUID** | Universally Unique Identifier | 128-bit identifier |
| **XML** | eXtensible Markup Language | Markup language for structured data |

---

## Cross-References by Category

### Architecture and Design
- [System Architecture](/content/System-Specification/architecture/system-architecture/)
- [Entity-Driven Architecture](/content/System-Specification/architecture/entity-driven-architecture/)
- [Event-Driven Architecture](/content/System-Specification/events/event-driven-architecture/)

### Business Domain
- [Business Use Cases](/content/System-Specification/business/business-use-cases/)
- [Decision Points and Business Rules](/content/System-Specification/business/decision-points-business-rules/)
- [Entity Overview](/content/System-Specification/entities/entity-overview/)

### Technical Implementation
- [Workflow State Machines](/content/System-Specification/workflows/workflow-state-machines/)
- [Functional Specifications](/content/System-Specification/functional-specifications/)
- [External Interfaces](/content/System-Specification/external-interfaces/external-interface-specifications/)

### Operations and Monitoring
- [Performance Requirements](/content/System-Specification/requirements/performance-requirements/)
- [Timing Requirements and SLAs](/content/System-Specification/requirements/timing-requirements-slas/)
- [Event Monitoring](/content/System-Specification/events/event-traceability-audit/)

### Standards and Guidelines
- [Naming Conventions](/content/System-Specification/standards/naming-conventions/)
- [Cyoda Design Principles](/content/Background/cyoda-design-principles/)
- [Workflow Configuration Guide](/content/Background/workflow-config-guide/)

---

*This glossary provides comprehensive coverage of terms and concepts used throughout the DTCC Regulatory Reporting System specification. For additional context, refer to the cross-referenced documentation sections.*
