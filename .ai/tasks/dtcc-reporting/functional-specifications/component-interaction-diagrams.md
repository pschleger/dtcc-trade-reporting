# Entity Workflow Interaction Diagrams

## Overview

This document provides comprehensive diagrams showing entity state transitions and workflow interactions in the DTCC regulatory reporting system. Each diagram illustrates how entity workflows process business events through state machines, with processors and criteria implementing the workflow logic within each entity.

## TradeConfirmation Entity Workflow

```mermaid
graph TD
    A[FpML Message Received] --> B[TradeConfirmation Entity Created]
    B --> C[Received State]
    C --> D[validateFpMLMessage Processor]
    D --> E{validateFpMLMessage Criterion}
    E -->|Success| F[Validated State]
    E -->|Failure| G[Failed State]
    F --> H[convertToTrade Processor]
    H --> I{isTradeCreated Criterion}
    I -->|Success| J[Processed State]
    I -->|Failure| K[Review State]
    J --> L[archiveConfirmation Processor]
    L --> M[Archived State]
    G --> N[notifyProcessingFailure Processor]
    N --> O[Error State]

    subgraph "Entity State Machine"
        P[Received] --> Q[Validated]
        Q --> R[Processed]
        R --> S[Archived]
        P --> T[Failed]
        T --> U[Error]
    end
```

## Trade Entity Lifecycle Workflow

```mermaid
graph TD
    A[Trade Entity Created] --> B[Draft State]
    B --> C[validateBusinessRules Processor]
    C --> D{validateBusinessRules Criterion}
    D -->|Success| E[Active State]
    D -->|Failure| F[Invalid State]
    E --> G{isApproachingMaturity Criterion}
    G -->|True| H[processMaturity Processor]
    G -->|False| I[Continue Active]
    E --> J{Amendment Event}
    J -->|Received| K[Amendment State]
    E --> L{Cancellation Event}
    L -->|Received| M[Cancellation State]
    H --> N[Matured State]
    K --> O[processAmendment Processor]
    O --> P[Amended State]
    M --> Q[processCancellation Processor]
    Q --> R[Cancelled State]

    subgraph "Entity State Machine"
        S[Draft] --> T[Active]
        T --> U[Matured]
        T --> V[Amended]
        T --> W[Cancelled]
        S --> X[Invalid]
    end
```

## Position Entity Workflow

```mermaid
graph TD
    A[Trade State Change] --> B[Position Entity Updated]
    B --> C[Calculating State]
    C --> D[calculatePosition Processor]
    D --> E{isCalculationSuccessful Criterion}
    E -->|Success| F[Calculated State]
    E -->|Failure| G[Calculation Failed State]
    F --> H[validatePosition Processor]
    H --> I{isValidationSuccessful Criterion}
    I -->|Success| J[Validated State]
    I -->|Failure| K[Validation Failed State]
    J --> L[reconcilePosition Processor]
    L --> M{isReconciliationSuccessful Criterion}
    M -->|Success| N[Reconciled State]
    M -->|Failure| O[Reconciliation Failed State]
    N --> P[Ready for Reporting State]

    subgraph "Entity State Machine"
        Q[Calculating] --> R[Calculated]
        R --> S[Validated]
        S --> T[Reconciled]
        T --> U[Ready for Reporting]
        Q --> V[Failed]
    end
```

## RegulatoryReport Entity Workflow

```mermaid
graph TD
    A[Position Ready for Reporting] --> B[RegulatoryReport Entity Created]
    B --> C[Draft State]
    C --> D[generateReport Processor]
    D --> E{isGenerationSuccessful Criterion}
    E -->|Success| F[Generated State]
    E -->|Failure| G[Generation Failed State]
    F --> H[validateReport Processor]
    H --> I{isValidationSuccessful Criterion}
    I -->|Success| J[Validated State]
    I -->|Failure| K[Validation Failed State]
    J --> L[submitReport Processor]
    L --> M{isSubmissionSuccessful Criterion}
    M -->|Success| N[Submitted State]
    M -->|Failure| O[Submission Failed State]
    N --> P[processAcknowledgment Processor]
    P --> Q{isDtccAcknowledgmentReceived Criterion}
    Q -->|Success| R[Acknowledged State]
    Q -->|Rejection| S[Rejected State]
    R --> T[archiveReport Processor]
    T --> U[Archived State]

    subgraph "Entity State Machine"
        V[Draft] --> W[Generated]
        W --> X[Validated]
        X --> Y[Submitted]
        Y --> Z[Acknowledged]
        Z --> AA[Archived]
        Y --> BB[Rejected]
    end
```

## Error Handling and Escalation Flow

```mermaid
graph TD
    A[Processing Error Detected] --> B{Error Severity}
    B -->|Critical| C[escalateIssue Processor]
    B -->|Standard| D[notifyFailure Processor]
    C --> E[Operations Team Alert]
    D --> F[Standard Notification]
    E --> G{Manual Intervention Required}
    G -->|Yes| H[Manual Review Queue]
    G -->|No| I[Automated Recovery]
    F --> J{Retry Available}
    J -->|Yes| K[Retry Processing]
    J -->|No| L[Move to Failed State]
    
    subgraph "Recovery Mechanisms"
        M[Transient Errors] --> N[Automatic Retry]
        O[Data Errors] --> P[Manual Correction]
        Q[System Errors] --> R[System Recovery]
    end
```

## Batch Processing Coordination

```mermaid
graph TD
    A[Batch Schedule] --> B[initializeBatch Processor]
    B --> C[executeBatchProcessing Processor]
    C --> D[monitorBatchProgress Processor]
    D --> E{areAllItemsProcessed Criterion}
    E -->|No| F{areProcessingErrorsDetected Criterion}
    F -->|Yes| G[handleBatchErrors Processor]
    F -->|No| D
    G --> H{isMaxRetriesExceeded Criterion}
    H -->|No| I[retryBatchProcessing Processor]
    H -->|Yes| J[Batch Failed State]
    I --> D
    E -->|Yes| K[completeBatchProcessing Processor]
    K --> L[archiveBatch Processor]
    
    subgraph "Parallel Processing"
        M[Item 1] --> N[Process 1]
        O[Item 2] --> P[Process 2]
        Q[Item N] --> R[Process N]
    end
```

## Cross-Workflow Dependencies

```mermaid
graph LR
    A[TradeConfirmation] --> B[Trade]
    B --> C[Position]
    C --> D[RegulatoryReport]
    B --> E[Amendment]
    B --> F[Cancellation]
    C --> G[ReconciliationResult]
    
    subgraph "Master Data Dependencies"
        H[Counterparty] --> A
        I[Product] --> A
        H --> B
        I --> B
    end
    
    subgraph "Batch Processing"
        J[ProcessingBatch] --> B
        J --> C
        J --> D
    end
```

## Data Quality and Validation Chain

```mermaid
graph TD
    A[Raw Data Input] --> B[Schema Validation]
    B --> C[Business Rule Validation]
    C --> D[Data Quality Assessment]
    D --> E{Quality Score >= Threshold}
    E -->|Yes| F[Processing Approved]
    E -->|No| G[Quality Review Required]
    G --> H{Manual Override Available}
    H -->|Yes| I[Override Applied]
    H -->|No| J[Processing Rejected]
    I --> F
    
    subgraph "Validation Layers"
        K[Structural] --> L[Semantic]
        L --> M[Business Logic]
        M --> N[Regulatory Compliance]
    end
```

## Performance and Scalability Patterns

```mermaid
graph TD
    A[Load Balancer] --> B[Processing Node 1]
    A --> C[Processing Node 2]
    A --> D[Processing Node N]
    
    B --> E[Processor Instance]
    C --> F[Processor Instance]
    D --> G[Processor Instance]
    
    E --> H[Shared Cache]
    F --> H
    G --> H
    
    H --> I[Master Data]
    H --> J[Configuration]
    H --> K[Reference Data]
    
    subgraph "Scaling Strategies"
        L[Horizontal Scaling] --> M[Node Addition]
        N[Vertical Scaling] --> O[Resource Increase]
        P[Caching] --> Q[Performance Optimization]
    end
```

## Integration Points and External Dependencies

```mermaid
graph TD
    A[DTCC Regulatory Reporting System] --> B[DTCC GTR]
    A --> C[LEI Registry]
    A --> D[Market Data Service]
    A --> E[Holiday Calendar Service]
    
    B --> F[Report Submission]
    B --> G[Acknowledgment Receipt]
    C --> H[LEI Validation]
    D --> I[Currency Rates]
    E --> J[Business Date Calculation]
    
    subgraph "Internal Services"
        K[Master Data Service] --> L[Counterparty Data]
        K --> M[Product Data]
        N[Entity Service] --> O[Trade Management]
        N --> P[Position Management]
    end
    
    subgraph "Monitoring and Alerting"
        Q[Performance Monitoring] --> R[SLA Tracking]
        S[Error Monitoring] --> T[Alert Generation]
        U[Audit Service] --> V[Compliance Tracking]
    end
```

## Entity Workflow Interaction Summary

### Key Entity Workflow Patterns

1. **State-Driven Processing**: Entity workflows execute through defined state transitions with validation gates
2. **Conditional State Transitions**: Criteria determine entity state changes based on business logic
3. **Error State Management**: Failures transition entities to appropriate error states with notification workflows
4. **Concurrent Entity Processing**: Multiple entity workflows execute concurrently across the platform
5. **Entity Data Transformation**: Processors transform entity data during state transitions
6. **External System Integration**: Entity workflows interact with external services through defined interfaces

### Performance Considerations

1. **Entity Caching**: Frequently accessed entities cached for rapid state retrieval
2. **Distributed Processing**: Entity workflows distributed across platform nodes for scalability
3. **Workflow Timeout Management**: Each entity workflow has appropriate timeout configurations
4. **Resource Optimization**: Entity workflows designed for efficient platform resource utilization

### Error Handling Strategy

1. **Error State Management**: Entity workflows transition to appropriate error states for partial failures
2. **Retry Workflows**: Transient errors trigger automatic retry workflows with backoff
3. **Manual Review States**: Complex errors transition entities to manual review states
4. **Escalation Workflows**: Critical errors trigger escalation entity workflows to operations teams

### Entity Data Integrity

1. **State Transition Validation**: Each entity state transition includes validation checkpoints
2. **Audit Trail Entities**: All entity interactions create audit trail entities for compliance
3. **Data Quality Workflows**: Continuous quality assessment through dedicated entity workflows
4. **Cross-Entity Consistency**: Entity relationships maintain data consistency through workflow coordination
