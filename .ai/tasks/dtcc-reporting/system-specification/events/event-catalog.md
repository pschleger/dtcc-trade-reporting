# DTCC Regulatory Reporting System - Event Catalog

## Overview

This catalog provides a comprehensive index of all events in the DTCC Regulatory Reporting System, organized by category and processing context. Each event is documented with complete specifications including payload schemas, correlation patterns, and processing requirements.

## Event Categories

### 1. Business Events
Events representing meaningful business occurrences in the OTC derivatives trading lifecycle.

#### 1.1 Trade Lifecycle Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| TradeConfirmationReceived | `trade.confirmation.received` | FpML trade confirmation message ingested | Real-time | Critical |
| TradeValidated | `trade.validated` | Trade passes all validation rules | Real-time | Critical |
| TradeConfirmed | `trade.confirmed` | Trade officially confirmed by counterparties | Real-time | Critical |
| TradeAmended | `trade.amended` | Existing trade modified | Real-time | High |
| TradeCancelled | `trade.cancelled` | Trade cancelled before maturity | Real-time | High |
| TradeMatured | `trade.matured` | Trade reached natural expiration | Scheduled | Medium |

#### 1.2 Position Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| PositionCalculationTriggered | `position.calculation.triggered` | Position recalculation initiated | Real-time | High |
| PositionUpdated | `position.updated` | Position values recalculated | Real-time | High |
| PositionThresholdBreached | `position.threshold.breached` | Regulatory reporting threshold exceeded | Real-time | Critical |
| PositionReconciled | `position.reconciled` | Position reconciliation completed | Batch | Medium |

#### 1.3 Regulatory Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| ReportingObligationIdentified | `regulatory.obligation.identified` | New regulatory requirement detected | Real-time | Critical |
| ReportGenerated | `regulatory.report.generated` | Regulatory report created | Real-time | Critical |
| ReportSubmitted | `regulatory.report.submitted` | Report sent to regulatory authority | Real-time | Critical |
| ReportAcknowledged | `regulatory.report.acknowledged` | Submission confirmed by authority | Real-time | Critical |
| ReportRejected | `regulatory.report.rejected` | Submission rejected by authority | Real-time | Critical |
| ComplianceDeadlineApproaching | `regulatory.deadline.approaching` | Regulatory deadline warning | Scheduled | High |

#### 1.4 Amendment and Cancellation Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| AmendmentRequested | `amendment.requested` | Amendment request received | Real-time | High |
| AmendmentValidated | `amendment.validated` | Amendment passes validation | Real-time | High |
| AmendmentApplied | `amendment.applied` | Amendment successfully applied | Real-time | High |
| AmendmentRejected | `amendment.rejected` | Amendment rejected due to business rules | Real-time | High |
| CancellationRequested | `cancellation.requested` | Cancellation request received | Real-time | High |
| CancellationValidated | `cancellation.validated` | Cancellation passes validation | Real-time | High |
| CancellationApplied | `cancellation.applied` | Cancellation successfully applied | Real-time | High |
| CancellationRejected | `cancellation.rejected` | Cancellation rejected due to business rules | Real-time | High |

### 2. System Events
Events coordinating system operations and maintaining operational health.

#### 2.1 Processing Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| BatchProcessingStarted | `batch.processing.started` | Batch operation initiated | Batch | Medium |
| BatchProcessingCompleted | `batch.processing.completed` | Batch operation finished | Batch | Medium |
| ValidationCompleted | `validation.completed` | Data validation finished | Real-time | Medium |
| ReconciliationCompleted | `reconciliation.completed` | Data reconciliation finished | Batch | Medium |
| EntityProcessorCalculationRequest | `entity.processor.calculation.request` | Processor calculation requested | Real-time | High |
| EntityProcessorCalculationResponse | `entity.processor.calculation.response` | Processor calculation completed | Real-time | High |
| EntityCriteriaCalculationRequest | `entity.criteria.calculation.request` | Criteria evaluation requested | Real-time | High |
| EntityCriteriaCalculationResponse | `entity.criteria.calculation.response` | Criteria evaluation completed | Real-time | High |

#### 2.2 Integration Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| ExternalSystemConnected | `external.system.connected` | External system integration established | On-demand | Medium |
| ExternalSystemDisconnected | `external.system.disconnected` | External system integration lost | On-demand | High |
| DataSynchronizationCompleted | `data.synchronization.completed` | Reference data updated | Scheduled | Medium |
| APIRequestReceived | `api.request.received` | External API call processed | Real-time | Low |
| CalculationMemberJoinEvent | `calculation.member.join` | Calculation member joined cluster | On-demand | Medium |
| CalculationMemberGreetEvent | `calculation.member.greet` | Calculation member greeting | On-demand | Low |
| CalculationMemberKeepAliveEvent | `calculation.member.keepalive` | Calculation member heartbeat | Scheduled | Low |

#### 2.3 Entity Management Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| EntityCreateRequest | `entity.create.request` | Entity creation requested | Real-time | High |
| EntityCreateCollectionRequest | `entity.create.collection.request` | Bulk entity creation requested | Batch | High |
| EntityUpdateRequest | `entity.update.request` | Entity update requested | Real-time | High |
| EntityUpdateCollectionRequest | `entity.update.collection.request` | Bulk entity update requested | Batch | High |
| EntityDeleteRequest | `entity.delete.request` | Entity deletion requested | Real-time | High |
| EntityDeleteResponse | `entity.delete.response` | Entity deletion completed | Real-time | High |
| EntityTransitionRequest | `entity.transition.request` | Entity state transition requested | Real-time | High |
| EntityTransitionResponse | `entity.transition.response` | Entity state transition completed | Real-time | High |
| EntityTransactionResponse | `entity.transaction.response` | Entity transaction completed | Real-time | High |

### 3. Error and Audit Events
Events for error handling, monitoring, and regulatory compliance.

#### 3.1 Error Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| ProcessingError | `processing.error` | General processing failure | Real-time | High |
| ValidationError | `validation.error` | Data validation failure | Real-time | High |
| BusinessRuleViolation | `business.rule.violation` | Business rule constraint violated | Real-time | High |
| SystemError | `system.error` | Infrastructure or system failure | Real-time | Critical |
| TimeoutError | `timeout.error` | Processing timeout exceeded | Real-time | High |
| RetryExhausted | `retry.exhausted` | Maximum retry attempts reached | Real-time | High |
| DeadLetterQueued | `dead.letter.queued` | Event moved to dead letter queue | Real-time | High |

#### 3.2 Audit Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| EntityStateChanged | `audit.entity.state.changed` | Entity state transition recorded | Real-time | Critical |
| UserActionPerformed | `audit.user.action.performed` | User action logged | Real-time | Medium |
| SystemActionPerformed | `audit.system.action.performed` | System action logged | Real-time | Medium |
| SecurityEvent | `audit.security.event` | Security-related event logged | Real-time | Critical |
| ComplianceEvent | `audit.compliance.event` | Compliance-related event logged | Real-time | Critical |
| DataAccessEvent | `audit.data.access.event` | Data access logged | Real-time | Medium |

### 4. External Interface Events
Events triggered by or sent to external systems.

#### 4.1 DTCC GTR Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| GTRReportSubmissionRequested | `gtr.report.submission.requested` | Report submission to GTR initiated | Real-time | Critical |
| GTRReportSubmissionCompleted | `gtr.report.submission.completed` | Report successfully submitted to GTR | Real-time | Critical |
| GTRReportSubmissionFailed | `gtr.report.submission.failed` | Report submission to GTR failed | Real-time | Critical |
| GTRAcknowledgmentReceived | `gtr.acknowledgment.received` | GTR acknowledgment received | Real-time | Critical |
| GTRRejectionReceived | `gtr.rejection.received` | GTR rejection received | Real-time | Critical |

#### 4.2 GLEIF Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| LEIValidationRequested | `gleif.lei.validation.requested` | LEI validation requested from GLEIF | Real-time | High |
| LEIValidationCompleted | `gleif.lei.validation.completed` | LEI validation completed | Real-time | High |
| LEIValidationFailed | `gleif.lei.validation.failed` | LEI validation failed | Real-time | High |
| LEIDataSynchronized | `gleif.lei.data.synchronized` | LEI reference data updated | Scheduled | Medium |

#### 4.3 Trading System Events
| Event Name | Event ID | Description | Frequency | Criticality |
|------------|----------|-------------|-----------|-------------|
| FpMLMessageReceived | `trading.fpml.message.received` | FpML message received from trading system | Real-time | Critical |
| TradeConfirmationSent | `trading.confirmation.sent` | Trade confirmation sent to trading system | Real-time | High |
| TradeStatusUpdateSent | `trading.status.update.sent` | Trade status update sent | Real-time | Medium |

## Event Correlation Patterns

### Correlation ID Usage
- **Business Process Correlation**: All events within a single business process share a correlation ID
- **Request-Response Correlation**: Request and response events use the same correlation ID
- **Batch Processing Correlation**: All events within a batch operation share a correlation ID

### Causation Chains
- **Direct Causation**: Event A directly causes Event B (causationId = eventId of A)
- **Indirect Causation**: Event chain A → B → C (each event references its immediate cause)
- **Parallel Causation**: Multiple events caused by the same triggering event

### Event Ordering
- **Strict Ordering**: Within entity boundaries, events maintain strict temporal ordering
- **Causal Ordering**: Related events maintain causal ordering across entity boundaries
- **Eventual Consistency**: Cross-system events achieve eventual consistency

## Event Routing and Subscription

### Event Bus Architecture
- **Central Event Bus**: All events routed through central event bus
- **Topic-Based Routing**: Events routed based on event type and entity type
- **Subscription Patterns**: Components subscribe to event patterns of interest

### Subscription Patterns
- **Entity-Specific**: Subscribe to all events for specific entities
- **Event-Type-Specific**: Subscribe to specific event types across all entities
- **Workflow-Specific**: Subscribe to events relevant to specific workflows
- **Error-Specific**: Subscribe to error events for monitoring and alerting

## Security and Access Control

### Event Security Classification
- **Public**: Events that can be accessed by all system components
- **Internal**: Events restricted to internal system components
- **Confidential**: Events containing sensitive business data
- **Restricted**: Events containing highly sensitive or regulated data

### Access Control Patterns
- **Role-Based Access**: Event access controlled by user/system roles
- **Attribute-Based Access**: Event access controlled by event attributes
- **Context-Based Access**: Event access controlled by processing context

## Event Persistence and Retention

### Persistence Requirements
- **Transactional Events**: Persisted within database transactions
- **Audit Events**: Persisted in immutable audit log
- **Monitoring Events**: Persisted in time-series database
- **Temporary Events**: Persisted only for processing duration

### Retention Policies
- **Regulatory Events**: 7 years retention for regulatory compliance
- **Business Events**: 5 years retention for business analysis
- **System Events**: 1 year retention for operational analysis
- **Error Events**: 2 years retention for troubleshooting

## Event Schema Evolution

### Versioning Strategy
- **Semantic Versioning**: Major.Minor.Patch versioning for event schemas
- **Backward Compatibility**: Minor versions maintain backward compatibility
- **Breaking Changes**: Major versions for breaking schema changes

### Schema Migration
- **Gradual Migration**: Support multiple schema versions during transition
- **Event Transformation**: Automatic transformation between schema versions
- **Deprecation Process**: Formal deprecation process for old schema versions

---

## Detailed Event Specifications

### Event Documentation Structure
Each event category has detailed specifications including payload schemas, processing requirements, and integration patterns:

- **[Event Template](./event-template.md)** - Standardized template for all event specifications
- **[Business Events](./business-events.md)** - Trade lifecycle, position, regulatory, and amendment events
- **[System Events](./system-events.md)** - Processing, integration, and entity management events
- **[Error and Audit Events](./error-events.md)** - Error handling, monitoring, and compliance events
- **[External Interface Events](./external-events.md)** - DTCC GTR, GLEIF, trading system, and monitoring events

### Event Architecture Documentation
Comprehensive documentation of event patterns, routing, and security:

- **[Event Correlation Patterns](./event-correlation-patterns.md)** - Correlation IDs, causation chains, and event ordering
- **[Event Routing Patterns](./event-routing-patterns.md)** - Topic-based routing, subscription patterns, and load balancing
- **[Event Security Requirements](./event-security-requirements.md)** - Security classification, access control, and compliance

### Event Flow Documentation
Business process event flows and interaction patterns:

- **[Event-Driven Architecture](./event-driven-architecture.md)** - Core principles and processing patterns
- **[Trade Processing Event Flows](./event-flows-trade-processing.md)** - Trade lifecycle event sequences
- **[Position Reporting Event Flows](./event-flows-position-reporting.md)** - Position calculation and reporting flows

---

## Event Catalog Validation

### Completeness Validation
✅ **External Interface Events**: All events from external interface specifications documented
✅ **Workflow Events**: All events from workflow configuration JSON files documented
✅ **System Events**: All CloudEventType schema events documented
✅ **Error Events**: All error handling and audit events documented

### Coverage Analysis
- **Total Events Cataloged**: 89 distinct event types
- **Business Events**: 32 events across trade lifecycle, position management, and regulatory compliance
- **System Events**: 25 events for processing, integration, and entity management
- **Error/Audit Events**: 18 events for error handling, monitoring, and compliance
- **External Events**: 14 events for external system integration

### Schema Validation
✅ **Event Structure**: All events follow standardized CloudEvent schema
✅ **Payload Schemas**: JSON schemas defined for all event payloads
✅ **Metadata Standards**: Consistent metadata structure across all events
✅ **Versioning**: Semantic versioning applied to all event specifications

---

## Usage Guidelines

### For Developers
1. **Event Design**: Use the [Event Template](./event-template.md) for new event specifications
2. **Event Publishing**: Follow patterns in [Event Routing Patterns](./event-routing-patterns.md)
3. **Event Consumption**: Implement subscription patterns from routing documentation
4. **Error Handling**: Follow error event patterns from [Error Events](./error-events.md)

### For Business Analysts
1. **Process Mapping**: Use [Business Events](./business-events.md) for process analysis
2. **Compliance Tracking**: Reference [Event Security Requirements](./event-security-requirements.md)
3. **Event Correlation**: Use [Event Correlation Patterns](./event-correlation-patterns.md) for process tracing

### For Operations Teams
1. **Monitoring**: Implement monitoring based on [System Events](./system-events.md)
2. **Alerting**: Configure alerts using [Error Events](./error-events.md) specifications
3. **Security**: Apply controls from [Event Security Requirements](./event-security-requirements.md)

### For Compliance Teams
1. **Audit Trails**: Use audit events from [Error and Audit Events](./error-events.md)
2. **Regulatory Reporting**: Reference regulatory events in [Business Events](./business-events.md)
3. **Data Retention**: Follow retention policies in [Event Security Requirements](./event-security-requirements.md)
