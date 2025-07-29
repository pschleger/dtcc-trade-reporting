# External Business Events Specifications

## Overview

External business events are the only true "events" in the DTCC Regulatory Reporting System. These represent external stimuli that trigger business processes and originate from systems outside our control.

**Key Principle**: Events are external triggers only. Internal processing, state transitions, and system coordination are handled through workflow mechanisms, not events.

## 1. External Trading System Events

### FpMLMessageReceived

**Event ID**: `trading.fpml.message.received`
**Event Type**: `ExternalEvent`
**Source**: External Trading Systems
**Criticality**: Critical

#### Overview
Triggered when an FpML trade confirmation, amendment, or cancellation message is received from external trading systems.

#### Triggering Source
- External trading platforms
- Counterparty trading systems
- Trade capture systems
- Electronic trading networks

#### Business Impact
- Initiates TradeConfirmationWorkflow
- Creates TRADE_CONFIRMATION entity
- Triggers validation and processing workflows

---

### AmendmentRequested

**Event ID**: `trading.amendment.requested`
**Event Type**: `ExternalEvent`
**Source**: External Trading Systems
**Criticality**: High

#### Overview
Triggered when an external system requests amendment of an existing trade.

#### Triggering Source
- Trading system amendment requests
- Counterparty amendment notifications
- Operations team amendment submissions

#### Business Impact
- Initiates AmendmentWorkflow
- Creates AMENDMENT entity
- Triggers impact assessment and validation

---

### CancellationRequested

**Event ID**: `trading.cancellation.requested`
**Event Type**: `ExternalEvent`
**Source**: External Trading Systems
**Criticality**: High

#### Overview
Triggered when an external system requests cancellation of an existing trade.

#### Triggering Source
- Trading system cancellation requests
- Counterparty cancellation notifications
- Operations team cancellation submissions

#### Business Impact
- Initiates CancellationWorkflow
- Creates CANCELLATION entity
- Triggers impact assessment and validation

---

## 2. External Regulatory Authority Events

### GTRAcknowledgmentReceived

**Event ID**: `gtr.acknowledgment.received`
**Event Type**: `ExternalEvent`
**Source**: DTCC Global Trade Repository
**Criticality**: Critical

#### Overview
Triggered when DTCC GTR acknowledges receipt and successful processing of a regulatory report.

#### Triggering Source
- DTCC GTR response systems
- Regulatory authority acknowledgment services

#### Business Impact
- Updates SUBMISSION_STATUS entity
- Completes regulatory reporting obligation
- Triggers compliance tracking updates

---

### GTRRejectionReceived

**Event ID**: `gtr.rejection.received`
**Event Type**: `ExternalEvent`
**Source**: DTCC Global Trade Repository
**Criticality**: Critical

#### Overview
Triggered when DTCC GTR rejects a regulatory report submission due to validation errors or compliance issues.

#### Triggering Source
- DTCC GTR validation systems
- Regulatory authority rejection notifications

#### Business Impact
- Updates SUBMISSION_STATUS entity to failed state
- Triggers error resolution workflow
- Initiates resubmission process

---

### ComplianceDeadlineApproaching

**Event ID**: `regulatory.deadline.approaching`
**Event Type**: `ExternalEvent`
**Source**: Compliance Monitoring Systems
**Criticality**: High

#### Overview
Triggered by external compliance monitoring systems when regulatory reporting deadlines are approaching.

#### Triggering Source
- External compliance monitoring platforms
- Regulatory calendar systems
- Deadline notification services

#### Business Impact
- Triggers urgent processing workflows
- Escalates pending regulatory reports
- Initiates deadline compliance procedures

---

## 3. External Reference Data Events

### LEIDataSynchronized

**Event ID**: `gleif.lei.data.synchronized`
**Event Type**: `ExternalEvent`
**Source**: GLEIF Registry
**Criticality**: Medium

#### Overview
Triggered when LEI reference data is updated from the GLEIF registry.

#### Triggering Source
- GLEIF data synchronization services
- LEI registry update notifications
- Scheduled data refresh processes

#### Business Impact
- Updates REFERENCE_DATA entities
- Triggers counterparty data validation
- Updates LEI validation rules

---

### ReferenceDataUpdated

**Event ID**: `reference.data.updated`
**Event Type**: `ExternalEvent`
**Source**: External Data Vendors
**Criticality**: Medium

#### Overview
Triggered when market reference data is updated from external data providers.

#### Triggering Source
- Market data vendors
- Reference data providers
- Regulatory data services

#### Business Impact
- Updates REFERENCE_DATA entities
- Triggers data quality validation
- Updates pricing and valuation data

---

## 4. External Monitoring Events

### MonitoringAlertTriggered

**Event ID**: `monitoring.alert.triggered`
**Event Type**: `ExternalEvent`
**Source**: External Monitoring Systems
**Criticality**: High

#### Overview
Triggered when external monitoring systems detect conditions requiring attention.

#### Triggering Source
- External monitoring platforms
- Infrastructure monitoring systems
- Business process monitoring tools

#### Business Impact
- Triggers incident response procedures
- Initiates system health checks
- Escalates operational issues

---

### ScheduledProcessTriggered

**Event ID**: `scheduled.process.triggered`
**Event Type**: `ExternalEvent`
**Source**: External Schedulers
**Criticality**: Medium

#### Overview
Triggered by external scheduling systems to initiate batch processing operations.

#### Triggering Source
- Enterprise job schedulers
- Batch processing systems
- Time-based trigger systems

#### Business Impact
- Initiates batch processing workflows
- Triggers scheduled reconciliation
- Starts end-of-day processing

---

## Event Processing Philosophy

### External Trigger Pattern
All events follow the external trigger pattern:
1. **External System** generates event
2. **Event Gateway** receives and validates event
3. **Workflow Engine** initiates appropriate business workflow
4. **Internal Processing** handled through workflow state transitions

### No Internal Events
The system does not generate events for:
- Internal state transitions (handled by workflow)
- System coordination (handled by direct calls)
- Error conditions (handled by workflow error states)
- Audit activities (handled by transaction records)

### Event-to-Workflow Mapping
Each external event maps to specific workflows:
- **FpMLMessageReceived** → TradeConfirmationWorkflow
- **AmendmentRequested** → AmendmentWorkflow
- **CancellationRequested** → CancellationWorkflow
- **GTRAcknowledgmentReceived** → SubmissionStatusWorkflow
- **LEIDataSynchronized** → ReferenceDataWorkflow
- **ScheduledProcessTriggered** → BatchProcessingWorkflow

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "TradeConfirmationReceived",
  "entityType": "TRADE_CONFIRMATION",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": null,
  "actorId": "trading-system",
  "source": "urn:trading-system:fpml-gateway",
  "payload": {
    "fpmlMessage": "string",
    "messageType": "string",
    "tradingSystemId": "string",
    "counterpartyId": "string",
    "productType": "string",
    "notionalAmount": "number",
    "currency": "string",
    "tradeDate": "date",
    "effectiveDate": "date",
    "maturityDate": "date"
  },
  "metadata": {
    "traceId": "string",
    "businessContext": {
      "tradingDesk": "string",
      "businessUnit": "string"
    },
    "securityContext": {
      "classification": "confidential"
    }
  }
}
```

#### Triggering Conditions
- FpML message received via trading system interface
- Message passes initial format validation
- Message contains required trade identification fields

#### Event Processing
- **Primary Handlers**: TradeConfirmationWorkflow
- **Processing SLA**: 5 seconds
- **Retry Policy**: 3 retries with exponential backoff
- **Downstream Events**: TradeValidated (success), entity transitions to `validation-failed` state (failure)

---

### TradeValidated

**Event ID**: `trade.validated`  
**Event Type**: `BusinessEvent`  
**Category**: `Trade Lifecycle`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a trade confirmation passes all validation rules including business rules, data quality checks, and regulatory compliance requirements.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "TradeValidated",
  "entityType": "TRADE",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "validation-engine",
  "source": "urn:system:validation-engine",
  "payload": {
    "tradeId": "string",
    "validationResults": {
      "businessRulesValid": true,
      "dataQualityValid": true,
      "regulatoryComplianceValid": true,
      "counterpartyValid": true,
      "productValid": true
    },
    "validationTimestamp": "iso8601",
    "validationVersion": "string"
  },
  "metadata": {
    "traceId": "string",
    "businessContext": {
      "validationScope": "full",
      "validationLevel": "strict"
    }
  }
}
```

#### Triggering Conditions
- Trade confirmation entity created successfully
- All validation criteria pass
- No business rule violations detected

#### Event Processing
- **Primary Handlers**: TradeWorkflow
- **Processing SLA**: 2 seconds
- **Downstream Events**: TradeConfirmed, PositionCalculationTriggered

---

## Position Events

### PositionCalculationTriggered

**Event ID**: `position.calculation.triggered`  
**Event Type**: `BusinessEvent`  
**Category**: `Position Management`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when position recalculation is required due to trade events, market data updates, or scheduled recalculation processes.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "PositionCalculationTriggered",
  "entityType": "POSITION",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "position-engine",
  "source": "urn:system:position-engine",
  "payload": {
    "positionId": "string",
    "calculationType": "incremental|full",
    "triggerReason": "trade_update|market_data|scheduled|threshold_breach",
    "affectedTrades": ["string"],
    "calculationScope": {
      "counterparties": ["string"],
      "products": ["string"],
      "currencies": ["string"]
    },
    "calculationParameters": {
      "valuationDate": "date",
      "marketDataVersion": "string",
      "calculationMethod": "string"
    }
  },
  "metadata": {
    "traceId": "string",
    "businessContext": {
      "urgency": "high|medium|low",
      "businessImpact": "regulatory|risk|reporting"
    }
  }
}
```

#### Triggering Conditions
- New trade confirmed
- Existing trade amended or cancelled
- Market data update affecting position
- Scheduled position recalculation
- Regulatory threshold monitoring

#### Event Processing
- **Primary Handlers**: PositionWorkflow
- **Processing SLA**: 30 seconds
- **Downstream Events**: PositionUpdated, PositionThresholdBreached

---

## Regulatory Events

### ReportingObligationIdentified

**Event ID**: `regulatory.obligation.identified`  
**Event Type**: `BusinessEvent`  
**Category**: `Regulatory Compliance`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when the system identifies a new regulatory reporting obligation based on trade characteristics, position thresholds, or regulatory rule changes.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "ReportingObligationIdentified",
  "entityType": "REPORTING_OBLIGATION",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "compliance-engine",
  "source": "urn:system:compliance-engine",
  "payload": {
    "obligationId": "string",
    "regulatoryFramework": "EMIR|CFTC|MiFID|ASIC",
    "reportingRegime": "string",
    "obligationType": "initial|lifecycle|valuation|collateral",
    "reportingDeadline": "iso8601",
    "reportingFrequency": "real-time|daily|weekly|monthly",
    "affectedEntities": {
      "trades": ["string"],
      "positions": ["string"],
      "counterparties": ["string"]
    },
    "reportingRequirements": {
      "reportFormat": "DTCC_GTR|FCA_XML|CFTC_XML",
      "dataElements": ["string"],
      "validationRules": ["string"]
    }
  },
  "metadata": {
    "traceId": "string",
    "businessContext": {
      "jurisdiction": "string",
      "regulatoryAuthority": "string",
      "complianceLevel": "mandatory|voluntary"
    }
  }
}
```

#### Triggering Conditions
- Trade characteristics meet reporting thresholds
- Position values exceed regulatory limits
- New regulatory rules become effective
- Counterparty status changes affecting reporting

#### Event Processing
- **Primary Handlers**: RegulatoryReportWorkflow
- **Processing SLA**: 10 seconds
- **Downstream Events**: ReportGenerated, ComplianceDeadlineApproaching

---

### ReportGenerated

**Event ID**: `regulatory.report.generated`  
**Event Type**: `BusinessEvent`  
**Category**: `Regulatory Compliance`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a regulatory report has been successfully generated and is ready for submission to the appropriate regulatory authority.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "ReportGenerated",
  "entityType": "REGULATORY_REPORT",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "report-generator",
  "source": "urn:system:report-generator",
  "payload": {
    "reportId": "string",
    "reportType": "initial|lifecycle|valuation|collateral",
    "reportFormat": "DTCC_GTR|FCA_XML|CFTC_XML",
    "reportingRegime": "string",
    "reportContent": {
      "reportXML": "string",
      "reportHash": "string",
      "reportSize": "number"
    },
    "reportMetadata": {
      "generationTimestamp": "iso8601",
      "reportingPeriod": "string",
      "dataAsOfDate": "date",
      "reportVersion": "string"
    },
    "submissionTarget": {
      "regulatoryAuthority": "string",
      "submissionEndpoint": "string",
      "submissionMethod": "API|SFTP|Portal"
    }
  },
  "metadata": {
    "traceId": "string",
    "businessContext": {
      "urgency": "immediate|standard|batch",
      "complianceDeadline": "iso8601"
    }
  }
}
```

#### Triggering Conditions
- Reporting obligation requires report generation
- All required data elements are available
- Report generation rules are satisfied
- Submission deadline is approaching

#### Event Processing
- **Primary Handlers**: RegulatoryReportWorkflow
- **Processing SLA**: 5 seconds
- **Downstream Events**: ReportSubmitted (success), entity transitions to `submission-failed` state (failure)

---

## Amendment and Cancellation Events

### AmendmentRequested

**Event ID**: `amendment.requested`  
**Event Type**: `BusinessEvent`  
**Category**: `Trade Lifecycle`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a trade amendment request is received, requiring validation and processing to update the existing trade entity.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "AmendmentRequested",
  "entityType": "AMENDMENT",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "trading-system",
  "source": "urn:trading-system:amendment-gateway",
  "payload": {
    "amendmentId": "string",
    "originalTradeId": "string",
    "amendmentType": "economic|administrative|lifecycle",
    "amendmentReason": "string",
    "requestedChanges": {
      "notionalAmount": "number",
      "maturityDate": "date",
      "fixedRate": "number",
      "currency": "string",
      "otherChanges": {}
    },
    "amendmentSource": {
      "requestingParty": "string",
      "requestTimestamp": "iso8601",
      "requestReference": "string"
    }
  },
  "metadata": {
    "traceId": "string",
    "businessContext": {
      "amendmentUrgency": "immediate|standard",
      "businessJustification": "string"
    }
  }
}
```

#### Triggering Conditions
- Amendment request received from trading system
- Amendment request passes initial format validation
- Original trade exists and is amendable

#### Event Processing
- **Primary Handlers**: AmendmentWorkflow
- **Processing SLA**: 10 seconds
- **Downstream Events**: AmendmentValidated (success), entity transitions to `validation-failed` state (failure)

---

## Event Correlation Patterns

### Business Process Correlation
All events within a single business process (e.g., trade confirmation to regulatory reporting) share the same correlation ID, enabling end-to-end process tracking.

### Causation Chains
- **TradeConfirmationReceived** → **TradeValidated** → **TradeConfirmed**
- **TradeConfirmed** → **PositionCalculationTriggered** → **PositionUpdated**
- **PositionUpdated** → **ReportingObligationIdentified** → **ReportGenerated**
- **AmendmentRequested** → **AmendmentValidated** → **AmendmentApplied**

### Cross-Entity Dependencies
- Trade events trigger position recalculation
- Position events trigger regulatory reporting obligations
- Amendment events trigger impact assessment across related entities
- Regulatory events trigger compliance monitoring and alerting
