# Trade Processing Swimlane Diagrams

## Overview

This document provides swimlane diagrams showing the complete flow of trade processing events from FpML message ingestion through trade confirmation, validation, and conversion, including all entity interactions, decision points, and error handling paths.

## Diagram Summary

### 1. FpML Message Ingestion and Validation Flow
**Purpose**: Shows the initial processing of FpML messages from trading systems through validation and entity creation.

**Key Components**:
- Trading System → Message Gateway → Trade Processing Engine
- External validation via GLEIF LEI Registry and Reference Data
- Event streaming for audit and monitoring
- Schema validation and business rule validation

**SLA Requirements**:
- Message ingestion: < 1s
- Schema validation: < 2s
- Business validation: < 10s
- End-to-end validation: < 15s

### 2. Successful Trade Confirmation Processing Swimlane
**Purpose**: Demonstrates the complete successful flow from trade confirmation through regulatory reporting.

**Key Components**:
- TradeConfirmation entity creation and workflow execution
- Trade entity conversion with counterparty and product mapping
- Position calculation and impact assessment
- Regulatory report generation and DTCC GTR submission
- Complete audit trail creation

**SLA Requirements**:
- Validation: < 10s
- Trade conversion: < 30s
- Position calculation: < 15s
- Report generation: < 20s
- DTCC submission: < 30s
- **Total end-to-end: < 90s**

### 3. Trade Validation Failure Scenarios Swimlane
**Purpose**: Shows error handling paths for different types of validation failures.

**Failure Types**:
- Schema validation failures → immediate rejection
- Business rule violations → manual review queue
- External system failures → retry logic with exponential backoff

**Error Recovery**:
- Exponential backoff retry (2^attempt seconds)
- Maximum 3 retry attempts
- Dead letter queue for permanent failures
- Manual review queue for business rule violations
- SLA monitoring and escalation

**SLA Requirements**:
- Error recovery: < 300s for automated retry
- Manual review: < 4 hours

### 4. Trade Amendment Processing Swimlane
**Purpose**: Handles trade amendment requests with position recalculation and regulatory reporting.

**Key Features**:
- Amendment authority validation
- Original trade linking and preservation
- Position delta calculation and recalculation
- Amendment-specific regulatory reporting
- Complete audit trail with state change tracking

**SLA Requirements**:
- Amendment validation: < 15s
- Amendment processing: < 30s
- Position recalculation: < 20s
- Amendment reporting: < 25s
- **Total amendment processing: < 90s**

### 5. Trade Cancellation Processing Swimlane
**Purpose**: Manages trade cancellation with position reversal and regulatory compliance.

**Key Features**:
- Cancellation authority and eligibility validation
- Dependency checking (no active positions)
- Complete position reversal and risk metric recalculation
- Cancellation-specific regulatory reporting
- Comprehensive audit trail

**SLA Requirements**:
- Cancellation validation: < 10s
- Cancellation processing: < 20s
- Position reversal: < 15s
- Cancellation reporting: < 20s
- **Total cancellation processing: < 65s**

### 6. Error Handling and Retry Mechanisms Swimlane
**Purpose**: Comprehensive error handling architecture with circuit breaker patterns.

**Key Components**:
- Error detection and classification (transient vs permanent)
- Exponential backoff retry logic (2s, 4s, 8s intervals)
- Dead letter queue for failed messages
- Manual review queue for operations team
- Circuit breaker pattern for external system protection

**External System SLAs**:
- DTCC GTR: 99.5% availability
- GLEIF Registry: 99% availability
- Reference Data: 99.9% availability

### 7. External System Interactions Swimlane
**Purpose**: Shows integration patterns with external systems and fallback mechanisms.

**Integration Points**:
- Trading Systems (< 2s response)
- GLEIF LEI Registry (< 10s, 24h cache)
- Reference Data (< 5s, 1h cache)
- Market Data (< 3s, real-time)
- DTCC GTR (< 30s submission)

**Fallback Mechanisms**:
- Cached data for temporary outages
- Circuit breaker protection
- Degraded mode processing
- Dead letter queuing for critical failures

### 8. Audit Trail and Compliance Logging Swimlane
**Purpose**: Comprehensive audit and compliance logging for regulatory requirements.

**Key Features**:
- Immutable audit record creation with cryptographic integrity
- Regulatory impact assessment across multiple frameworks (EMIR, CFTC, MiFID II, ASIC)
- Real-time compliance evaluation and reporting
- Structured regulatory data storage
- Audit analytics and anomaly detection

**Audit SLAs**:
- Event capture: < 100ms
- Immutable storage: < 500ms
- Compliance evaluation: < 2s
- Regulatory reporting: < 24h
- Audit availability: 99.99%

## Decision Points and Branching Logic

### Critical Decision Points
1. **Message Structure Validation**: FpML schema compliance check
2. **Business Rule Validation**: Comprehensive business logic validation
3. **External System Availability**: Circuit breaker status evaluation
4. **Position Impact Assessment**: High vs low impact determination
5. **Regulatory Reporting Requirements**: Multi-jurisdiction compliance evaluation

### Branching Logic
- **Success Paths**: Continue to next processing stage
- **Validation Failures**: Route to appropriate error handling
- **External System Failures**: Trigger retry logic or fallback mechanisms
- **SLA Breaches**: Escalate to operations team
- **Regulatory Impact**: Trigger compliance reporting workflows

## Timing Requirements and SLA Constraints

### Processing SLAs
- **Trade Confirmation**: < 90s end-to-end
- **Trade Amendment**: < 90s with position recalculation (< 120s total)
- **Trade Cancellation**: < 65s with position reversal (< 100s with reporting)
- **Error Recovery**: < 300s automated, < 4h manual review

### External System SLAs
- **DTCC GTR**: 99.5% availability, < 30s submission
- **GLEIF Registry**: 99% availability, < 10s validation
- **Reference Data**: 99.9% availability, < 5s validation
- **Trading Systems**: 99.9% availability, < 2s response

### Audit and Compliance SLAs
- **Audit Capture**: < 100ms
- **Compliance Evaluation**: < 2s
- **Regulatory Reporting**: < 24h
- **Audit Availability**: 99.99%

## Synchronization Points

### Parallel Processing
- External system validations (LEI, Reference Data, Market Data)
- Position calculation and regulatory report generation
- Audit logging and compliance evaluation

### Synchronization Requirements
- All validations must complete before trade creation
- Position calculation must complete before regulatory reporting
- Audit logging must complete before transaction finalization
- External system calls must respect circuit breaker status

## Validation Against Use Cases

These swimlane diagrams have been validated against the following trade processing use cases:
- UC-001: Process New Trade Confirmation
- UC-002: Handle Trade Amendment
- UC-003: Process Trade Cancellation
- UC-004: Handle Validation Failures
- UC-005: External System Integration
- UC-006: Audit and Compliance Logging

All diagrams include complete error handling, timing requirements, SLA constraints, and regulatory compliance considerations as specified in the trade processing requirements.

## Mermaid Diagram Definitions

### 1. FpML Message Ingestion and Validation Flow

```mermaid
graph TD
    subgraph "Trading System"
        TS[Trading System]
    end

    subgraph "Message Gateway"
        MG[Message Gateway<br/>REST API/Kafka]
        MV[Message Validator<br/>Schema & Integrity]
    end

    subgraph "Trade Processing Engine"
        TC[TradeConfirmation<br/>Entity Creation]
        FV[FpML Validator<br/>Business Rules]
        DD[Duplicate Detector<br/>Message Dedup]
    end

    subgraph "External Systems"
        LEI[GLEIF LEI Registry<br/>Counterparty Validation]
        RD[Reference Data<br/>Product Validation]
    end

    subgraph "Event Streaming"
        ES[Event Stream<br/>Kafka/Pulsar]
    end

    subgraph "Audit & Monitoring"
        AT[Audit Trail<br/>Immutable Log]
        MS[Monitoring System<br/>Metrics & Alerts]
    end

    TS -->|FpML XML Message<br/>SLA: < 1s| MG
    MG -->|Schema Validation<br/>SLA: < 2s| MV

    MV -->|Valid Schema| TC
    MV -->|Invalid Schema| ES

    TC -->|Create Entity<br/>State: received| DD
    DD -->|No Duplicate| FV
    DD -->|Duplicate Found| ES

    FV -->|Validate LEI| LEI
    FV -->|Validate Product| RD

    LEI -->|LEI Valid| FV
    LEI -->|LEI Invalid| ES
    RD -->|Product Valid| FV
    RD -->|Product Invalid| ES

    FV -->|All Validations Pass<br/>State: validated| ES
    FV -->|Validation Failure<br/>State: validation-failed| ES

    ES -->|Log All Events| AT
    ES -->|Metrics & Alerts| MS

    style TS fill:#e1f5fe
    style MG fill:#f3e5f5
    style TC fill:#e8f5e8
    style ES fill:#fff3e0
    style AT fill:#fce4ec
```

### 2. Successful Trade Confirmation Processing Swimlane

```mermaid
sequenceDiagram
    participant TS as Trading System
    participant MG as Message Gateway
    participant TPE as Trade Processing Engine
    participant WE as Workflow Engine
    participant ES as Event Streaming
    participant PCE as Position Calculation Engine
    participant RGE as Report Generation Engine
    participant DTCC as DTCC GTR
    participant AT as Audit Trail

    Note over TS,AT: Successful Trade Confirmation Processing Flow

    TS->>+MG: FpML Trade Confirmation
    Note right of MG: SLA: < 1s ingestion

    MG->>+TPE: Validated Message
    TPE->>TPE: Create TradeConfirmation Entity
    TPE->>WE: Trigger TradeConfirmationWorkflow
    Note right of WE: State: received → validating

    WE->>+TPE: Execute validateFpMLMessage
    Note right of TPE: SLA: < 10s validation
    TPE->>TPE: Validate Business Rules
    TPE->>TPE: Extract Trade Data
    TPE-->>-WE: Validation Success

    WE->>WE: State: validating → validated
    WE->>+TPE: Execute convertToTrade
    Note right of TPE: SLA: < 30s conversion

    TPE->>TPE: Create Trade Entity
    TPE->>TPE: Map Counterparties
    TPE->>TPE: Map Product Details
    TPE->>ES: Publish TradeConfirmed Event
    TPE-->>-WE: Conversion Success

    WE->>WE: State: validated → processed

    ES->>+PCE: Trigger Position Calculation
    Note right of PCE: SLA: < 15s calculation
    PCE->>PCE: Calculate Position Impact
    PCE->>PCE: Update Position Records
    PCE->>ES: Publish PositionUpdated Event
    PCE-->>-ES: Position Calculation Complete

    ES->>+RGE: Evaluate Reporting Requirements
    Note right of RGE: SLA: < 20s evaluation
    RGE->>RGE: Check Regulatory Obligations
    RGE->>RGE: Generate DTCC Report
    RGE->>+DTCC: Submit Regulatory Report
    Note right of DTCC: SLA: < 30s submission
    DTCC-->>-RGE: Submission Acknowledged
    RGE->>ES: Publish ReportSubmitted Event
    RGE-->>-ES: Reporting Complete

    ES->>+AT: Log All Events
    AT->>AT: Create Immutable Audit Record
    AT->>AT: Store Compliance Information
    AT-->>-ES: Audit Complete

    Note over TS,AT: End-to-End SLA: < 90s total processing time
```

### 3. Trade Validation Failure Scenarios Swimlane

```mermaid
sequenceDiagram
    participant TS as Trading System
    participant MG as Message Gateway
    participant TPE as Trade Processing Engine
    participant WE as Workflow Engine
    participant ES as Event Streaming
    participant NS as Notification Service
    participant MR as Manual Review Queue
    participant AT as Audit Trail
    participant MS as Monitoring System

    Note over TS,MS: Trade Validation Failure Processing Flow

    TS->>+MG: FpML Trade Confirmation
    MG->>+TPE: Message Received
    TPE->>TPE: Create TradeConfirmation Entity
    TPE->>WE: Trigger TradeConfirmationWorkflow

    WE->>+TPE: Execute validateFpMLMessage

    alt Schema Validation Failure
        TPE->>TPE: FpML Schema Invalid
        TPE->>ES: Publish ValidationFailed Event
        TPE-->>-WE: Schema Validation Failed
        WE->>WE: State: received → validation-failed

        ES->>+NS: Send Schema Error Alert
        NS->>NS: Generate Error Notification
        NS->>TS: Send Error Response
        NS-->>-ES: Notification Sent

        ES->>+AT: Log Schema Validation Failure
        AT->>AT: Record Error Details
        AT->>AT: Store Message for Analysis
        AT-->>-ES: Audit Complete

    else Business Rule Validation Failure
        TPE->>TPE: Business Rules Failed
        TPE->>ES: Publish BusinessRuleViolation Event
        TPE-->>-WE: Business Validation Failed
        WE->>WE: State: validating → validation-failed

        ES->>+MR: Queue for Manual Review
        MR->>MR: Create Review Task
        MR->>MR: Assign to Operations Team
        MR-->>-ES: Queued for Review

        ES->>+NS: Send Business Rule Alert
        NS->>NS: Generate Detailed Error Report
        NS->>TS: Send Detailed Error Response
        NS-->>-ES: Notification Sent

    else External System Failure
        TPE->>TPE: LEI/Reference Data Unavailable
        TPE->>ES: Publish ExternalSystemError Event
        TPE-->>-WE: External Validation Failed
        WE->>WE: State: validating → retry-pending

        ES->>+TPE: Trigger Retry Logic
        Note right of TPE: Exponential Backoff<br/>Max 3 retries

        loop Retry Attempts
            TPE->>TPE: Wait (2^attempt seconds)
            TPE->>TPE: Retry External Validation

            alt Retry Success
                TPE->>ES: Publish RetrySuccess Event
                TPE->>WE: Continue Processing
                WE->>WE: State: retry-pending → validated
            else Max Retries Exceeded
                TPE->>ES: Publish MaxRetriesExceeded Event
                TPE->>MR: Queue for Manual Review
                WE->>WE: State: retry-pending → manual-review
            end
        end
        TPE-->>-ES: Retry Logic Complete
    end

    ES->>+MS: Update Error Metrics
    MS->>MS: Increment Failure Counters
    MS->>MS: Check SLA Thresholds

    alt SLA Breach Detected
        MS->>NS: Trigger SLA Alert
        MS->>MS: Escalate to Operations
    end
    MS-->>-ES: Metrics Updated

    ES->>+AT: Log Complete Failure Flow
    AT->>AT: Record Error Resolution Path
    AT->>AT: Store Compliance Information
    AT-->>-ES: Audit Complete

    Note over TS,MS: Error Recovery SLA: < 300s for automated retry<br/>Manual Review SLA: < 4 hours
```

### 4. Trade Amendment Processing Swimlane

```mermaid
sequenceDiagram
    participant TS as Trading System
    participant MG as Message Gateway
    participant TPE as Trade Processing Engine
    participant WE as Workflow Engine
    participant ES as Event Streaming
    participant PCE as Position Calculation Engine
    participant RGE as Report Generation Engine
    participant DTCC as DTCC GTR
    participant AT as Audit Trail
    participant VE as Validation Engine

    Note over TS,VE: Trade Amendment Processing Flow

    TS->>+MG: FpML Amendment Message
    Note right of MG: Amendment Type: TRADE_AMENDMENT

    MG->>+TPE: Validated Amendment
    TPE->>TPE: Create TradeConfirmation Entity
    TPE->>TPE: Link to Original Trade
    TPE->>WE: Trigger TradeWorkflow Amendment

    WE->>WE: State: active → amending
    WE->>+VE: Execute validateAmendment
    Note right of VE: SLA: < 15s validation

    VE->>VE: Validate Amendment Authority
    VE->>VE: Check Amendment Rules
    VE->>VE: Verify Original Trade Status

    alt Amendment Valid
        VE->>TPE: Amendment Validation Success
        VE-->>-WE: Validation Complete

        WE->>+TPE: Execute processAmendment
        Note right of TPE: SLA: < 30s processing

        TPE->>TPE: Create Amendment Record
        TPE->>TPE: Update Trade Entity
        TPE->>TPE: Preserve Original Values
        TPE->>ES: Publish TradeAmended Event
        TPE-->>-WE: Amendment Processing Complete

        WE->>WE: State: amending → amended

        ES->>+PCE: Trigger Position Recalculation
        Note right of PCE: SLA: < 20s recalculation

        PCE->>PCE: Calculate Position Delta
        PCE->>PCE: Update Position Records
        PCE->>PCE: Reverse Original Position Impact
        PCE->>PCE: Apply Amended Position Impact
        PCE->>ES: Publish PositionRecalculated Event
        PCE-->>-ES: Position Update Complete

        ES->>+RGE: Evaluate Amendment Reporting
        Note right of RGE: SLA: < 25s evaluation

        RGE->>RGE: Check Reporting Requirements
        RGE->>RGE: Generate Amendment Report
        RGE->>+DTCC: Submit Amendment Report
        Note right of DTCC: Report Type: LIFECYCLE
        DTCC-->>-RGE: Amendment Report Acknowledged
        RGE->>ES: Publish AmendmentReported Event
        RGE-->>-ES: Amendment Reporting Complete

    else Amendment Invalid
        VE->>ES: Publish AmendmentValidationFailed Event
        VE-->>-WE: Validation Failed

        WE->>WE: State: amending → amendment-failed

        ES->>TPE: Create Amendment Rejection
        ES->>TS: Send Amendment Rejection Notice

        ES->>+AT: Log Amendment Failure
        AT->>AT: Record Rejection Reason
        AT->>AT: Store Amendment Details
        AT-->>-ES: Audit Complete
    end

    ES->>+AT: Log Complete Amendment Flow
    AT->>AT: Create Amendment Audit Trail
    AT->>AT: Link to Original Trade Audit
    AT->>AT: Record State Changes
    AT->>AT: Store Regulatory Information
    AT-->>-ES: Amendment Audit Complete

    Note over TS,VE: Amendment Processing SLA: < 90s total<br/>Position Impact SLA: < 120s total
```

### 5. Trade Cancellation Processing Swimlane

```mermaid
sequenceDiagram
    participant TS as Trading System
    participant MG as Message Gateway
    participant TPE as Trade Processing Engine
    participant WE as Workflow Engine
    participant ES as Event Streaming
    participant PCE as Position Calculation Engine
    participant RGE as Report Generation Engine
    participant DTCC as DTCC GTR
    participant AT as Audit Trail
    participant VE as Validation Engine

    Note over TS,VE: Trade Cancellation Processing Flow

    TS->>+MG: FpML Cancellation Message
    Note right of MG: Message Type: TRADE_CANCELLATION

    MG->>+TPE: Validated Cancellation
    TPE->>TPE: Create TradeConfirmation Entity
    TPE->>TPE: Link to Original Trade
    TPE->>WE: Trigger TradeWorkflow Cancellation

    WE->>WE: State: active → cancelling
    WE->>+VE: Execute validateCancellation
    Note right of VE: SLA: < 10s validation

    VE->>VE: Validate Cancellation Authority
    VE->>VE: Check Trade Status Eligibility
    VE->>VE: Verify No Dependent Positions
    VE->>VE: Check Regulatory Constraints

    alt Cancellation Valid
        VE->>TPE: Cancellation Validation Success
        VE-->>-WE: Validation Complete

        WE->>+TPE: Execute processCancellation
        Note right of TPE: SLA: < 20s processing

        TPE->>TPE: Create Cancellation Record
        TPE->>TPE: Update Trade Status to CANCELLED
        TPE->>TPE: Preserve Original Trade Data
        TPE->>ES: Publish TradeCancelled Event
        TPE-->>-WE: Cancellation Processing Complete

        WE->>WE: State: cancelling → cancelled

        ES->>+PCE: Trigger Position Reversal
        Note right of PCE: SLA: < 15s reversal

        PCE->>PCE: Calculate Position Reversal
        PCE->>PCE: Reverse All Position Impacts
        PCE->>PCE: Update Portfolio Positions
        PCE->>PCE: Recalculate Risk Metrics
        PCE->>ES: Publish PositionReversed Event
        PCE-->>-ES: Position Reversal Complete

        ES->>+RGE: Evaluate Cancellation Reporting
        Note right of RGE: SLA: < 20s evaluation

        RGE->>RGE: Check Reporting Requirements
        RGE->>RGE: Generate Cancellation Report
        RGE->>+DTCC: Submit Cancellation Report
        Note right of DTCC: Report Type: LIFECYCLE<br/>Action: CANCEL
        DTCC-->>-RGE: Cancellation Report Acknowledged
        RGE->>ES: Publish CancellationReported Event
        RGE-->>-ES: Cancellation Reporting Complete

        ES->>TS: Send Cancellation Confirmation

    else Cancellation Invalid
        VE->>ES: Publish CancellationValidationFailed Event
        VE-->>-WE: Validation Failed

        WE->>WE: State: cancelling → cancellation-failed

        alt Trade Has Dependencies
            ES->>TPE: Create Dependency Error
            ES->>TS: Send Dependency Rejection Notice
            Note right of TS: Error: Trade has active positions

        else Regulatory Constraint
            ES->>TPE: Create Regulatory Error
            ES->>TS: Send Regulatory Rejection Notice
            Note right of TS: Error: Regulatory hold period

        else Authority Issue
            ES->>TPE: Create Authority Error
            ES->>TS: Send Authority Rejection Notice
            Note right of TS: Error: Insufficient authority
        end

        ES->>+AT: Log Cancellation Failure
        AT->>AT: Record Rejection Reason
        AT->>AT: Store Cancellation Details
        AT->>AT: Record Regulatory Impact
        AT-->>-ES: Audit Complete
    end

    ES->>+AT: Log Complete Cancellation Flow
    AT->>AT: Create Cancellation Audit Trail
    AT->>AT: Link to Original Trade Audit
    AT->>AT: Record Position Impact
    AT->>AT: Store Regulatory Compliance Data
    AT-->>-ES: Cancellation Audit Complete

    Note over TS,VE: Cancellation Processing SLA: < 65s total<br/>Position Reversal SLA: < 80s total<br/>Regulatory Reporting SLA: < 100s total
```

### 6. Error Handling and Retry Mechanisms Swimlane

```mermaid
graph TD
    subgraph "Error Detection"
        ED[Error Detector<br/>Circuit Breaker Pattern]
        ET[Error Classifier<br/>Transient vs Permanent]
        EM[Error Metrics<br/>SLA Monitoring]
    end

    subgraph "Retry Logic"
        RL[Retry Logic Engine<br/>Exponential Backoff]
        RC[Retry Counter<br/>Max 3 Attempts]
        RQ[Retry Queue<br/>Delayed Processing]
    end

    subgraph "Dead Letter Handling"
        DLQ[Dead Letter Queue<br/>Failed Messages]
        MR[Manual Review Queue<br/>Operations Team]
        ER[Error Recovery<br/>Reprocessing]
    end

    subgraph "External Systems"
        DTCC[DTCC GTR<br/>99.5% SLA]
        LEI[GLEIF Registry<br/>99% SLA]
        RD[Reference Data<br/>99.9% SLA]
    end

    subgraph "Notification & Escalation"
        NS[Notification Service<br/>Real-time Alerts]
        ES[Escalation Service<br/>SLA Breach Handling]
        MS[Monitoring System<br/>Health Checks]
    end

    subgraph "Audit & Compliance"
        AT[Audit Trail<br/>Error Tracking]
        CR[Compliance Reporter<br/>Regulatory Impact]
    end

    ED -->|Classify Error| ET
    ET -->|Transient Error| RL
    ET -->|Permanent Error| DLQ
    ET -->|SLA Impact| EM

    RL -->|Attempt 1<br/>Wait 2s| RC
    RL -->|Attempt 2<br/>Wait 4s| RC
    RL -->|Attempt 3<br/>Wait 8s| RC
    RC -->|Max Retries| DLQ
    RC -->|Success| AT

    RL -->|Queue Retry| RQ
    RQ -->|Delayed Execution| RL

    DLQ -->|Manual Review| MR
    MR -->|Fixed & Reprocess| ER
    ER -->|Resubmit| RL

    DTCC -->|Timeout/Error| ED
    LEI -->|Timeout/Error| ED
    RD -->|Timeout/Error| ED

    EM -->|SLA Breach| ES
    ED -->|Critical Error| NS
    DLQ -->|Queue Full| NS

    NS -->|Alert Operations| MR
    ES -->|Escalate| NS
    MS -->|Health Check Fail| NS

    AT -->|Error Logged| CR
    CR -->|Regulatory Impact| NS

    style ED fill:#ffebee
    style RL fill:#e3f2fd
    style DLQ fill:#fff3e0
    style NS fill:#f3e5f5
    style AT fill:#e8f5e8
```

### 7. External System Interactions Swimlane

```mermaid
sequenceDiagram
    participant TPE as Trade Processing Engine
    participant AG as API Gateway
    participant TS as Trading Systems
    participant DTCC as DTCC GTR
    participant LEI as GLEIF Registry
    participant RD as Reference Data
    participant MD as Market Data
    participant MS as Monitoring System
    participant CB as Circuit Breaker

    Note over TPE,CB: External System Integration Flow

    TPE->>+AG: Process Trade Request
    AG->>CB: Check Circuit Breaker Status

    alt All Systems Healthy
        CB->>AG: Systems Available

        par Trading System Integration
            AG->>+TS: Validate Trade Source
            Note right of TS: SLA: < 2s response
            TS-->>-AG: Source Validated
        and LEI Registry Integration
            AG->>+LEI: Validate Counterparty LEI
            Note right of LEI: SLA: < 10s response<br/>Cache: 24h TTL
            LEI-->>-AG: LEI Validation Result
        and Reference Data Integration
            AG->>+RD: Validate Product Data
            Note right of RD: SLA: < 5s response<br/>Cache: 1h TTL
            RD-->>-AG: Product Validation Result
        and Market Data Integration
            AG->>+MD: Get Current Rates
            Note right of MD: SLA: < 3s response<br/>Real-time feed
            MD-->>-AG: Market Data Retrieved
        end

        AG->>TPE: All Validations Complete

        TPE->>+AG: Submit Regulatory Report
        AG->>+DTCC: Submit to GTR
        Note right of DTCC: SLA: < 30s submission<br/>Retry: 3 attempts
        DTCC-->>-AG: Submission Acknowledged
        AG->>TPE: Report Submitted Successfully

    else Circuit Breaker Open
        CB->>AG: System Unavailable
        AG->>TPE: Fallback to Cached Data

        alt LEI Registry Down
            AG->>AG: Use Cached LEI Data
            Note right of AG: Fallback: Local LEI DB<br/>Risk: Stale data warning
        else Reference Data Down
            AG->>AG: Use Cached Product Data
            Note right of AG: Fallback: Last known good<br/>Risk: Price impact
        else DTCC GTR Down
            AG->>AG: Queue for Later Submission
            Note right of AG: Fallback: Dead letter queue<br/>Risk: Regulatory deadline
        end

        AG->>TPE: Degraded Mode Processing
    end

    AG->>+MS: Log Integration Metrics
    MS->>MS: Update SLA Dashboards
    MS->>MS: Check Threshold Breaches

    alt SLA Breach Detected
        MS->>MS: Trigger Alert
        MS->>AG: Escalate to Operations
    end
    MS-->>-AG: Metrics Logged

    Note over TPE,CB: Integration SLAs:<br/>LEI Validation: < 10s (99% availability)<br/>Reference Data: < 5s (99.9% availability)<br/>DTCC Submission: < 30s (99.5% availability)<br/>Market Data: < 3s (99.9% availability)
```

### 8. Audit Trail and Compliance Logging Swimlane

```mermaid
sequenceDiagram
    participant BE as Business Event
    participant AL as Audit Logger
    participant AT as Audit Trail Store
    participant CS as Compliance Service
    participant RS as Regulatory Store
    participant MS as Monitoring System
    participant ES as Event Streaming
    participant AR as Audit Reporter
    participant RR as Regulatory Reporter

    Note over BE,RR: Comprehensive Audit Trail and Compliance Logging Flow

    BE->>+AL: Business Event Occurred
    Note right of AL: Event Types:<br/>- Entity State Change<br/>- Workflow Transition<br/>- External System Call<br/>- User Action<br/>- System Error

    AL->>AL: Generate Audit ID
    AL->>AL: Capture Event Context
    AL->>AL: Extract Business Metadata
    AL->>AL: Calculate Immutable Signature

    AL->>+AT: Store Audit Record
    Note right of AT: Immutable Storage<br/>Cryptographic Integrity<br/>Retention: 7+ years

    AT->>AT: Validate Record Integrity
    AT->>AT: Index by Entity & Time
    AT->>AT: Apply Retention Policy
    AT-->>-AL: Audit Record Stored

    AL->>+CS: Evaluate Compliance Impact
    Note right of CS: Regulatory Frameworks:<br/>- EMIR<br/>- CFTC<br/>- MiFID II<br/>- ASIC

    CS->>CS: Check Regulatory Scope
    CS->>CS: Determine Reporting Requirements
    CS->>CS: Assess Privacy Impact
    CS->>CS: Calculate Retention Period

    alt Regulatory Reporting Required
        CS->>+RS: Store Regulatory Data
        Note right of RS: Structured for Reporting<br/>Jurisdiction-specific<br/>Real-time indexing

        RS->>RS: Apply Data Classification
        RS->>RS: Encrypt Sensitive Data
        RS->>RS: Index for Reporting
        RS-->>-CS: Regulatory Data Stored

        CS->>+RR: Schedule Regulatory Report
        Note right of RR: Report Types:<br/>- Trade Reporting<br/>- Position Reporting<br/>- Transaction Reporting<br/>- Compliance Reporting

        RR->>RR: Generate Report Content
        RR->>RR: Apply Regulatory Format
        RR->>RR: Validate Report Structure
        RR-->>-CS: Report Scheduled
    end

    CS-->>-AL: Compliance Evaluation Complete

    AL->>+ES: Publish Audit Event
    Note right of ES: Event Distribution:<br/>- Real-time Monitoring<br/>- Analytics Pipeline<br/>- Compliance Dashboard<br/>- External Audit Systems

    ES->>+MS: Update Audit Metrics
    MS->>MS: Track Audit Volume
    MS->>MS: Monitor Compliance SLAs
    MS->>MS: Check Data Quality

    alt Audit SLA Breach
        MS->>MS: Trigger Compliance Alert
        MS->>AR: Escalate to Audit Team
    end
    MS-->>-ES: Metrics Updated

    ES->>+AR: Stream to Audit Analytics
    AR->>AR: Real-time Audit Analysis
    AR->>AR: Detect Anomalies
    AR->>AR: Generate Audit Reports

    alt Audit Anomaly Detected
        AR->>MS: Trigger Investigation Alert
        AR->>CS: Flag for Compliance Review
    end
    AR-->>-ES: Analytics Complete

    ES-->>-AL: Event Distribution Complete
    AL-->>-BE: Audit Logging Complete

    Note over BE,RR: Audit SLAs:<br/>Event Capture: < 100ms<br/>Immutable Storage: < 500ms<br/>Compliance Evaluation: < 2s<br/>Regulatory Reporting: < 24h<br/>Audit Availability: 99.99%
```
