# Business Events Specifications

## Overview

Business events represent meaningful business occurrences in the OTC derivatives trading lifecycle. These events drive the core business processes and maintain the regulatory compliance requirements of the DTCC Regulatory Reporting System.

## Trade Lifecycle Events

### TradeConfirmationReceived

**Event ID**: `trade.confirmation.received`  
**Event Type**: `BusinessEvent`  
**Category**: `Trade Lifecycle`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when an FpML trade confirmation message is received from external trading systems. This is the entry point for all trade processing workflows.

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
- **Downstream Events**: TradeValidated, ValidationError

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
- **Downstream Events**: ReportSubmitted, ReportSubmissionFailed

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
- **Downstream Events**: AmendmentValidated, AmendmentRejected

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
