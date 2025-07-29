# DTCC Regulatory Reporting System - Glossary

## Overview

This glossary provides definitions for terms, acronyms, and concepts used throughout the DTCC Regulatory Reporting System specification. Terms are organized alphabetically with cross-references to related concepts and documentation.

---

## A

**Amendment**
A modification to an existing trade that changes one or more trade attributes such as notional amount, maturity date, or terms. Amendments must be reported to regulatory authorities within specified timeframes.
*See also: [Amendment Processing](workflows/workflow-state-machines.md#7-amendment-workflow), [Trade Processing](business/business-use-cases.md#1-trade-processing-use-cases)*

**API Gateway**
The entry point for all external API requests, providing authentication, authorization, rate limiting, and request routing to internal services.
*See also: [System Architecture](architecture/system-architecture.md), [External Interfaces](external-interfaces/external-interface-specifications.md)*

**Audit Trail**  
A complete chronological record of all system activities, entity changes, and business transactions maintained for regulatory compliance and operational monitoring.  
*See also: [Audit Management](events/event-traceability-audit.md), [Compliance](requirements/timing-requirements-slas.md)*

---

## B

**Batch Processing**
Processing mode where multiple entities or transactions are processed together in groups, typically used for high-volume operations and end-of-day processing.
*See also: [Batch Processing Workflow](workflows/workflow-state-machines.md#10-processingbatch-workflow), [Performance Requirements](requirements/performance-requirements.md)*

**Business Event**  
An occurrence in the business domain that triggers system processing, such as trade confirmation receipt or position threshold breach.  
*See also: [Event Catalog](events/event-catalog.md), [Business Events](events/business-events.md)*

**Business Rule**  
A statement that defines or constrains business operations, implemented as criteria components in workflows to ensure compliance and consistency.  
*See also: [Business Rules](business/decision-points-business-rules.md), [Criteria Components](functional-specifications/README.md)*

---

## C

**Cancellation**
The complete reversal of a previously confirmed trade, removing all associated obligations and positions.
*See also: [Cancellation Processing](workflows/workflow-state-machines.md#8-cancellation-workflow), [Trade Processing](business/business-use-cases.md#1-trade-processing-use-cases)*

**Counterparty**
The other party in a financial transaction or trade, identified by Legal Entity Identifier (LEI) and subject to eligibility and credit checks.
*See also: [Counterparty Management](functional-specifications/processors/counterparty-management/), [LEI Validation](external-interfaces/external-interface-specifications.md#lei-registry)*

**Criteria Component**
A functional component that evaluates business logic, data quality, or processing conditions to determine workflow paths and decisions.
*See also: [Functional Specifications](functional-specifications/README.md), [Criteria Categories](functional-specifications/criteria/)*

**Cyoda EDBMS**  
Entity Database Management System - the underlying platform providing entity-driven architecture, workflow processing, and data management capabilities.  
*See also: [Entity-Driven Architecture](architecture/entity-driven-architecture.md), [System Architecture](architecture/system-architecture.md)*

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
*See also: [Entity Overview](entities/entity-overview.md), [Entity Schema Index](indexes/entity-schema-index.md)*

**Entity-Driven Architecture**  
An architectural pattern where business entities are the primary organizing principle, with workflows and processing organized around entity lifecycles.  
*See also: [Entity-Driven Architecture](architecture/entity-driven-architecture.md), [System Architecture](architecture/system-architecture.md)*

**Event**  
A significant occurrence in the system that triggers processing, such as business events, system events, or external events.  
*See also: [Event Catalog](events/event-catalog.md), [Event-Driven Architecture](events/event-driven-architecture.md)*

**Event-Driven Architecture**  
An architectural pattern where system components communicate through events, enabling loose coupling and scalable processing.  
*See also: [Event-Driven Architecture](events/event-driven-architecture.md), [Event Processing](events/event-architecture-patterns.md)*

---

## F

**FpML**
Financial products Markup Language - an XML-based standard for representing financial derivatives and structured products.
*See also: [Trade Processing](functional-specifications/processors/trade-management/), [External Interfaces](external-interfaces/external-interface-specifications.md)*

**Functional Component**  
A reusable software component that implements specific business logic, either as a Criteria (decision-making) or Processor (data transformation) component.  
*See also: [Functional Specifications](functional-specifications/README.md), [Component Architecture](functional-specifications/component-interaction-diagrams.md)*

---

## G

**GLEIF**
Global Legal Entity Identifier Foundation - the organization that maintains the global LEI registry for legal entity identification.
*See also: [LEI Registry](external-interfaces/external-interface-specifications.md#lei-registry), [LEI Validation](functional-specifications/processors/reference-data-management/)*

**GTR**  
Global Trade Repository - see DTCC GTR.

---

## L

**LEI**
Legal Entity Identifier - a 20-character alphanumeric code that uniquely identifies legal entities participating in financial transactions.
*See also: [LEI Validation](external-interfaces/external-interface-specifications.md#lei-registry), [Counterparty Management](functional-specifications/processors/counterparty-management/)*

**Lifecycle Event**
A significant change in an entity's state or attributes, such as trade amendment, cancellation, or maturity.
*See also: [Trade Processing](business/business-use-cases.md#1-trade-processing-use-cases), [Entity Lifecycle](entities/entity-overview.md)*

---

## O

**OTC**
Over-The-Counter - financial transactions conducted directly between parties without going through a centralized exchange.
*See also: [Trade Processing](functional-specifications/processors/trade-management/), [Regulatory Reporting](business/business-use-cases.md#3-regulatory-reporting-use-cases)*

---

## P

**Position**
An aggregated view of trades and exposures for a specific product, counterparty, or portfolio, calculated for risk management and regulatory reporting.
*See also: [Position Management](functional-specifications/processors/position-management/), [Position Entity](entities/entity-overview.md)*

**Processor Component**
A functional component that performs data transformation, business process execution, or system integration operations.
*See also: [Functional Specifications](functional-specifications/README.md), [Processor Categories](functional-specifications/processors/)*

---

## R

**Reconciliation**
The process of comparing and aligning data from different sources to identify and resolve discrepancies.
*See also: [Position Reconciliation](business/business-use-cases.md#2-position-management-use-cases), [Reconciliation Processing](functional-specifications/processors/reconciliation-processing/)*

**Reference Data**
Master data that provides context and validation for business transactions, including counterparty information, product specifications, and market data.
*See also: [Reference Data Management](functional-specifications/processors/reference-data-management/), [Reference Data Entities](entities/entity-overview.md)*

**Regulatory Report**
A structured report submitted to regulatory authorities containing trade, position, or transaction information as required by regulations.
*See also: [Regulatory Reporting](functional-specifications/processors/regulatory-reporting/), [DTCC GTR](external-interfaces/external-interface-specifications.md#dtcc-gtr)*

**Reporting Obligation**  
A regulatory requirement to report specific transactions or positions to authorities within defined timeframes and formats.  
*See also: [Regulatory Compliance](business/decision-points-business-rules.md), [Timing Requirements](requirements/timing-requirements-slas.md)*

---

## S

**SLA**  
Service Level Agreement - a commitment to specific performance metrics such as response time, availability, or throughput.  
*See also: [Performance Requirements](requirements/performance-requirements.md), [Timing Requirements](requirements/timing-requirements-slas.md)*

**State Machine**  
A model that defines the possible states of an entity and the valid transitions between states, used to control workflow processing.  
*See also: [Workflow State Machines](workflows/workflow-state-machines.md), [Entity Lifecycle](entities/entity-overview.md)*

**Swimlane Diagram**
A visual representation showing the flow of processes across different systems or organizational boundaries.
*See also: [Swimlane Diagrams](events/), [Process Flows](events/)*

---

## T

**T+1**  
Trade date plus one business day - the standard timeframe for regulatory reporting of OTC derivatives transactions.  
*See also: [Timing Requirements](requirements/timing-requirements-slas.md), [Regulatory Compliance](business/decision-points-business-rules.md)*

**Trade**
A financial transaction between counterparties involving the exchange of financial instruments or obligations.
*See also: [Trade Entity](entities/entity-overview.md), [Trade Processing](functional-specifications/processors/trade-management/)*

**Trade Confirmation**
The process of verifying and matching trade details between counterparties to ensure agreement on transaction terms.
*See also: [Trade Confirmation](functional-specifications/processors/trade-confirmation/), [Trade Processing](business/business-use-cases.md#1-trade-processing-use-cases)*

---

## V

**Validation**
The process of checking data, business rules, or system states to ensure compliance with requirements and constraints.
*See also: [Validation Criteria](functional-specifications/criteria/validation-criteria/), [Data Quality](functional-specifications/criteria/data-quality-criteria/)*

---

## W

**Workflow**  
A defined sequence of processing steps that transform entities through their lifecycle states, implemented using criteria and processor components.  
*See also: [Workflow State Machines](workflows/workflow-state-machines.md), [Workflow Index](indexes/workflow-index.md)*

**Workflow Engine**  
The system component responsible for executing workflows, managing state transitions, and coordinating component execution.  
*See also: [System Architecture](architecture/system-architecture.md), [Workflow Management](workflows/workflow-dependencies.md)*

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
- [System Architecture](architecture/system-architecture.md)
- [Entity-Driven Architecture](architecture/entity-driven-architecture.md)
- [Event-Driven Architecture](events/event-driven-architecture.md)

### Business Domain
- [Business Use Cases](business/business-use-cases.md)
- [Decision Points and Business Rules](business/decision-points-business-rules.md)
- [Entity Overview](entities/entity-overview.md)

### Technical Implementation
- [Workflow State Machines](workflows/workflow-state-machines.md)
- [Functional Specifications](functional-specifications/README.md)
- [External Interfaces](external-interfaces/external-interface-specifications.md)

### Operations and Monitoring
- [Performance Requirements](requirements/performance-requirements.md)
- [Timing Requirements and SLAs](requirements/timing-requirements-slas.md)
- [Event Monitoring](events/event-traceability-audit.md)

### Standards and Guidelines
- [Naming Conventions](standards/naming-conventions.md)
- [Cyoda Design Principles](../Background/cyoda-design-principles.md)
- [Workflow Configuration Guide](../Background/workflow-config-guide.md)

---

*This glossary provides comprehensive coverage of terms and concepts used throughout the DTCC Regulatory Reporting System specification. For additional context, refer to the cross-referenced documentation sections.*
