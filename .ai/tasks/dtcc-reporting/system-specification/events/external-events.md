# External Interface Events Specifications

## Overview

External interface events represent interactions with external systems including regulatory authorities, trading systems, reference data providers, and monitoring systems. These events enable integration, data exchange, and compliance with external requirements.

## DTCC GTR Events

### GTRReportSubmissionRequested

**Event ID**: `gtr.report.submission.requested`  
**Event Type**: `ExternalEvent`  
**Category**: `DTCC GTR Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a regulatory report submission to DTCC Global Trade Repository is initiated, beginning the external submission process.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "GTRReportSubmissionRequested",
  "entityType": "REGULATORY_REPORT",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "gtr-submission-service",
  "source": "urn:system:gtr-submission-service",
  "payload": {
    "submissionId": "string",
    "reportId": "string",
    "submissionType": "INITIAL|LIFECYCLE|VALUATION|COLLATERAL",
    "reportContent": {
      "reportXML": "string",
      "reportFormat": "DTCC_GTR_XML_2.1",
      "reportSize": "number",
      "reportChecksum": "string"
    },
    "submissionTarget": {
      "gtrEndpoint": "string",
      "submissionMethod": "HTTPS_POST",
      "authenticationMethod": "CERTIFICATE",
      "timeoutSeconds": "number"
    },
    "businessContext": {
      "reportingRegime": "string",
      "jurisdiction": "string",
      "reportingDeadline": "iso8601",
      "urgency": "IMMEDIATE|STANDARD|BATCH"
    }
  },
  "metadata": {
    "traceId": "string",
    "submissionContext": {
      "attemptNumber": "number",
      "maxRetries": "number",
      "submissionWindow": "string"
    }
  }
}
```

#### Triggering Conditions
- Regulatory report ready for submission
- Submission deadline approaching
- Manual submission initiated
- Retry submission after failure

#### Event Processing
- **Primary Handlers**: GTRSubmissionService
- **Processing SLA**: 30 seconds
- **Downstream Events**: GTRReportSubmissionCompleted, GTRReportSubmissionFailed

---

### GTRAcknowledgmentReceived

**Event ID**: `gtr.acknowledgment.received`  
**Event Type**: `ExternalEvent`  
**Category**: `DTCC GTR Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when an acknowledgment is received from DTCC GTR confirming successful report submission and processing.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "GTRAcknowledgmentReceived",
  "entityType": "SUBMISSION_STATUS",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "gtr-response-handler",
  "source": "urn:external:dtcc-gtr",
  "payload": {
    "acknowledgmentId": "string",
    "originalSubmissionId": "string",
    "reportId": "string",
    "acknowledgmentStatus": "ACCEPTED|PROCESSED|COMPLETED",
    "gtrResponse": {
      "gtrReferenceId": "string",
      "processingTimestamp": "iso8601",
      "validationResults": {
        "schemaValid": "boolean",
        "businessRulesValid": "boolean",
        "validationMessages": ["string"]
      }
    },
    "complianceInfo": {
      "reportingObligation": "string",
      "complianceStatus": "FULFILLED",
      "regulatoryTimestamp": "iso8601"
    }
  },
  "metadata": {
    "traceId": "string",
    "gtrMetadata": {
      "responseTime": "number",
      "gtrVersion": "string",
      "processingNode": "string"
    }
  }
}
```

#### Triggering Conditions
- GTR processes submitted report successfully
- GTR validation completes without errors
- GTR confirms regulatory compliance

#### Event Processing
- **Primary Handlers**: SubmissionStatusService, ComplianceTrackingService
- **Processing SLA**: 5 seconds
- **Downstream Events**: ComplianceObligationFulfilled, AuditTrailUpdated

---

## GLEIF Events

### LEIValidationRequested

**Event ID**: `gleif.lei.validation.requested`  
**Event Type**: `ExternalEvent`  
**Category**: `GLEIF Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when Legal Entity Identifier validation is requested from the GLEIF registry to verify counterparty information.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "LEIValidationRequested",
  "entityType": "COUNTERPARTY",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "lei-validation-service",
  "source": "urn:system:lei-validation-service",
  "payload": {
    "validationRequestId": "string",
    "leiCode": "string",
    "validationType": "EXISTENCE|STATUS|DETAILS|HIERARCHY",
    "requestContext": {
      "entityId": "string",
      "entityType": "COUNTERPARTY",
      "validationReason": "ONBOARDING|PERIODIC|TRADE_VALIDATION|COMPLIANCE"
    },
    "gleifRequest": {
      "endpoint": "https://api.gleif.org/api/v1/lei-records",
      "requestMethod": "GET",
      "requestParameters": {
        "lei": "string",
        "include": "entity,registration"
      }
    }
  },
  "metadata": {
    "traceId": "string",
    "validationContext": {
      "cachePolicy": "USE_CACHE|BYPASS_CACHE",
      "timeoutSeconds": "number",
      "priority": "HIGH|MEDIUM|LOW"
    }
  }
}
```

#### Triggering Conditions
- New counterparty onboarding
- Periodic LEI validation
- Trade validation requiring LEI check
- Compliance audit requirement

#### Event Processing
- **Primary Handlers**: LEIValidationService
- **Processing SLA**: 10 seconds
- **Downstream Events**: LEIValidationCompleted, LEIValidationFailed

---

### LEIDataSynchronized

**Event ID**: `gleif.lei.data.synchronized`  
**Event Type**: `ExternalEvent`  
**Category**: `GLEIF Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when LEI reference data is synchronized from GLEIF registry, updating local counterparty information.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "LEIDataSynchronized",
  "entityType": "REFERENCE_DATA",
  "entityId": "lei-sync",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "lei-sync-service",
  "source": "urn:system:lei-sync-service",
  "payload": {
    "synchronizationId": "string",
    "syncType": "FULL|INCREMENTAL|TARGETED",
    "syncScope": {
      "leiCodes": ["string"],
      "syncStartTime": "iso8601",
      "syncEndTime": "iso8601"
    },
    "syncResults": {
      "recordsProcessed": "number",
      "recordsUpdated": "number",
      "recordsAdded": "number",
      "recordsDeactivated": "number",
      "errors": "number"
    },
    "dataQuality": {
      "completenessScore": "number",
      "accuracyScore": "number",
      "freshnessScore": "number"
    }
  },
  "metadata": {
    "traceId": "string",
    "syncMetadata": {
      "gleifApiVersion": "string",
      "syncDuration": "number",
      "dataVersion": "string"
    }
  }
}
```

#### Triggering Conditions
- Scheduled LEI data synchronization
- Manual data refresh initiated
- Data quality threshold breach
- New LEI codes detected

#### Event Processing
- **Primary Handlers**: ReferenceDataService, DataQualityService
- **Processing SLA**: 60 seconds
- **Downstream Events**: ReferenceDataUpdated, DataQualityReport

---

## Trading System Events

### FpMLMessageReceived

**Event ID**: `trading.fpml.message.received`  
**Event Type**: `ExternalEvent`  
**Category**: `Trading System Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when an FpML message is received from external trading systems, initiating trade processing workflows.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "FpMLMessageReceived",
  "entityType": "TRADE_CONFIRMATION",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": null,
  "actorId": "fpml-gateway",
  "source": "urn:external:trading-system",
  "payload": {
    "messageId": "string",
    "fpmlContent": {
      "messageType": "CONFIRMATION|AMENDMENT|CANCELLATION",
      "fpmlVersion": "5.12",
      "messageXML": "string",
      "messageSize": "number",
      "messageChecksum": "string"
    },
    "tradingSystemInfo": {
      "systemId": "string",
      "systemName": "string",
      "messageTimestamp": "iso8601",
      "sequenceNumber": "number"
    },
    "businessContext": {
      "tradingDesk": "string",
      "businessUnit": "string",
      "trader": "string",
      "counterparty": "string"
    }
  },
  "metadata": {
    "traceId": "string",
    "messageMetadata": {
      "receivedTimestamp": "iso8601",
      "processingPriority": "HIGH|MEDIUM|LOW",
      "messageRoute": "string"
    }
  }
}
```

#### Triggering Conditions
- FpML message received via trading system interface
- Message passes initial format validation
- Message routing rules matched

#### Event Processing
- **Primary Handlers**: FpMLProcessingService, TradeConfirmationWorkflow
- **Processing SLA**: 10 seconds
- **Downstream Events**: TradeConfirmationReceived, MessageValidationFailed

---

### TradeConfirmationSent

**Event ID**: `trading.confirmation.sent`  
**Event Type**: `ExternalEvent`  
**Category**: `Trading System Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a trade confirmation is sent back to the trading system, completing the confirmation loop.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "TradeConfirmationSent",
  "entityType": "TRADE",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "trade-confirmation-service",
  "source": "urn:system:trade-confirmation-service",
  "payload": {
    "confirmationId": "string",
    "tradeId": "string",
    "confirmationType": "ACCEPTANCE|REJECTION|AMENDMENT_CONFIRMATION",
    "confirmationContent": {
      "confirmationMessage": "string",
      "confirmationFormat": "FpML|JSON|XML",
      "confirmationTimestamp": "iso8601"
    },
    "targetSystem": {
      "tradingSystemId": "string",
      "endpoint": "string",
      "deliveryMethod": "API|MESSAGE_QUEUE|FILE"
    },
    "confirmationStatus": {
      "deliveryStatus": "SENT|DELIVERED|ACKNOWLEDGED|FAILED",
      "deliveryTimestamp": "iso8601",
      "responseReceived": "boolean"
    }
  },
  "metadata": {
    "traceId": "string",
    "deliveryMetadata": {
      "attemptNumber": "number",
      "deliveryDuration": "number",
      "responseTime": "number"
    }
  }
}
```

#### Triggering Conditions
- Trade validation completed successfully
- Trade confirmation decision made
- Amendment processing completed
- Cancellation processing completed

#### Event Processing
- **Primary Handlers**: TradingSystemIntegrationService
- **Processing SLA**: 15 seconds
- **Downstream Events**: ConfirmationDelivered, DeliveryFailed

---

## Monitoring System Events

### MonitoringAlertTriggered

**Event ID**: `monitoring.alert.triggered`  
**Event Type**: `ExternalEvent`  
**Category**: `Monitoring Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when external monitoring systems detect conditions requiring attention, enabling proactive system management.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "MonitoringAlertTriggered",
  "entityType": "MONITORING_ALERT",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "monitoring-system",
  "source": "urn:external:monitoring-system",
  "payload": {
    "alertId": "string",
    "alertType": "PERFORMANCE|AVAILABILITY|ERROR_RATE|THRESHOLD|SECURITY",
    "alertSeverity": "CRITICAL|HIGH|MEDIUM|LOW|INFO",
    "alertCondition": {
      "metricName": "string",
      "threshold": "number",
      "actualValue": "number",
      "comparisonOperator": "GT|LT|EQ|NE",
      "evaluationPeriod": "string"
    },
    "affectedComponents": {
      "systemComponent": "string",
      "serviceEndpoint": "string",
      "businessProcess": "string"
    },
    "alertDetails": {
      "alertMessage": "string",
      "alertDescription": "string",
      "recommendedActions": ["string"],
      "escalationRequired": "boolean"
    }
  },
  "metadata": {
    "traceId": "string",
    "monitoringMetadata": {
      "monitoringSystem": "string",
      "alertRule": "string",
      "evaluationTimestamp": "iso8601"
    }
  }
}
```

#### Triggering Conditions
- Performance threshold exceeded
- System availability degraded
- Error rate threshold breached
- Security event detected

#### Event Processing
- **Primary Handlers**: AlertHandlingService, IncidentManagementService
- **Processing SLA**: 30 seconds
- **Downstream Events**: IncidentCreated, EscalationTriggered

---

## External Event Integration Patterns

### Request-Response Pattern
External systems often require request-response interactions:
- LEIValidationRequested → LEIValidationCompleted
- GTRReportSubmissionRequested → GTRAcknowledgmentReceived
- API calls with synchronous responses

### Event-Driven Integration Pattern
Asynchronous event-driven integration with external systems:
- FpMLMessageReceived → TradeConfirmationSent
- MonitoringAlertTriggered → IncidentResponse
- Webhook-based event delivery

### Batch Integration Pattern
Scheduled batch processing with external systems:
- LEIDataSynchronized (daily sync)
- ReferenceDataUpdated (periodic updates)
- ComplianceReportSubmitted (regulatory deadlines)

### Circuit Breaker Pattern
Protection against external system failures:
- Automatic fallback to cached data
- Graceful degradation of functionality
- Health check monitoring and recovery

## External System SLA Requirements

### DTCC GTR Integration
- **Submission SLA**: 99.5% availability during business hours
- **Response Time**: < 30 seconds for acknowledgments
- **Retry Policy**: 3 attempts with exponential backoff
- **Error Handling**: Dead letter queue for failed submissions

### GLEIF Integration
- **Validation SLA**: 99% availability
- **Response Time**: < 10 seconds for LEI lookups
- **Cache Policy**: 24-hour cache for validated LEIs
- **Fallback**: Local LEI database for critical operations

### Trading System Integration
- **Message Processing**: < 5 seconds for FpML messages
- **Confirmation Delivery**: < 15 seconds
- **Availability**: 99.9% during trading hours
- **Message Ordering**: Strict ordering within trading session
