# Position Management and Reconciliation Swimlane Diagrams

## Overview

This document provides swimlane diagrams showing position calculation, real-time position updates, reconciliation processes, and position reporting flows, including all entity interactions, timing dependencies, and error resolution procedures for the DTCC Regulatory Reporting System.

## Diagram Summary

### 1. Real-Time Position Calculation from Trade Events
**Purpose**: Shows automatic position calculation triggered by trade events with real-time aggregation and netting.

**Key Components**:
- Trade Event → Position Calculation Engine → Position Entity Updates
- Real-time trade aggregation and netting calculations
- Market data integration for position valuation
- Risk metric calculation and threshold monitoring

**SLA Requirements**:
- Trade event processing: < 2s
- Position calculation: < 15s
- Risk metric calculation: < 10s
- End-to-end position update: < 30s

### 2. Position Aggregation and Netting Processes
**Purpose**: Demonstrates position aggregation across multiple dimensions and netting calculations.

**Key Components**:
- Multi-dimensional aggregation (counterparty, product, portfolio)
- Netting set calculations for collateral management
- Currency conversion and base currency normalization
- Position hierarchy management

**SLA Requirements**:
- Aggregation processing: < 20s
- Netting calculations: < 15s
- Currency conversion: < 5s
- Total aggregation cycle: < 45s

### 3. Daily Position Reconciliation Process
**Purpose**: Shows complete daily reconciliation between internal and external position data.

**Key Components**:
- External position data retrieval from counterparty systems
- Multi-dimensional position comparison and variance analysis
- Tolerance threshold application and break detection
- Reconciliation result generation and reporting

**SLA Requirements**:
- External data retrieval: < 300s
- Position comparison: < 180s
- Reconciliation analysis: < 120s
- Total daily reconciliation: < 600s (10 minutes)

### 4. Reconciliation Break Detection and Resolution
**Purpose**: Handles reconciliation discrepancies with investigation workflows and resolution procedures.

**Key Features**:
- Automated break detection with significance analysis
- Discrepancy categorization and priority assignment
- Investigation workflow with manual review processes
- Resolution tracking and audit trail maintenance

**SLA Requirements**:
- Break detection: < 30s
- Initial investigation: < 4 hours
- Resolution tracking: < 24 hours
- Escalation procedures: < 1 hour for critical breaks

### 5. Position Recalculation from Trade Amendments
**Purpose**: Manages position recalculation triggered by trade amendments with delta processing.

**Key Features**:
- Amendment impact assessment and position delta calculation
- Original position reversal and amended position application
- Cascading position updates across aggregation levels
- Amendment-specific audit trail and reporting

**SLA Requirements**:
- Amendment validation: < 15s
- Position delta calculation: < 20s
- Position recalculation: < 25s
- Total amendment processing: < 60s

### 6. Batch Processing and End-of-Day Procedures
**Purpose**: Comprehensive batch processing for end-of-day position calculations and reporting.

**Key Components**:
- Batch position calculation for all portfolios
- End-of-day position snapshots and archival
- Regulatory reporting preparation and submission
- Performance monitoring and SLA validation

**Processing Windows**:
- Batch processing window: 18:00 - 22:00 EST
- Position calculation: 18:00 - 20:00 EST
- Reconciliation processing: 20:00 - 21:00 EST
- Reporting preparation: 21:00 - 22:00 EST

### 7. Escalation Procedures for Unresolved Breaks
**Purpose**: Shows escalation workflows for critical reconciliation breaks requiring manual intervention.

**Escalation Levels**:
- Level 1: Operations Team (< 1 hour)
- Level 2: Risk Management (< 4 hours)
- Level 3: Senior Management (< 24 hours)
- Level 4: Regulatory Notification (< 48 hours)

### 8. Position Reporting Threshold Monitoring
**Purpose**: Continuous monitoring of position thresholds with automated alerting and reporting.

**Monitoring Components**:
- Real-time threshold monitoring across all positions
- Automated alert generation for threshold breaches
- Regulatory reporting trigger evaluation
- Performance dashboard updates and SLA tracking

**Alert Thresholds**:
- Position limit breaches: Immediate alert
- Risk metric thresholds: < 5 minutes
- Regulatory thresholds: < 15 minutes
- SLA breaches: < 1 minute

## Decision Points and Branching Logic

### Critical Decision Points
1. **Trade Event Classification**: Determines position calculation scope and priority
2. **Position Impact Assessment**: High vs low impact position changes
3. **Reconciliation Break Significance**: Tolerance-based break classification
4. **Escalation Trigger Evaluation**: Severity-based escalation routing
5. **Threshold Breach Assessment**: Regulatory vs operational threshold evaluation

### Branching Logic
- **Position Calculation Paths**: Real-time vs batch processing routes
- **Reconciliation Outcomes**: Clean vs break detection workflows
- **Error Handling**: Retry logic vs escalation procedures
- **Threshold Monitoring**: Alert generation vs regulatory reporting triggers

## Timing Requirements and SLA Constraints

### Real-Time Processing SLAs
- **Trade Event Processing**: < 2s for event ingestion and validation
- **Position Calculation**: < 15s for single position calculation
- **Risk Metric Updates**: < 10s for risk calculation completion
- **Threshold Monitoring**: < 5s for threshold evaluation

### Batch Processing SLAs
- **Daily Reconciliation**: < 10 minutes for complete reconciliation cycle
- **End-of-Day Processing**: < 4 hours for complete batch cycle
- **Position Aggregation**: < 45s for multi-dimensional aggregation
- **Reporting Preparation**: < 2 hours for regulatory report generation

### Error Recovery SLAs
- **Automated Retry**: < 300s for transient error recovery
- **Manual Investigation**: < 4 hours for break investigation
- **Escalation Response**: < 1 hour for critical issue escalation
- **Resolution Tracking**: < 24 hours for break resolution

## Synchronization Points

### Parallel Processing
- Position calculations across multiple counterparties and products
- Reconciliation processing for different position types
- Risk metric calculations and threshold evaluations
- External system data retrieval and validation

### Synchronization Requirements
- All trade events must be processed before position calculation
- Position calculations must complete before reconciliation processing
- Reconciliation results must be available before reporting preparation
- Threshold monitoring must be synchronized with position updates

## Validation Against Use Cases

These swimlane diagrams have been validated against the following position management use cases:
- UC-PM-001: Calculate Real-Time Positions from Trade Events
- UC-PM-002: Perform Daily Position Reconciliation
- UC-PM-003: Handle Reconciliation Break Investigation
- UC-PM-004: Process Position Recalculation from Amendments
- UC-PM-005: Execute End-of-Day Batch Processing
- UC-PM-006: Monitor Position Thresholds and Generate Alerts

All diagrams include complete error handling, timing requirements, SLA constraints, and regulatory compliance considerations as specified in the position management requirements.

## Mermaid Diagram Definitions

### 1. Real-Time Position Calculation from Trade Events

```mermaid
sequenceDiagram
    participant TE as Trade Event
    participant ES as Event Streaming
    participant PCE as Position Calculation Engine
    participant PE as Position Entity
    participant RCE as Risk Calculation Engine
    participant TME as Threshold Monitoring Engine
    participant MS as Monitoring System
    participant AT as Audit Trail

    Note over TE,AT: Real-Time Position Calculation Flow

    TE->>+ES: TradeConfirmed Event
    Note right of ES: SLA: < 2s event processing

    ES->>+PCE: Trigger Position Calculation
    Note right of PCE: SLA: < 15s calculation

    PCE->>PCE: Retrieve Related Trades
    PCE->>PCE: Apply Aggregation Rules
    PCE->>PCE: Calculate Notional Amounts
    PCE->>PCE: Apply Currency Conversion

    PCE->>+PE: Update Position Entity
    Note right of PE: State: calculating → calculated
    PE->>PE: Store Calculated Values
    PE->>PE: Update Calculation Timestamp
    PE-->>-PCE: Position Updated

    PCE->>+RCE: Calculate Risk Metrics
    Note right of RCE: SLA: < 10s risk calculation

    RCE->>RCE: Calculate VaR and ES
    RCE->>RCE: Update Risk Exposures
    RCE->>PE: Update Risk Metrics
    RCE-->>-PCE: Risk Calculation Complete

    PCE->>ES: Publish PositionCalculated Event
    PCE-->>-ES: Calculation Complete

    ES->>+TME: Trigger Threshold Monitoring
    Note right of TME: SLA: < 5s threshold check

    TME->>TME: Evaluate Position Limits
    TME->>TME: Check Risk Thresholds
    TME->>TME: Assess Regulatory Limits

    alt Threshold Breach Detected
        TME->>MS: Generate Threshold Alert
        TME->>ES: Publish ThresholdBreach Event
        Note right of MS: Alert: Position limit exceeded
    else Thresholds Within Limits
        TME->>MS: Update Monitoring Dashboard
    end
    TME-->>-ES: Monitoring Complete

    ES->>+AT: Log Position Calculation
    AT->>AT: Create Audit Record
    AT->>AT: Store Calculation Details
    AT-->>-ES: Audit Complete

    Note over TE,AT: End-to-End SLA: < 30s total processing time
```

### 2. Position Aggregation and Netting Processes

```mermaid
sequenceDiagram
    participant PCE as Position Calculation Engine
    participant AGE as Aggregation Engine
    parameter NE as Netting Engine
    participant CCE as Currency Conversion Engine
    participant PE as Position Entity
    participant ES as Event Streaming
    participant MS as Monitoring System
    participant AT as Audit Trail

    Note over PCE,AT: Position Aggregation and Netting Flow

    PCE->>+AGE: Trigger Position Aggregation
    Note right of AGE: SLA: < 20s aggregation

    AGE->>AGE: Group by Counterparty
    AGE->>AGE: Group by Product Type
    AGE->>AGE: Group by Portfolio
    AGE->>AGE: Group by Netting Set

    par Counterparty Aggregation
        AGE->>AGE: Sum Notional by Counterparty
        AGE->>AGE: Calculate Counterparty Exposure
    and Product Aggregation
        AGE->>AGE: Sum Notional by Product
        AGE->>AGE: Calculate Product Concentration
    and Portfolio Aggregation
        AGE->>AGE: Sum Portfolio Positions
        AGE->>AGE: Calculate Portfolio Risk
    end

    AGE->>+NE: Calculate Netting Sets
    Note right of NE: SLA: < 15s netting

    NE->>NE: Apply Netting Agreements
    NE->>NE: Calculate Net Exposures
    NE->>NE: Determine Collateral Requirements
    NE-->>-AGE: Netting Complete

    AGE->>+CCE: Convert to Base Currency
    Note right of CCE: SLA: < 5s conversion

    CCE->>CCE: Retrieve Current FX Rates
    CCE->>CCE: Apply Currency Conversion
    CCE->>CCE: Validate Conversion Results
    CCE-->>-AGE: Conversion Complete

    AGE->>+PE: Update Aggregated Positions
    PE->>PE: Store Aggregated Values
    PE->>PE: Update Hierarchy Links
    PE->>PE: Set Aggregation Timestamp
    PE-->>-AGE: Positions Updated

    AGE->>ES: Publish PositionAggregated Event
    AGE-->>-PCE: Aggregation Complete

    ES->>+MS: Update Aggregation Metrics
    MS->>MS: Track Aggregation Performance
    MS->>MS: Monitor Netting Efficiency
    MS-->>-ES: Metrics Updated

    ES->>+AT: Log Aggregation Process
    AT->>AT: Record Aggregation Details
    AT->>AT: Store Netting Results
    AT-->>-ES: Audit Complete

    Note over PCE,AT: Total Aggregation SLA: < 45s
```

### 3. Daily Position Reconciliation Process

```mermaid
sequenceDiagram
    participant SCH as Scheduler
    participant RCE as Reconciliation Engine
    participant EDS as External Data Service
    participant PCE as Position Calculation Engine
    participant CE as Comparison Engine
    participant RRE as Reconciliation Result Entity
    participant ES as Event Streaming
    participant NS as Notification Service
    participant AT as Audit Trail

    Note over SCH,AT: Daily Position Reconciliation Flow

    SCH->>+RCE: Trigger Daily Reconciliation
    Note right of RCE: Schedule: 06:00 EST daily

    RCE->>+EDS: Retrieve External Positions
    Note right of EDS: SLA: < 300s data retrieval

    par Counterparty A Data
        EDS->>EDS: Connect to Counterparty A
        EDS->>EDS: Download Position Files
        EDS->>EDS: Validate Data Format
    and Counterparty B Data
        EDS->>EDS: Connect to Counterparty B
        EDS->>EDS: Download Position Files
        EDS->>EDS: Validate Data Format
    and Counterparty C Data
        EDS->>EDS: Connect to Counterparty C
        EDS->>EDS: Download Position Files
        EDS->>EDS: Validate Data Format
    end

    EDS-->>-RCE: External Data Retrieved

    RCE->>+PCE: Retrieve Internal Positions
    PCE->>PCE: Extract Position Data
    PCE->>PCE: Apply Business Date Filter
    PCE->>PCE: Format for Comparison
    PCE-->>-RCE: Internal Data Ready

    RCE->>+CE: Execute Position Comparison
    Note right of CE: SLA: < 180s comparison

    CE->>CE: Normalize Data Formats
    CE->>CE: Match Positions by Key
    CE->>CE: Compare Notional Amounts
    CE->>CE: Compare Market Values
    CE->>CE: Apply Tolerance Thresholds

    alt Positions Match Within Tolerance
        CE->>RRE: Create Clean Reconciliation
        CE->>ES: Publish ReconciliationClean Event
    else Discrepancies Detected
        CE->>CE: Calculate Variance Amounts
        CE->>CE: Classify Break Significance
        CE->>RRE: Create Break Reconciliation
        CE->>ES: Publish ReconciliationBreak Event
    end

    CE-->>-RCE: Comparison Complete

    RCE->>+RRE: Store Reconciliation Results
    Note right of RRE: SLA: < 120s analysis
    RRE->>RRE: Generate Summary Statistics
    RRE->>RRE: Create Detailed Break Report
    RRE->>RRE: Set Investigation Status
    RRE-->>-RCE: Results Stored

    alt Critical Breaks Detected
        RCE->>+NS: Send Break Notifications
        NS->>NS: Generate Alert Messages
        NS->>NS: Route to Operations Team
        NS-->>-RCE: Notifications Sent
    end

    RCE->>ES: Publish ReconciliationComplete Event
    RCE-->>-SCH: Reconciliation Complete

    ES->>+AT: Log Reconciliation Process
    AT->>AT: Record Reconciliation Results
    AT->>AT: Store Break Details
    AT->>AT: Create Compliance Record
    AT-->>-ES: Audit Complete

    Note over SCH,AT: Total Reconciliation SLA: < 600s (10 minutes)
```

### 4. Reconciliation Break Detection and Resolution

```mermaid
sequenceDiagram
    participant RCE as Reconciliation Engine
    participant BDE as Break Detection Engine
    participant IWE as Investigation Workflow Engine
    participant MRQ as Manual Review Queue
    participant OT as Operations Team
    participant RTE as Resolution Tracking Engine
    participant ES as Event Streaming
    participant NS as Notification Service
    participant AT as Audit Trail

    Note over RCE,AT: Reconciliation Break Detection and Resolution Flow

    RCE->>+BDE: Analyze Reconciliation Results
    Note right of BDE: SLA: < 30s break detection

    BDE->>BDE: Apply Significance Thresholds
    BDE->>BDE: Classify Break Categories
    BDE->>BDE: Calculate Impact Scores
    BDE->>BDE: Determine Priority Levels

    alt Significant Breaks Detected
        BDE->>+IWE: Trigger Investigation Workflow
        Note right of IWE: SLA: < 4 hours investigation

        IWE->>IWE: Create Investigation Case
        IWE->>IWE: Assign Priority Level
        IWE->>IWE: Route to Appropriate Team

        IWE->>+MRQ: Queue for Manual Review
        MRQ->>MRQ: Create Review Task
        MRQ->>MRQ: Set SLA Timers
        MRQ->>+OT: Assign to Operations Team

        Note right of OT: Investigation Process
        OT->>OT: Analyze Break Details
        OT->>OT: Research Root Cause
        OT->>OT: Identify Resolution Actions

        alt Break Resolved
            OT->>+RTE: Record Resolution
            RTE->>RTE: Update Break Status
            RTE->>RTE: Document Resolution Steps
            RTE->>RTE: Close Investigation Case
            RTE->>ES: Publish BreakResolved Event
            RTE-->>-OT: Resolution Recorded

        else Escalation Required
            OT->>+NS: Escalate to Senior Team
            Note right of NS: Escalation Level 2
            NS->>NS: Generate Escalation Alert
            NS->>NS: Route to Risk Management
            NS-->>-OT: Escalation Initiated

            alt Critical Escalation
                NS->>+NS: Escalate to Management
                Note right of NS: Escalation Level 3
                NS->>NS: Generate Critical Alert
                NS->>NS: Route to Senior Management
                NS-->>-NS: Critical Escalation Active
            end
        end

        OT-->>-MRQ: Investigation Complete
        MRQ-->>-IWE: Review Complete
        IWE-->>-BDE: Investigation Workflow Complete

    else Minor Breaks Within Tolerance
        BDE->>ES: Publish MinorBreak Event
        BDE->>AT: Log Minor Break Details
    end

    BDE->>ES: Publish BreakDetectionComplete Event
    BDE-->>-RCE: Break Detection Complete

    ES->>+AT: Log Break Detection Process
    AT->>AT: Record Break Analysis
    AT->>AT: Store Investigation Results
    AT->>AT: Create Resolution Audit Trail
    AT-->>-ES: Audit Complete

    Note over RCE,AT: Break Detection SLA: < 30s<br/>Investigation SLA: < 4 hours<br/>Escalation SLA: < 1 hour for critical
```

### 5. Position Recalculation from Trade Amendments

```mermaid
sequenceDiagram
    participant TE as Trade Event
    participant AWE as Amendment Workflow Engine
    parameter IAE as Impact Assessment Engine
    participant PCE as Position Calculation Engine
    participant PE as Position Entity
    participant RCE as Risk Calculation Engine
    participant ES as Event Streaming
    participant RGE as Report Generation Engine
    participant AT as Audit Trail

    Note over TE,AT: Position Recalculation from Trade Amendments Flow

    TE->>+AWE: TradeAmended Event
    Note right of AWE: SLA: < 15s validation

    AWE->>AWE: Validate Amendment Authority
    AWE->>AWE: Check Amendment Rules
    AWE->>AWE: Verify Original Trade Status

    AWE->>+IAE: Assess Amendment Impact
    Note right of IAE: SLA: < 20s assessment

    IAE->>IAE: Calculate Position Delta
    IAE->>IAE: Identify Affected Positions
    IAE->>IAE: Assess Risk Impact
    IAE->>IAE: Determine Recalculation Scope
    IAE-->>-AWE: Impact Assessment Complete

    AWE->>+PCE: Trigger Position Recalculation
    Note right of PCE: SLA: < 25s recalculation

    PCE->>+PE: Retrieve Original Positions
    PE->>PE: Load Current Position Data
    PE->>PE: Identify Related Positions
    PE-->>-PCE: Original Positions Retrieved

    PCE->>PCE: Reverse Original Position Impact
    PCE->>PCE: Apply Amendment Changes
    PCE->>PCE: Recalculate Aggregated Positions
    PCE->>PCE: Update Position Hierarchies

    PCE->>+PE: Update Position Entities
    PE->>PE: Store Recalculated Values
    PE->>PE: Update Amendment References
    PE->>PE: Set Recalculation Timestamp
    PE-->>-PCE: Positions Updated

    PCE->>+RCE: Recalculate Risk Metrics
    RCE->>RCE: Update VaR Calculations
    RCE->>RCE: Recalculate Exposures
    RCE->>RCE: Update Risk Limits
    RCE-->>-PCE: Risk Recalculation Complete

    PCE->>ES: Publish PositionRecalculated Event
    PCE-->>-AWE: Recalculation Complete

    ES->>+RGE: Evaluate Amendment Reporting
    RGE->>RGE: Check Reporting Requirements
    RGE->>RGE: Generate Amendment Reports
    RGE->>ES: Publish AmendmentReported Event
    RGE-->>-ES: Reporting Complete

    AWE->>ES: Publish AmendmentProcessed Event
    AWE-->>-TE: Amendment Processing Complete

    ES->>+AT: Log Amendment Processing
    AT->>AT: Record Amendment Details
    AT->>AT: Store Position Changes
    AT->>AT: Link to Original Trade
    AT->>AT: Create Compliance Record
    AT-->>-ES: Audit Complete

    Note over TE,AT: Total Amendment Processing SLA: < 60s
```

### 6. Batch Processing and End-of-Day Procedures

```mermaid
graph TD
    subgraph "Batch Processing Control"
        SCH[Scheduler<br/>18:00 EST Trigger]
        BPE[Batch Processing Engine<br/>Orchestration]
        BM[Batch Monitor<br/>Progress Tracking]
    end

    subgraph "Position Calculation Phase (18:00-20:00)"
        PC1[Portfolio A<br/>Position Calculation]
        PC2[Portfolio B<br/>Position Calculation]
        PC3[Portfolio C<br/>Position Calculation]
        AGG[Position Aggregation<br/>Cross-Portfolio]
    end

    subgraph "Reconciliation Phase (20:00-21:00)"
        RC1[Counterparty Reconciliation<br/>External Data Comparison]
        RC2[Internal Reconciliation<br/>Cross-System Validation]
        RR[Reconciliation Reporting<br/>Break Analysis]
    end

    subgraph "Reporting Phase (21:00-22:00)"
        RG1[Regulatory Report Generation<br/>DTCC Preparation]
        RG2[Internal Report Generation<br/>Management Reporting]
        RS[Report Submission<br/>External Delivery]
    end

    subgraph "Monitoring & Control"
        MS[Monitoring System<br/>SLA Tracking]
        ES[Event Streaming<br/>Progress Events]
        AT[Audit Trail<br/>Batch Logging]
    end

    SCH -->|Trigger Batch| BPE
    BPE -->|Phase 1| PC1
    BPE -->|Phase 1| PC2
    BPE -->|Phase 1| PC3

    PC1 -->|Complete| AGG
    PC2 -->|Complete| AGG
    PC3 -->|Complete| AGG

    AGG -->|Phase 2| RC1
    AGG -->|Phase 2| RC2

    RC1 -->|Complete| RR
    RC2 -->|Complete| RR

    RR -->|Phase 3| RG1
    RR -->|Phase 3| RG2

    RG1 -->|Complete| RS
    RG2 -->|Complete| RS

    BPE -->|Monitor| BM
    BM -->|Status| MS
    BPE -->|Events| ES
    ES -->|Audit| AT

    style SCH fill:#e1f5fe
    style BPE fill:#f3e5f5
    style AGG fill:#e8f5e8
    style RR fill:#fff3e0
    style RS fill:#fce4ec
```

### 7. Escalation Procedures for Unresolved Breaks

```mermaid
graph TD
    subgraph "Break Detection"
        BD[Break Detected<br/>Reconciliation Engine]
        SA[Significance Analysis<br/>Impact Assessment]
        PC[Priority Classification<br/>Urgency Matrix]
    end

    subgraph "Level 1 Escalation (< 1 hour)"
        L1[Operations Team<br/>Initial Investigation]
        L1A[Root Cause Analysis<br/>Data Validation]
        L1R[Resolution Attempt<br/>Standard Procedures]
    end

    subgraph "Level 2 Escalation (< 4 hours)"
        L2[Risk Management<br/>Senior Analysis]
        L2A[Impact Assessment<br/>Business Risk Evaluation]
        L2R[Advanced Resolution<br/>System Adjustments]
    end

    subgraph "Level 3 Escalation (< 24 hours)"
        L3[Senior Management<br/>Executive Review]
        L3A[Strategic Assessment<br/>Business Impact]
        L3D[Decision Authority<br/>Resolution Approval]
    end

    subgraph "Level 4 Escalation (< 48 hours)"
        L4[Regulatory Notification<br/>Compliance Team]
        L4R[Regulatory Reporting<br/>Incident Disclosure]
        L4F[Follow-up Actions<br/>Remediation Plan]
    end

    subgraph "Monitoring & Tracking"
        ET[Escalation Tracker<br/>SLA Monitoring]
        NS[Notification Service<br/>Alert Management]
        AT[Audit Trail<br/>Escalation History]
    end

    BD -->|Analyze| SA
    SA -->|Classify| PC

    PC -->|Minor Break| L1
    PC -->|Significant Break| L2
    PC -->|Critical Break| L3

    L1 -->|Investigation| L1A
    L1A -->|Attempt| L1R

    L1R -->|Unresolved| L2
    L2 -->|Analysis| L2A
    L2A -->|Advanced| L2R

    L2R -->|Unresolved| L3
    L3 -->|Review| L3A
    L3A -->|Decision| L3D

    L3D -->|Regulatory Impact| L4
    L4 -->|Report| L4R
    L4R -->|Plan| L4F

    L1 -->|Track| ET
    L2 -->|Track| ET
    L3 -->|Track| ET
    L4 -->|Track| ET

    ET -->|Alert| NS
    ET -->|Log| AT

    style BD fill:#ffebee
    style L1 fill:#e3f2fd
    style L2 fill:#fff3e0
    style L3 fill:#f3e5f5
    style L4 fill:#fce4ec
```

### 8. Position Reporting Threshold Monitoring

```mermaid
sequenceDiagram
    participant PE as Position Entity
    participant TME as Threshold Monitoring Engine
    participant TC as Threshold Configuration
    participant AE as Alert Engine
    participant RTE as Regulatory Trigger Engine
    participant MS as Monitoring System
    participant NS as Notification Service
    participant RGE as Report Generation Engine
    participant AT as Audit Trail

    Note over PE,AT: Continuous Position Threshold Monitoring Flow

    PE->>+TME: Position Updated Event
    Note right of TME: Real-time monitoring trigger

    TME->>+TC: Retrieve Threshold Configuration
    TC->>TC: Load Position Limits
    TC->>TC: Load Risk Thresholds
    TC->>TC: Load Regulatory Thresholds
    TC-->>-TME: Configuration Retrieved

    TME->>TME: Evaluate Position Limits
    TME->>TME: Check Risk Metric Thresholds
    TME->>TME: Assess Regulatory Thresholds
    TME->>TME: Calculate Threshold Utilization

    alt Position Limit Breach
        TME->>+AE: Generate Position Alert
        Note right of AE: Alert: Immediate notification
        AE->>AE: Create Critical Alert
        AE->>+NS: Send Immediate Notification
        NS->>NS: Route to Trading Desk
        NS->>NS: Route to Risk Management
        NS-->>-AE: Notifications Sent
        AE-->>-TME: Position Alert Complete

    else Risk Threshold Breach
        TME->>+AE: Generate Risk Alert
        Note right of AE: Alert: < 5 minutes
        AE->>AE: Create High Priority Alert
        AE->>+NS: Send Risk Notification
        NS->>NS: Route to Risk Team
        NS-->>-AE: Risk Notification Sent
        AE-->>-TME: Risk Alert Complete

    else Regulatory Threshold Breach
        TME->>+RTE: Trigger Regulatory Evaluation
        Note right of RTE: Alert: < 15 minutes
        RTE->>RTE: Assess Reporting Requirements
        RTE->>RTE: Check Regulatory Deadlines
        RTE->>RTE: Determine Reporting Obligations

        alt Immediate Reporting Required
            RTE->>+RGE: Generate Regulatory Report
            RGE->>RGE: Create Threshold Report
            RGE->>RGE: Submit to Regulators
            RGE-->>-RTE: Report Submitted
        end

        RTE->>+NS: Send Regulatory Alert
        NS->>NS: Route to Compliance Team
        NS-->>-RTE: Regulatory Alert Sent
        RTE-->>-TME: Regulatory Evaluation Complete

    else Thresholds Within Limits
        TME->>MS: Update Monitoring Dashboard
        TME->>MS: Record Threshold Status
    end

    TME->>+MS: Update Threshold Metrics
    Note right of MS: SLA: < 1 minute for updates
    MS->>MS: Update Real-time Dashboard
    MS->>MS: Track Threshold Utilization
    MS->>MS: Monitor Alert Frequency

    alt SLA Breach Detected
        MS->>NS: Generate SLA Alert
        MS->>AT: Log SLA Breach
    end
    MS-->>-TME: Metrics Updated

    TME->>+AT: Log Threshold Monitoring
    AT->>AT: Record Threshold Evaluation
    AT->>AT: Store Alert Generation
    AT->>AT: Create Compliance Record
    AT-->>-TME: Audit Complete

    TME-->>-PE: Monitoring Complete

    Note over PE,AT: Threshold Monitoring SLAs:<br/>Position Limits: Immediate alert<br/>Risk Thresholds: < 5 minutes<br/>Regulatory Thresholds: < 15 minutes<br/>SLA Monitoring: < 1 minute
```

## Conclusion

These complete swimlane diagrams provide detailed visualization of all position management and reconciliation processes within the DTCC Regulatory Reporting System. Each diagram includes:

- **Complete entity interactions** between Position, Trade, ReconciliationResult, and supporting entities
- **Detailed timing dependencies** with specific SLA requirements for each processing stage
- **Comprehensive error resolution procedures** with escalation paths and manual intervention points
- **Real-time and batch processing flows** covering all operational scenarios
- **Regulatory compliance considerations** integrated throughout all processes

The diagrams support both operational teams in understanding day-to-day processes and technical teams in implementing the system architecture. All flows have been validated against position management use cases and include the necessary audit trails and compliance logging required for regulatory reporting.

**Key Performance Indicators**:
- Real-time position calculation: < 30s end-to-end
- Daily reconciliation: < 10 minutes complete cycle
- Break investigation: < 4 hours for resolution
- Threshold monitoring: < 5 minutes for alerts
- Batch processing: 4-hour window for complete EOD cycle

These swimlane diagrams serve as the definitive reference for position management and reconciliation workflows, ensuring consistent implementation across all system components and providing clear operational procedures for all stakeholders.
