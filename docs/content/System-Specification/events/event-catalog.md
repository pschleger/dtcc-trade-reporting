# DTCC Regulatory Reporting System - Business Event Catalog

## Overview

This catalog documents all external business events that trigger regulatory reporting processes in the DTCC Regulatory Reporting System. These events represent business occurrences from external sources that require regulatory compliance action.

## External Business Events

The DTCC Regulatory Reporting System responds to externally triggered business events. These are the only true "events" in the system - external stimuli that initiate business processes.

### 1. External Trading System Events
Events triggered by external trading systems and counterparties.

| Event Name | Event ID | Description | Source | Criticality |
|------------|----------|-------------|---------|-------------|
| FpMLMessageReceived | `trading.fpml.message.received` | FpML trade confirmation message received | Trading Systems | Critical |
| AmendmentRequested | `trading.amendment.requested` | Trade amendment request received | Trading Systems | High |
| CancellationRequested | `trading.cancellation.requested` | Trade cancellation request received | Trading Systems | High |

### 2. External Regulatory Authority Events
Events triggered by regulatory authorities and compliance systems.

| Event Name | Event ID | Description | Source | Criticality |
|------------|----------|-------------|---------|-------------|
| GTRAcknowledgmentReceived | `gtr.acknowledgment.received` | Report acknowledgment from DTCC GTR | DTCC GTR | Critical |
| GTRRejectionReceived | `gtr.rejection.received` | Report rejection from DTCC GTR | DTCC GTR | Critical |
| ComplianceDeadlineApproaching | `regulatory.deadline.approaching` | Regulatory deadline notification | Compliance Systems | High |

### 3. External Reference Data Events
Events triggered by external reference data providers.

| Event Name | Event ID | Description | Source | Criticality |
|------------|----------|-------------|---------|-------------|
| LEIDataSynchronized | `gleif.lei.data.synchronized` | LEI reference data update received | GLEIF | Medium |
| ReferenceDataUpdated | `reference.data.updated` | Market reference data update received | Data Vendors | Medium |

### 4. External Monitoring Events
Events triggered by external monitoring and operational systems.

| Event Name | Event ID | Description | Source | Criticality |
|------------|----------|-------------|---------|-------------|
| MonitoringAlertTriggered | `monitoring.alert.triggered` | Alert from external monitoring system | Monitoring Systems | High |
| ScheduledProcessTriggered | `scheduled.process.triggered` | Scheduled batch process initiated | Scheduler | Medium |

## Business Event Processing Requirements

### Business Process Correlation
Business events must be correlated to enable end-to-end business process tracking and regulatory audit trails.

**Correlation Requirements:**
- All events within a single business process must be traceable
- Trade lifecycle events must be correlated from confirmation to reporting
- Amendment and cancellation events must be linked to original trades
- Regulatory reporting events must be traceable to triggering business events

### Business Event Sequencing
Business events must be processed in the correct business sequence to ensure data integrity and regulatory compliance.

**Sequencing Requirements:**
- Trade amendments must be processed after original trade confirmation
- Trade cancellations must be processed before settlement date
- Regulatory reports must be generated after all required trade data is available
- Reference data updates must be applied before dependent business processing

## Business Data Requirements

### Event Data Completeness
Business events must contain complete business information required for regulatory reporting.

**Data Requirements:**
- Trade events must include all regulatory data elements
- Counterparty events must include complete legal entity information
- Product events must include full product specification details
- Regulatory events must include jurisdiction and framework information

### Data Quality Standards
Business events must meet data quality standards to ensure regulatory compliance.

**Quality Standards:**
- Business data must be complete (no missing required fields)
- Business data must be accurate (validated against authoritative sources)
- Business data must be timely (received within business deadlines)
- Business data must be consistent (no conflicting information)

## Business Event Retention Requirements

### Regulatory Retention
Business events must be retained to meet regulatory compliance requirements.

**Retention Requirements:**
- Trade events: 7 years retention for regulatory compliance
- Regulatory reporting events: 7 years retention for audit purposes
- Reference data events: 5 years retention for business analysis
- Amendment/cancellation events: 7 years retention for trade history

### Business Access Requirements
Business events must be accessible to authorized business users for analysis and compliance purposes.

**Access Requirements:**
- Compliance officers must have access to all regulatory events
- Trade operations must have access to trade lifecycle events
- Risk managers must have access to position and threshold events
- Auditors must have read-only access to all business events

---

## Detailed Event Specifications

### Business Event Documentation
Business event specifications and requirements:

- **[Event Template](/content/System-Specification/events/event-template/)** - Standardized template for business event specifications
- **[External Business Events](/content/System-Specification/events/business-events/)** - Complete catalog of externally-triggered business events
- **[External Interface Events](/content/System-Specification/events/external-events/)** - Detailed specifications and data structures for external events
- **[Business Event Processing](/content/System-Specification/events/event-driven-architecture/)** - Business requirements for event processing and compliance

### Event Correlation and Validation Documentation
Comprehensive event correlation, validation, and operational support:

- **[Event Correlation Matrix](/content/System-Specification/events/event-correlation-matrix/)** - Complete correlation patterns and relationships between all event types
- **[Event Causality Chains](/content/System-Specification/events/event-causality-chains/)** - Detailed causality relationships showing how events trigger subsequent events
- **[Event Timing Dependencies](/content/System-Specification/events/event-timing-dependencies/)** - Critical path analysis and timing constraints for regulatory compliance
- **[Event Traceability and Audit](/content/System-Specification/events/event-traceability-audit/)** - End-to-end traceability for regulatory audit and compliance purposes
- **[Event Coverage Validation](/content/System-Specification/events/event-coverage-validation/)** - Comprehensive validation against all business requirements and use cases
- **[Event Troubleshooting Guide](/content/System-Specification/events/event-troubleshooting-guide/)** - Operational support guide for diagnosing and resolving event issues
- **[Event Architecture Patterns](/content/System-Specification/events/event-architecture-patterns/)** - Event-driven architecture patterns and best practices

---

## Event Catalog Validation

### Completeness Validation
✅ **External Interface Events**: All externally-triggered events from interface specifications documented
✅ **Trading System Events**: All events from external trading systems documented
✅ **Regulatory Events**: All events from external regulatory authorities documented
✅ **Reference Data Events**: All events from external data providers documented

### Coverage Analysis
- **Total External Events**: 12 distinct externally-triggered events
- **Trading System Events**: 3 events from external trading systems
- **Regulatory Authority Events**: 3 events from regulatory systems
- **Reference Data Events**: 2 events from external data providers
- **Monitoring Events**: 2 events from external monitoring systems
- **Scheduled Events**: 2 events from external schedulers

### Event Philosophy
**Events are external stimuli only:**
- Events represent external triggers that initiate business processes
- Internal processing is handled through workflow state transitions
- System coordination is handled through direct method calls and messaging
- Only externally-triggered business events are considered true "events"

### Error Handling Philosophy
**Error handling is managed through workflow states rather than separate error events:**
- Entities transition to error states (e.g., `validation-failed`, `submission-failed`)
- Workflow provides recovery transitions from error states
- ErrorResolutionTask entities can track complex error resolution processes
- Entity state reflects error status, eliminating need for separate error events

### Audit Trail Philosophy
**Audit trails are maintained through entity transactions rather than separate audit events:**
- All entity state changes are captured in immutable AuditTrail entities
- Entity transaction history provides data lineage
- Workflow state transitions are automatically audited
- No separate audit events needed as transactions serve as audit records

### Schema Validation
✅ **Event Structure**: All events follow standardized CloudEvent schema
✅ **Payload Schemas**: JSON schemas defined for all event payloads
✅ **Metadata Standards**: Consistent metadata structure across all events
✅ **Versioning**: Semantic versioning applied to all event specifications

---

## Business Usage Guidelines

### For Business Process Owners
1. **Process Definition**: Use [External Business Events](/content/System-Specification/events/business-events/) to understand business triggers
2. **Process Requirements**: Reference [Business Event Processing](/content/System-Specification/events/event-driven-architecture/) for processing requirements
3. **Data Requirements**: Ensure business events contain complete regulatory data elements
4. **Timeline Requirements**: Understand processing timeframes for regulatory compliance

### For Compliance Officers
1. **Regulatory Events**: Monitor regulatory authority events for compliance status
2. **Audit Requirements**: Ensure business events meet audit trail requirements
3. **Data Retention**: Verify business events are retained per regulatory requirements
4. **Deadline Management**: Track compliance deadline events and escalation procedures

### For Trade Operations
1. **Trade Events**: Understand trade confirmation, amendment, and cancellation events
2. **Processing Times**: Monitor trade event processing against business SLAs
3. **Error Resolution**: Follow business procedures for trade event errors
4. **Regulatory Impact**: Understand how trade events trigger regulatory reporting

### For Risk Management
1. **Position Events**: Monitor position threshold breach events
2. **Reference Data**: Track reference data update events affecting risk calculations
3. **Regulatory Changes**: Monitor regulatory events affecting risk reporting
4. **Business Continuity**: Ensure critical business events continue during system issues

### For Data Management
1. **Data Quality**: Monitor reference data update events for quality issues
2. **Data Lineage**: Track business events affecting data dependencies
3. **Data Retention**: Manage business event data per regulatory retention requirements
4. **Data Access**: Provide authorized access to business event data for analysis
