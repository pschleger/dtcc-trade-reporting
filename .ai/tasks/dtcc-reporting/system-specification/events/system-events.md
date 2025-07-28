# System Events Specifications

## Overview

System events coordinate system operations, maintain operational health, and manage the technical infrastructure supporting the DTCC Regulatory Reporting System. These events ensure reliable processing, monitoring, and integration across all system components.

## Processing Events

### EntityProcessorCalculationRequest

**Event ID**: `entity.processor.calculation.request`  
**Event Type**: `SystemEvent`  
**Category**: `Processing`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when the system requires processor-based calculation or transformation of entity data during workflow state transitions.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "EntityProcessorCalculationRequest",
  "entityType": "string",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "workflow-engine",
  "source": "urn:system:workflow-engine",
  "payload": {
    "id": "string",
    "meta": {
      "modelKey": {
        "name": "string",
        "version": "string"
      },
      "entityId": "string",
      "entityType": "string",
      "workflowInfo": {
        "workflowName": "string",
        "currentState": "string",
        "targetState": "string"
      },
      "transitionInfo": {
        "transitionName": "string",
        "processorName": "string",
        "executionMode": "SYNC|ASYNC"
      }
    },
    "data": {
      "format": "JSON|XML|BINARY",
      "content": "string|object"
    }
  },
  "metadata": {
    "traceId": "string",
    "processingHints": {
      "timeout": "number",
      "priority": "high|medium|low",
      "retryPolicy": "string"
    }
  }
}
```

#### Triggering Conditions
- Workflow state transition requires processor execution
- Entity data transformation needed
- Business logic processing required
- Validation or calculation operations triggered

#### Event Processing
- **Primary Handlers**: CyodaProcessor implementations
- **Processing SLA**: 30 seconds (configurable per processor)
- **Retry Policy**: 3 retries with exponential backoff
- **Downstream Events**: EntityProcessorCalculationResponse

---

### EntityCriteriaCalculationRequest

**Event ID**: `entity.criteria.calculation.request`  
**Event Type**: `SystemEvent`  
**Category**: `Processing`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when the system needs to evaluate criteria or business rules to determine if a workflow state transition should proceed.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "EntityCriteriaCalculationRequest",
  "entityType": "string",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "workflow-engine",
  "source": "urn:system:workflow-engine",
  "payload": {
    "id": "string",
    "meta": {
      "modelKey": {
        "name": "string",
        "version": "string"
      },
      "entityId": "string",
      "entityType": "string",
      "workflowInfo": {
        "workflowName": "string",
        "currentState": "string",
        "targetState": "string"
      },
      "transitionInfo": {
        "transitionName": "string",
        "criterionName": "string",
        "evaluationContext": "string"
      }
    },
    "data": {
      "format": "JSON|XML|BINARY",
      "content": "string|object"
    }
  },
  "metadata": {
    "traceId": "string",
    "evaluationHints": {
      "timeout": "number",
      "cachePolicy": "string",
      "evaluationLevel": "strict|lenient"
    }
  }
}
```

#### Triggering Conditions
- Workflow transition requires criteria evaluation
- Business rule validation needed
- Conditional logic evaluation required
- Guard condition checking triggered

#### Event Processing
- **Primary Handlers**: CyodaCriterion implementations
- **Processing SLA**: 10 seconds (configurable per criterion)
- **Retry Policy**: 2 retries with linear backoff
- **Downstream Events**: EntityCriteriaCalculationResponse

---

### BatchProcessingStarted

**Event ID**: `batch.processing.started`  
**Event Type**: `SystemEvent`  
**Category**: `Processing`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a batch processing operation is initiated, providing coordination and monitoring for large-scale data processing activities.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "BatchProcessingStarted",
  "entityType": "PROCESSING_BATCH",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "batch-scheduler",
  "source": "urn:system:batch-scheduler",
  "payload": {
    "batchId": "string",
    "batchType": "scheduled|triggered|manual",
    "processingScope": {
      "entityTypes": ["string"],
      "dateRange": {
        "startDate": "date",
        "endDate": "date"
      },
      "filterCriteria": {}
    },
    "processingParameters": {
      "parallelism": "number",
      "chunkSize": "number",
      "timeoutMinutes": "number",
      "retryPolicy": "string"
    },
    "estimatedMetrics": {
      "estimatedRecords": "number",
      "estimatedDurationMinutes": "number",
      "estimatedResources": "string"
    }
  },
  "metadata": {
    "traceId": "string",
    "operationalContext": {
      "scheduledTime": "iso8601",
      "priority": "high|medium|low",
      "businessJustification": "string"
    }
  }
}
```

#### Triggering Conditions
- Scheduled batch job execution time reached
- Manual batch processing initiated
- Event-driven batch processing triggered
- Recovery batch processing required

#### Event Processing
- **Primary Handlers**: BatchProcessingWorkflow
- **Processing SLA**: Variable (based on batch size)
- **Downstream Events**: BatchProcessingCompleted, BatchProcessingFailed

---

## Integration Events

### CalculationMemberJoinEvent

**Event ID**: `calculation.member.join`  
**Event Type**: `SystemEvent`  
**Category**: `Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a calculation member joins the distributed calculation cluster, enabling horizontal scaling of processing capabilities.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "CalculationMemberJoinEvent",
  "entityType": "CALCULATION_MEMBER",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": null,
  "actorId": "calculation-member",
  "source": "urn:system:calculation-member",
  "payload": {
    "memberId": "string",
    "memberInfo": {
      "hostname": "string",
      "ipAddress": "string",
      "port": "number",
      "capabilities": ["string"],
      "tags": ["string"]
    },
    "clusterInfo": {
      "clusterId": "string",
      "clusterVersion": "string",
      "memberCount": "number"
    },
    "resourceInfo": {
      "cpuCores": "number",
      "memoryMB": "number",
      "diskSpaceGB": "number"
    }
  },
  "metadata": {
    "traceId": "string",
    "systemContext": {
      "deploymentEnvironment": "production|staging|development",
      "serviceVersion": "string"
    }
  }
}
```

#### Triggering Conditions
- New calculation member instance started
- Calculation member reconnects after network partition
- Cluster scaling operation initiated
- Service deployment or restart

#### Event Processing
- **Primary Handlers**: ClusterManagementService
- **Processing SLA**: 5 seconds
- **Downstream Events**: CalculationMemberGreetEvent

---

### ExternalSystemConnected

**Event ID**: `external.system.connected`  
**Event Type**: `SystemEvent`  
**Category**: `Integration`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when a connection to an external system is successfully established, enabling integration and data exchange capabilities.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "ExternalSystemConnected",
  "entityType": "EXTERNAL_SYSTEM",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "integration-service",
  "source": "urn:system:integration-service",
  "payload": {
    "systemId": "string",
    "systemType": "DTCC_GTR|GLEIF|TRADING_SYSTEM|REFERENCE_DATA",
    "connectionInfo": {
      "endpoint": "string",
      "protocol": "HTTPS|SFTP|gRPC|WebSocket",
      "connectionMethod": "API|FILE_TRANSFER|STREAMING",
      "authenticationMethod": "OAUTH|CERTIFICATE|API_KEY"
    },
    "connectionMetrics": {
      "connectionTime": "iso8601",
      "latencyMs": "number",
      "throughputMbps": "number"
    },
    "capabilities": {
      "supportedOperations": ["string"],
      "dataFormats": ["string"],
      "maxRequestSize": "number"
    }
  },
  "metadata": {
    "traceId": "string",
    "integrationContext": {
      "connectionAttempt": "number",
      "lastFailureReason": "string",
      "healthCheckStatus": "healthy|degraded|unhealthy"
    }
  }
}
```

#### Triggering Conditions
- External system connection established successfully
- Connection restored after failure
- Health check confirms system availability
- Integration service startup

#### Event Processing
- **Primary Handlers**: IntegrationMonitoringService
- **Processing SLA**: 2 seconds
- **Downstream Events**: DataSynchronizationCompleted

---

## Entity Management Events

### EntityTransitionRequest

**Event ID**: `entity.transition.request`  
**Event Type**: `SystemEvent`  
**Category**: `Entity Management`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered when an entity state transition is requested, initiating the workflow processing that may involve criteria evaluation and processor execution.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "EntityTransitionRequest",
  "entityType": "string",
  "entityId": "string",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": "uuid",
  "actorId": "string",
  "source": "urn:system:entity-service",
  "payload": {
    "transitionRequest": {
      "entityId": "string",
      "entityType": "string",
      "currentState": "string",
      "targetState": "string",
      "transitionName": "string"
    },
    "transitionContext": {
      "triggeredBy": "user|system|schedule|event",
      "triggerReason": "string",
      "businessContext": {}
    },
    "entityData": {
      "currentVersion": "number",
      "entityContent": {}
    }
  },
  "metadata": {
    "traceId": "string",
    "workflowContext": {
      "workflowName": "string",
      "workflowVersion": "string",
      "executionMode": "SYNC|ASYNC"
    }
  }
}
```

#### Triggering Conditions
- User-initiated state transition
- System-triggered state transition
- Scheduled state transition
- Event-driven state transition

#### Event Processing
- **Primary Handlers**: WorkflowEngine
- **Processing SLA**: 60 seconds
- **Downstream Events**: EntityTransitionResponse, EntityProcessorCalculationRequest, EntityCriteriaCalculationRequest

---

## Monitoring and Health Events

### SystemHealthCheck

**Event ID**: `system.health.check`  
**Event Type**: `SystemEvent`  
**Category**: `Monitoring`  
**Version**: `1.0.0`  
**Status**: `Active`

#### Overview
Triggered periodically to assess system health and operational status across all components and external integrations.

#### Event Structure
```json
{
  "eventId": "uuid",
  "eventType": "SystemHealthCheck",
  "entityType": "SYSTEM_HEALTH",
  "entityId": "system",
  "timestamp": "iso8601",
  "version": "1.0.0",
  "correlationId": "uuid",
  "causationId": null,
  "actorId": "health-monitor",
  "source": "urn:system:health-monitor",
  "payload": {
    "healthCheckId": "string",
    "checkType": "comprehensive|basic|targeted",
    "systemComponents": {
      "database": "healthy|degraded|unhealthy",
      "messageQueue": "healthy|degraded|unhealthy",
      "calculationCluster": "healthy|degraded|unhealthy",
      "externalSystems": "healthy|degraded|unhealthy"
    },
    "performanceMetrics": {
      "avgResponseTimeMs": "number",
      "throughputTps": "number",
      "errorRate": "number",
      "resourceUtilization": "number"
    },
    "alertConditions": {
      "criticalAlerts": "number",
      "warningAlerts": "number",
      "infoAlerts": "number"
    }
  },
  "metadata": {
    "traceId": "string",
    "monitoringContext": {
      "checkFrequency": "string",
      "alertThresholds": {},
      "escalationRules": {}
    }
  }
}
```

#### Triggering Conditions
- Scheduled health check execution
- System startup health verification
- Alert condition triggered health check
- Manual health check initiated

#### Event Processing
- **Primary Handlers**: HealthMonitoringService
- **Processing SLA**: 30 seconds
- **Downstream Events**: SystemAlert, PerformanceAlert

---

## Event Processing Patterns

### Request-Response Pattern
System events often follow request-response patterns where a request event triggers processing that results in a corresponding response event:
- EntityProcessorCalculationRequest → EntityProcessorCalculationResponse
- EntityCriteriaCalculationRequest → EntityCriteriaCalculationResponse
- EntityTransitionRequest → EntityTransitionResponse

### Cluster Coordination Pattern
Calculation cluster events coordinate distributed processing:
- CalculationMemberJoinEvent → CalculationMemberGreetEvent
- CalculationMemberKeepAliveEvent (periodic heartbeat)
- Load balancing and failover coordination

### Integration Monitoring Pattern
External system integration events provide continuous monitoring:
- ExternalSystemConnected → DataSynchronizationCompleted
- ExternalSystemDisconnected → IntegrationAlert
- Connection health monitoring and automatic recovery
