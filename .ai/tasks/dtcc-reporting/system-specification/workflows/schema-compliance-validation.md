# Cyoda WorkflowConfiguration.json Schema Compliance Validation

## Schema Requirements Analysis

Based on the Cyoda WorkflowConfiguration.json schema, the following requirements must be met:

### Required Properties
- `version`: String (default "1.0")
- `name`: String - Name of the workflow
- `initialState`: String - Initial state for entities
- `states`: Object - Map of state codes to state definitions

### Optional Properties
- `desc`: String - Description of the workflow
- `active`: Boolean (default true) - Flag indicating if workflow is active
- `criterion`: QueryCondition - Criterion to determine workflow applicability

### State Naming Requirements
- State names must start with a letter
- State names can contain only letters, numbers, underscores, and hyphens
- Minimum of 1 state required

### Transition Requirements
- `next`: String (required) - Target state code
- `name`: String (optional) - Name of the transition
- `manual`: Boolean (default false) - Manual triggering flag
- `disabled`: Boolean (default false) - Disabled flag
- `processors`: Array (optional) - List of processors
- `criterion`: QueryCondition (optional) - Transition criterion

---

## State Name Validation

### TradeConfirmation Workflow States
✅ **All state names comply with schema requirements**
- `received` - starts with letter, alphanumeric
- `validating` - starts with letter, alphanumeric
- `validated` - starts with letter, alphanumeric
- `validation-failed` - starts with letter, contains hyphen (allowed)
- `processing` - starts with letter, alphanumeric
- `processed` - starts with letter, alphanumeric
- `processing-failed` - starts with letter, contains hyphen (allowed)
- `manual-review` - starts with letter, contains hyphen (allowed)
- `reprocessing` - starts with letter, alphanumeric
- `rejected` - starts with letter, alphanumeric
- `archived` - starts with letter, alphanumeric

### Trade Workflow States
✅ **All state names comply with schema requirements**
- `new` - starts with letter, alphanumeric
- `validating` - starts with letter, alphanumeric
- `validated` - starts with letter, alphanumeric
- `validation-failed` - starts with letter, contains hyphen (allowed)
- `confirmed` - starts with letter, alphanumeric
- `active` - starts with letter, alphanumeric
- `amending` - starts with letter, alphanumeric
- `cancelling` - starts with letter, alphanumeric
- `maturing` - starts with letter, alphanumeric
- `amendment-failed` - starts with letter, contains hyphen (allowed)
- `cancelled` - starts with letter, alphanumeric
- `cancellation-failed` - starts with letter, contains hyphen (allowed)
- `matured` - starts with letter, alphanumeric
- `manual-review` - starts with letter, contains hyphen (allowed)
- `revalidating` - starts with letter, alphanumeric
- `rejected` - starts with letter, alphanumeric
- `archived` - starts with letter, alphanumeric

### Position Workflow States
✅ **All state names comply with schema requirements**
- `calculating` - starts with letter, alphanumeric
- `calculated` - starts with letter, alphanumeric
- `calculation-failed` - starts with letter, contains hyphen (allowed)
- `validating` - starts with letter, alphanumeric
- `validated` - starts with letter, alphanumeric
- `validation-failed` - starts with letter, contains hyphen (allowed)
- `reporting-ready` - starts with letter, contains hyphen (allowed)
- `reconciling` - starts with letter, alphanumeric
- `current` - starts with letter, alphanumeric
- `reporting` - starts with letter, alphanumeric
- `reported` - starts with letter, alphanumeric
- `reporting-failed` - starts with letter, contains hyphen (allowed)
- `reconciled` - starts with letter, alphanumeric
- `reconciliation-failed` - starts with letter, contains hyphen (allowed)
- `manual-review` - starts with letter, contains hyphen (allowed)
- `recalculating` - starts with letter, alphanumeric

### RegulatoryReport Workflow States
✅ **All state names comply with schema requirements**
- `generating` - starts with letter, alphanumeric
- `generated` - starts with letter, alphanumeric
- `generation-failed` - starts with letter, contains hyphen (allowed)
- `validating` - starts with letter, alphanumeric
- `validated` - starts with letter, alphanumeric
- `validation-failed` - starts with letter, contains hyphen (allowed)
- `ready-for-submission` - starts with letter, contains hyphens (allowed)
- `submitting` - starts with letter, alphanumeric
- `submitted` - starts with letter, alphanumeric
- `submission-failed` - starts with letter, contains hyphen (allowed)
- `acknowledged` - starts with letter, alphanumeric
- `rejected-by-dtcc` - starts with letter, contains hyphens (allowed)
- `completed` - starts with letter, alphanumeric
- `correcting` - starts with letter, alphanumeric
- `retrying` - starts with letter, alphanumeric
- `manual-review` - starts with letter, contains hyphen (allowed)
- `regenerating` - starts with letter, alphanumeric
- `archived` - starts with letter, alphanumeric

---

## Workflow Configuration Examples

### TradeConfirmation Workflow Configuration
```json
{
  "version": "1.0",
  "name": "TradeConfirmationWorkflow",
  "desc": "Process incoming FpML trade confirmation messages",
  "initialState": "received",
  "active": true,
  "states": {
    "received": {
      "transitions": [
        {
          "name": "startValidation",
          "next": "validating",
          "manual": false
        }
      ]
    },
    "validating": {
      "transitions": [
        {
          "name": "validationSuccess",
          "next": "validated",
          "manual": false,
          "criterion": {
            "type": "function",
            "function": "validateFpMLMessage"
          }
        },
        {
          "name": "validationFailure",
          "next": "validation-failed",
          "manual": false
        }
      ]
    },
    "validated": {
      "transitions": [
        {
          "name": "startProcessing",
          "next": "processing",
          "manual": false
        }
      ]
    },
    "processing": {
      "transitions": [
        {
          "name": "processingSuccess",
          "next": "processed",
          "manual": false,
          "processors": [
            {
              "name": "CreateTradeProcessor",
              "type": "function"
            }
          ]
        },
        {
          "name": "processingFailure",
          "next": "processing-failed",
          "manual": false
        }
      ]
    },
    "validation-failed": {
      "transitions": [
        {
          "name": "sendToManualReview",
          "next": "manual-review",
          "manual": true
        }
      ]
    },
    "processing-failed": {
      "transitions": [
        {
          "name": "sendToManualReview",
          "next": "manual-review",
          "manual": true
        }
      ]
    },
    "manual-review": {
      "transitions": [
        {
          "name": "reprocess",
          "next": "reprocessing",
          "manual": true
        },
        {
          "name": "reject",
          "next": "rejected",
          "manual": true
        }
      ]
    },
    "reprocessing": {
      "transitions": [
        {
          "name": "restartProcessing",
          "next": "processing",
          "manual": false
        }
      ]
    },
    "processed": {
      "transitions": [
        {
          "name": "archive",
          "next": "archived",
          "manual": false,
          "criterion": {
            "type": "function",
            "function": "checkRetentionPeriod"
          }
        }
      ]
    },
    "rejected": {
      "transitions": [
        {
          "name": "archive",
          "next": "archived",
          "manual": false,
          "criterion": {
            "type": "function",
            "function": "checkRetentionPeriod"
          }
        }
      ]
    },
    "archived": {
      "transitions": []
    }
  }
}
```

---

## Schema Compliance Summary

### ✅ Compliant Elements
1. **State Naming**: All state names follow the required pattern (start with letter, alphanumeric/underscore/hyphen only)
2. **Required Properties**: All workflows include version, name, initialState, and states
3. **Transition Structure**: All transitions include required "next" property
4. **Manual Flags**: Manual transitions are properly marked with "manual": true
5. **Terminal States**: Terminal states have empty transitions arrays

### ✅ Schema-Aligned Features
1. **Function Conditions**: Workflows use "function" type conditions for external processing
2. **Processor Integration**: Transitions include processor definitions for business logic
3. **Conditional Transitions**: Criterion-based transitions for business rule evaluation
4. **State Definitions**: Proper state definition structure with transitions arrays

### 📋 Implementation Considerations
1. **QueryCondition Types**: Workflows should use supported condition types: 'group', 'simple', and 'function'
2. **Processor Definitions**: ExternalizedProcessorDefinition schema compliance required
3. **Criterion Functions**: External function implementations needed for business logic
4. **Error Handling**: Consistent error state patterns across all workflows

### 🎯 Validation Results
**All designed workflows are fully compliant with the Cyoda WorkflowConfiguration.json schema version 1.0**
