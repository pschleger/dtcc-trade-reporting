# DTCC Regulatory Reporting System - Workflow State Machine Designs

## Executive Summary

This document defines the high-level state machines that govern entity lifecycles in the DTCC reporting system. Each state machine is designed to comply with the Cyoda WorkflowConfiguration.json schema and supports both automated and manual transitions based on business requirements.

## State Machine Design Principles

### Naming Conventions
- State names start with a letter and contain only letters, numbers, underscores, and hyphens
- Transition names are descriptive and action-oriented
- Terminal states are clearly identified

### Transition Types
- **Automated**: System-triggered transitions based on business logic
- **Manual**: User-initiated transitions requiring explicit approval
- **Conditional**: Transitions dependent on external criteria or validation

### Error Handling
- Each workflow includes error states and recovery paths
- Failed states allow for manual intervention and retry mechanisms
- Audit trails are maintained for all state transitions

---

## 1. TradeConfirmation Workflow

**Purpose**: Process incoming FpML trade confirmation messages through validation and conversion to Trade entities.

**Initial State**: `received`

### States and Transitions

```
received → validating (auto)
validating → validated (auto, on validation success)
validating → validation-failed (auto, on validation failure)
validated → processing (auto)
processing → processed (auto, on successful trade creation)
processing → processing-failed (auto, on processing failure)
validation-failed → manual-review (manual)
processing-failed → manual-review (manual)
manual-review → reprocessing (manual, after correction)
manual-review → rejected (manual, if unrecoverable)
reprocessing → processing (auto)
processed → archived (auto, after retention period)
rejected → archived (auto, after retention period)
```

### Terminal States
- `processed`: Successfully converted to Trade entity
- `rejected`: Permanently rejected due to unrecoverable errors
- `archived`: Moved to long-term storage

### Manual Transitions
- `validation-failed` → `manual-review`: Operations team review
- `processing-failed` → `manual-review`: Technical team intervention
- `manual-review` → `reprocessing`: After manual correction
- `manual-review` → `rejected`: Permanent rejection decision

---

## 2. Trade Workflow

**Purpose**: Manage the complete lifecycle of trade entities from confirmation through maturity.

**Initial State**: `new`

### States and Transitions

```
new → validating (auto)
validating → validated (auto, on business rule validation)
validating → validation-failed (auto, on validation failure)
validated → confirmed (auto)
confirmed → active (auto)
active → amending (manual, on amendment request)
active → cancelling (manual, on cancellation request)
active → maturing (auto, approaching maturity date)
amending → active (auto, on successful amendment)
amending → amendment-failed (auto, on amendment failure)
cancelling → cancelled (auto, on successful cancellation)
cancelling → cancellation-failed (auto, on cancellation failure)
maturing → matured (auto, on maturity date)
validation-failed → manual-review (manual)
amendment-failed → manual-review (manual)
cancellation-failed → manual-review (manual)
manual-review → revalidating (manual, after correction)
manual-review → rejected (manual, if unrecoverable)
revalidating → validating (auto)
cancelled → archived (auto, after retention period)
matured → archived (auto, after retention period)
rejected → archived (auto, after retention period)
```

### Terminal States
- `cancelled`: Trade permanently cancelled
- `matured`: Trade reached natural maturity
- `rejected`: Trade permanently rejected
- `archived`: Moved to long-term storage

### Manual Transitions
- `active` → `amending`: User-initiated amendment
- `active` → `cancelling`: User-initiated cancellation
- `validation-failed` → `manual-review`: Operations review
- `amendment-failed` → `manual-review`: Technical intervention
- `cancellation-failed` → `manual-review`: Technical intervention

---

## 3. Position Workflow

**Purpose**: Calculate and maintain position data through aggregation of trades and regulatory reporting.

**Initial State**: `calculating`

### States and Transitions

```
calculating → calculated (auto, on successful calculation)
calculating → calculation-failed (auto, on calculation error)
calculated → validating (auto)
validating → validated (auto, on validation success)
validating → validation-failed (auto, on validation failure)
validated → reporting-ready (auto, if reporting required)
validated → reconciling (auto, if reconciliation due)
validated → current (auto, if no further action required)
reporting-ready → reporting (auto)
reporting → reported (auto, on successful report generation)
reporting → reporting-failed (auto, on report generation failure)
reconciling → reconciled (auto, on successful reconciliation)
reconciling → reconciliation-failed (auto, on reconciliation failure)
current → recalculating (auto, on trade update)
reported → current (auto)
reconciled → current (auto)
calculation-failed → manual-review (manual)
validation-failed → manual-review (manual)
reporting-failed → manual-review (manual)
reconciliation-failed → manual-review (manual)
manual-review → recalculating (manual, after correction)
recalculating → calculating (auto)
```

### Terminal States
- None (Position is a living entity that continuously updates)

### Manual Transitions
- `calculation-failed` → `manual-review`: Technical team intervention
- `validation-failed` → `manual-review`: Data quality review
- `reporting-failed` → `manual-review`: Reporting team review
- `reconciliation-failed` → `manual-review`: Operations review

---

## 4. RegulatoryReport Workflow

**Purpose**: Generate, validate, and submit regulatory reports to DTCC GTR.

**Initial State**: `generating`

### States and Transitions

```
generating → generated (auto, on successful generation)
generating → generation-failed (auto, on generation error)
generated → validating (auto)
validating → validated (auto, on validation success)
validating → validation-failed (auto, on validation failure)
validated → ready-for-submission (auto)
ready-for-submission → submitting (auto, at scheduled time)
submitting → submitted (auto, on successful submission)
submitting → submission-failed (auto, on submission error)
submitted → acknowledged (auto, on DTCC acknowledgment)
submitted → rejected-by-dtcc (auto, on DTCC rejection)
acknowledged → completed (auto)
rejected-by-dtcc → correcting (manual)
generation-failed → manual-review (manual)
validation-failed → manual-review (manual)
submission-failed → retrying (auto, if retries available)
submission-failed → manual-review (auto, if max retries exceeded)
retrying → submitting (auto, after retry delay)
correcting → regenerating (manual, after correction)
manual-review → regenerating (manual, after review)
regenerating → generating (auto)
completed → archived (auto, after retention period)
```

### Terminal States
- `completed`: Successfully submitted and acknowledged
- `archived`: Moved to long-term storage

### Manual Transitions
- `rejected-by-dtcc` → `correcting`: Manual correction required
- `generation-failed` → `manual-review`: Technical review
- `validation-failed` → `manual-review`: Data quality review
- `correcting` → `regenerating`: After manual correction
- `manual-review` → `regenerating`: After technical review

---

## 5. Counterparty Workflow

**Purpose**: Manage counterparty master data lifecycle including validation and regulatory compliance.

**Initial State**: `draft`

### States and Transitions

```
draft → validating (auto)
validating → validated (auto, on validation success)
validating → validation-failed (auto, on validation failure)
validated → active (manual, after approval)
active → updating (manual, on data update request)
active → suspending (manual, on compliance issue)
updating → validating (auto)
suspending → suspended (auto)
suspended → reactivating (manual, after issue resolution)
reactivating → validating (auto)
validation-failed → manual-review (manual)
manual-review → correcting (manual)
correcting → validating (auto)
active → archiving (manual, for decommission)
suspended → archiving (manual, for decommission)
archiving → archived (auto)
```

### Terminal States
- `archived`: Permanently decommissioned

### Manual Transitions
- `validated` → `active`: Approval required
- `active` → `updating`: Data update request
- `active` → `suspending`: Compliance action
- `suspended` → `reactivating`: Issue resolution
- `validation-failed` → `manual-review`: Data quality review
- `manual-review` → `correcting`: Manual correction
- `active` → `archiving`: Decommission decision
- `suspended` → `archiving`: Decommission decision

---

## 6. Product Workflow

**Purpose**: Manage product reference data lifecycle including taxonomy compliance and calculation parameters.

**Initial State**: `draft`

### States and Transitions

```
draft → validating (auto)
validating → validated (auto, on validation success)
validating → validation-failed (auto, on validation failure)
validated → approved (manual, after product review)
approved → active (auto)
active → updating (manual, on specification update)
active → deprecating (manual, for product retirement)
updating → validating (auto)
deprecating → deprecated (auto)
deprecated → archiving (auto, after grace period)
validation-failed → manual-review (manual)
manual-review → correcting (manual)
correcting → validating (auto)
archiving → archived (auto)
```

### Terminal States
- `archived`: Permanently retired

### Manual Transitions
- `validated` → `approved`: Product committee approval
- `active` → `updating`: Specification change request
- `active` → `deprecating`: Product retirement decision
- `validation-failed` → `manual-review`: Product review
- `manual-review` → `correcting`: Manual correction

---

## 7. Amendment Workflow

**Purpose**: Process trade amendments with validation and impact assessment.

**Initial State**: `pending`

### States and Transitions

```
pending → validating (auto)
validating → validated (auto, on validation success)
validating → validation-failed (auto, on validation failure)
validated → impact-assessing (auto)
impact-assessing → impact-assessed (auto, on successful assessment)
impact-assessing → impact-assessment-failed (auto, on assessment error)
impact-assessed → applying (auto)
applying → applied (auto, on successful application)
applying → application-failed (auto, on application error)
applied → reporting (auto, if reporting required)
applied → completed (auto, if no reporting required)
reporting → reported (auto, on successful reporting)
reporting → reporting-failed (auto, on reporting error)
reported → completed (auto)
validation-failed → manual-review (manual)
impact-assessment-failed → manual-review (manual)
application-failed → manual-review (manual)
reporting-failed → manual-review (manual)
manual-review → reprocessing (manual, after correction)
reprocessing → validating (auto)
completed → archived (auto, after retention period)
```

### Terminal States
- `completed`: Amendment successfully applied
- `archived`: Moved to long-term storage

### Manual Transitions
- `validation-failed` → `manual-review`: Data validation review
- `impact-assessment-failed` → `manual-review`: Technical review
- `application-failed` → `manual-review`: Operations review
- `reporting-failed` → `manual-review`: Reporting team review
- `manual-review` → `reprocessing`: After manual correction

---

## 8. Cancellation Workflow

**Purpose**: Process trade cancellations with authorization and impact reversal.

**Initial State**: `requested`

### States and Transitions

```
requested → authorizing (auto)
authorizing → authorized (auto, on authorization success)
authorizing → authorization-failed (auto, on authorization failure)
authorized → impact-assessing (auto)
impact-assessing → impact-assessed (auto, on successful assessment)
impact-assessing → impact-assessment-failed (auto, on assessment error)
impact-assessed → executing (auto)
executing → executed (auto, on successful execution)
executing → execution-failed (auto, on execution error)
executed → reporting (auto, if reporting required)
executed → completed (auto, if no reporting required)
reporting → reported (auto, on successful reporting)
reporting → reporting-failed (auto, on reporting error)
reported → completed (auto)
authorization-failed → manual-review (manual)
impact-assessment-failed → manual-review (manual)
execution-failed → manual-review (manual)
reporting-failed → manual-review (manual)
manual-review → reprocessing (manual, after correction)
reprocessing → authorizing (auto)
completed → archived (auto, after retention period)
```

### Terminal States
- `completed`: Cancellation successfully executed
- `archived`: Moved to long-term storage

### Manual Transitions
- `authorization-failed` → `manual-review`: Authorization review
- `impact-assessment-failed` → `manual-review`: Technical review
- `execution-failed` → `manual-review`: Operations review
- `reporting-failed` → `manual-review`: Reporting team review
- `manual-review` → `reprocessing`: After manual correction

---

## 9. ReconciliationResult Workflow

**Purpose**: Execute position reconciliation processes and manage discrepancy resolution.

**Initial State**: `initiated`

### States and Transitions

```
initiated → data-gathering (auto)
data-gathering → data-gathered (auto, on successful data collection)
data-gathering → data-gathering-failed (auto, on data collection failure)
data-gathered → comparing (auto)
comparing → compared (auto, on successful comparison)
comparing → comparison-failed (auto, on comparison error)
compared → analyzing (auto)
analyzing → analyzed (auto, on successful analysis)
analyzing → analysis-failed (auto, on analysis error)
analyzed → completed-clean (auto, if no discrepancies)
analyzed → discrepancies-found (auto, if discrepancies exist)
discrepancies-found → investigating (manual)
investigating → resolved (manual, after investigation)
investigating → escalated (manual, if complex issue)
escalated → resolved (manual, after escalation handling)
resolved → completed-with-resolution (auto)
data-gathering-failed → manual-review (manual)
comparison-failed → manual-review (manual)
analysis-failed → manual-review (manual)
manual-review → retrying (manual, after correction)
retrying → data-gathering (auto)
completed-clean → archived (auto, after retention period)
completed-with-resolution → archived (auto, after retention period)
```

### Terminal States
- `completed-clean`: Reconciliation completed with no issues
- `completed-with-resolution`: Reconciliation completed with resolved discrepancies
- `archived`: Moved to long-term storage

### Manual Transitions
- `discrepancies-found` → `investigating`: Manual investigation required
- `investigating` → `resolved`: Investigation completed
- `investigating` → `escalated`: Complex issue escalation
- `escalated` → `resolved`: Escalation resolved
- `data-gathering-failed` → `manual-review`: Technical review
- `comparison-failed` → `manual-review`: Technical review
- `analysis-failed` → `manual-review`: Technical review
- `manual-review` → `retrying`: After manual correction

---

## 10. ProcessingBatch Workflow

**Purpose**: Coordinate batch processing operations across multiple entities.

**Initial State**: `scheduled`

### States and Transitions

```
scheduled → starting (auto, at scheduled time)
starting → running (auto)
running → monitoring (auto)
monitoring → completing (auto, when all items processed)
monitoring → error-detected (auto, on processing errors)
completing → completed (auto, on successful completion)
error-detected → error-handling (auto)
error-handling → retrying (auto, if retries available)
error-handling → failed (auto, if max retries exceeded)
retrying → running (auto, after retry delay)
failed → manual-review (manual)
manual-review → restarting (manual, after correction)
restarting → starting (auto)
completed → archived (auto, after retention period)
failed → archived (manual, after issue resolution)
```

### Terminal States
- `completed`: Batch processing successfully completed
- `failed`: Batch processing permanently failed
- `archived`: Moved to long-term storage

### Manual Transitions
- `failed` → `manual-review`: Technical team intervention
- `manual-review` → `restarting`: After manual correction
- `failed` → `archived`: After issue documentation

