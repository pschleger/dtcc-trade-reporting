# DTCC Regulatory Reporting System - Workflow Dependencies and Coordination

## Cross-Workflow Dependencies

### Primary Processing Chain
The core business flow follows this dependency chain:

```
TradeConfirmation → Trade → Position → RegulatoryReport
```

**Dependency Details**:
1. **TradeConfirmation** must reach `processed` state before **Trade** can be created
2. **Trade** must reach `confirmed` state before **Position** calculation begins
3. **Position** must reach `validated` state before **RegulatoryReport** generation
4. **RegulatoryReport** must reach `completed` state to satisfy regulatory obligations

### Master Data Dependencies
All transactional workflows depend on master data entities:

```
Counterparty (active) ← Trade
Product (active) ← Trade
```

**Dependency Details**:
- **Trade** workflow cannot proceed beyond `validating` state without active **Counterparty**
- **Trade** workflow cannot proceed beyond `validating` state without active **Product**
- **Position** calculations require both **Counterparty** and **Product** to be in `active` state

### Amendment and Cancellation Dependencies
Trade lifecycle modifications have specific dependencies:

```
Trade (active) ← Amendment
Trade (active) ← Cancellation
```

**Dependency Details**:
- **Amendment** workflow can only be initiated when **Trade** is in `active` state
- **Cancellation** workflow can only be initiated when **Trade** is in `active` state
- **Trade** transitions to `amending` or `cancelling` states block other modifications
- **Position** recalculation is triggered when **Amendment** reaches `applied` state
- **Position** adjustment is triggered when **Cancellation** reaches `executed` state

### Reconciliation Dependencies
Position reconciliation has timing and state dependencies:

```
Position (current) ← ReconciliationResult
ProcessingBatch (scheduled) → ReconciliationResult
```

**Dependency Details**:
- **ReconciliationResult** can only be initiated when **Position** is in `current` state
- **ProcessingBatch** coordinates multiple **ReconciliationResult** workflows
- **Position** may transition to `reconciling` state during active reconciliation

---

## Workflow Coordination Points

### Event-Driven Coordination
Workflows coordinate through business events:

#### Trade Processing Events
- `TradeConfirmation.processed` → triggers `Trade.new`
- `Trade.confirmed` → triggers `Position.calculating`
- `Trade.amended` → triggers `Position.recalculating`
- `Trade.cancelled` → triggers `Position.recalculating`

#### Position Events
- `Position.validated` → triggers `RegulatoryReport.generating` (if reporting required)
- `Position.current` → enables `ReconciliationResult.initiated`

#### Reporting Events
- `RegulatoryReport.completed` → updates `Position` reporting status
- `ReconciliationResult.completed-clean` → confirms `Position` accuracy

### State Synchronization Points
Critical synchronization points where workflows must coordinate:

#### Trade Amendment Coordination
```
Trade.active → Trade.amending
  ↓
Amendment.pending → Amendment.applied
  ↓
Trade.amending → Trade.active
Position.current → Position.recalculating → Position.current
```

#### Batch Processing Coordination
```
ProcessingBatch.scheduled → ProcessingBatch.running
  ↓
Multiple ReconciliationResult.initiated
  ↓
All ReconciliationResult.completed → ProcessingBatch.completed
```

### Conflict Resolution
When multiple workflows attempt to modify the same entity:

#### Trade State Conflicts
- **Amendment** and **Cancellation** cannot run simultaneously on the same **Trade**
- **Trade** in `amending` or `cancelling` state blocks new modifications
- **Position** recalculation is queued until **Trade** returns to `active` state

#### Position Calculation Conflicts
- Multiple **Trade** updates queue **Position** recalculations
- **ReconciliationResult** blocks **Position** updates during reconciliation
- **RegulatoryReport** generation waits for **Position** to reach stable state

---

## Manual vs Automated Transition Patterns

### Fully Automated Workflows
These workflows run without manual intervention under normal conditions:
- **TradeConfirmation**: `received` → `processed` (happy path)
- **Position**: `calculating` → `current` (happy path)
- **ProcessingBatch**: `scheduled` → `completed` (happy path)

### Semi-Automated Workflows
These workflows require manual intervention at specific points:
- **Trade**: Manual intervention only on validation failures
- **RegulatoryReport**: Manual intervention on DTCC rejections
- **Amendment/Cancellation**: Manual intervention on processing failures

### Manual-Controlled Workflows
These workflows require explicit manual approval:
- **Counterparty**: Manual approval required for `validated` → `active`
- **Product**: Manual approval required for `validated` → `approved`

### Error Recovery Patterns
All workflows follow consistent error recovery patterns:

#### Automatic Retry Pattern
```
processing → processing-failed → retrying → processing
```
Applied to: **RegulatoryReport** submission failures

#### Manual Review Pattern
```
processing → processing-failed → manual-review → reprocessing → processing
```
Applied to: All workflows for unrecoverable errors

#### Escalation Pattern
```
manual-review → escalated → resolved → reprocessing
```
Applied to: Complex issues requiring senior intervention

---

## Terminal State Coordination

### Successful Completion
Workflows reach terminal states through different paths:

#### Natural Completion
- **Trade**: `active` → `maturing` → `matured` → `archived`
- **RegulatoryReport**: `generating` → `completed` → `archived`

#### Business-Driven Completion
- **Trade**: `active` → `cancelling` → `cancelled` → `archived`
- **Amendment**: `pending` → `applied` → `completed` → `archived`

#### Administrative Completion
- **Counterparty**: `active` → `archiving` → `archived`
- **Product**: `active` → `deprecating` → `deprecated` → `archived`

### Failure Termination
Workflows may terminate in failure states:
- **TradeConfirmation**: `validation-failed` → `rejected` → `archived`
- **Trade**: `validation-failed` → `rejected` → `archived`

### Retention and Archival
All workflows eventually reach `archived` state:
- Retention periods vary by entity type and regulatory requirements
- Archival is automated based on business rules
- Archived entities maintain audit trail but are removed from active processing

---

## Workflow Interaction Patterns

### Sequential Processing
```
TradeConfirmation → Trade → Position → RegulatoryReport
```
Each workflow must complete before the next begins.

### Parallel Processing
```
Multiple TradeConfirmation workflows → Single Position workflow
```
Multiple trades can be processed simultaneously, feeding into position calculation.

### Fan-Out Processing
```
Single Trade → Multiple Amendment/Cancellation workflows
```
A trade can have multiple amendments over its lifecycle.

### Aggregation Processing
```
Multiple Position workflows → Single ReconciliationResult workflow
```
Reconciliation processes multiple positions simultaneously.

### Coordination Processing
```
ProcessingBatch → Multiple ReconciliationResult workflows
```
Batch processing coordinates multiple related workflows.
