# Cyoda Workflow Configuration Guide

> Understanding Cyoda JSON workflow configurations.

## Overview

Cyoda workflows define finite, distributed state machines that govern the lifecycle of business entities in an event-driven environment. Each entity progresses through a sequence of states based on defined transitions, criteria, and processing rules.

The platform supports adaptable entity modeling, allowing business logic to evolve through configuration rather than implementation changes. Workflows declare the set of states, valid transitions, and associated processing steps while preserving immutable persistence for auditability.

## Workflow Architecture

### Core Components

1. **States**: Lifecycle stages of an entity
2. **Transitions**: Directed changes between states
3. **Criteria**: Conditional logic for transition eligibility
4. **Processors**: Executable logic triggered during transitions

## Configuration Schema


You can find the workflow schema in the schema directory. Here we explain the structure and meaning of each element.

### Workflow Object

```json
{
  "version": "1.0",
  "name": "Workflow Name",
  "desc": "Workflow description",
  "initialState": "StateName",
  "active": true,
  "criterion": {},
  "states": {}
}
```

#### Attributes

- `version`: Workflow schema version
- `name`: Identifier for the workflow
- `desc`: Detailed description of the workflow
- `initialState`: Starting point for new entities
- `active`: Indicates whether the workflow is active
- `criterion`: Optional criterion for choosing the workflow for an entity
- `states`: Map of states with transition definitions

## States

States describe lifecycle phases for entities. Names must start with a letter and use only alphanumeric characters, underscores, or hyphens.

### Format

```json
"StateName": {
  "transitions": []
}
```

#### Special States

- **Initial state**: The initial state of a new entity
- **Terminal States**: States with no outgoing transitions

## Transitions

Transitions define allowed movements between states, optionally gated by conditions and supported by executable logic.

### Format

```json
{
  "name": "TransitionName",
  "next": "TargetState",
  "manual": true,
  "disabled": false,
  "criterion": {},
  "processors": []
}
```

#### Attributes

- `manual`: deterimines if the transition is manual or automated
- `disabled`: Marks the transition as inactive
- `criterion`: Optional condition for eligibility
- `processors`: Optional processing steps

### Manual vs Automated Transitions

Transitions may be either **manual** or **automated**, and are guarded by criteria that determine their eligibility. When an entity enters a new state, the first eligible automated transition is executed immediately within the same transaction. This continues recursively until no further **automated** transitions are applicable, resulting in a stable state. Each transition may trigger one or more attached processes, which can run synchronously or asynchronously, either within the current transaction or in a separate one. This forms the foundation for event flow automation, where processors may create or mutate entities in response, allowing a single transition to initiate a cascade of events and function executions across the system.

## Criteria

Criteria define logic that determines if a transition is permitted. A criteria can be of three different types:

1. **Function**: Calls a custom function for evaluation
2. **Simple**: Evaluates a single condition on entity data
3. **Group**: Combines multiple criteria with logical AND/OR operators

### Function-Based Example

```json
"criterion": {
  "type": "function",
  "function": {
    "name": "FunctionName",
    "config": {
      "attachEntity": true,
      "context": "optionalContext"
    }
  }
}
```

#### Function Configuration

- `name`: The name of the function to execute
- `attachEntity`: Whether to pass the entity data to the function
- `context`: Optional string parameter passed to the function for additional context or configuration.

The `context` is passed "as is" with the event to the compute node. It can contain any sort of information that is relevant to the function's execution, in any format. The interpretation is up to the function itself.

### Simple Criteria Example

Simple criteria evaluate a single condition directly on entity data using JSONPath expressions.

```json
"criterion": {
  "type": "simple",
  "jsonPath": "$.amount",
  "operatorType": "GREATER_THAN",
  "value": 1000
}
```

See the [API Documentation](#api) for the operator types.

#### Simple Criteria Attributes

- `jsonPath`: JSONPath expression to extract the value from entity data
- `operatorType`: Comparison operator (`EQUALS`, `GREATER_THAN`, `LESS_THAN`, `CONTAINS`, etc.)
- `value`: The value to compare against

Simple Criteria are executed directly on the processing node, without involving external compute nodes.

### Group Criteria Example

Group criteria combine multiple conditions using logical operators.

```json
"criterion": {
  "type": "group",
  "operator": "AND",
  "conditions": [
    {
      "type": "simple",
      "jsonPath": "$.status",
      "operatorType": "EQUALS",
      "value": "VALIDATED"
    },
    {
      "type": "simple",
      "jsonPath": "$.amount",
      "operatorType": "GREATER_THAN",
      "value": 500
    }
  ]
}
```

#### Group Criteria Attributes

- `operator`: Logical operator combining conditions (`AND`, `OR`)
- `conditions`: Array of criteria (can be `simple`, `function`, or nested `group` types)

## Processors

Processors implement custom logic to run during transitions.

### Format

```json
{
  "name": "ProcessorName",
  "executionMode": "SYNC",
  "config": {
    "attachEntity": true,
    "calculationNodesTags": "tag1,tag2"
  }
}
```

#### Execution Modes

- `SYNC`: Inline execution within the transaction
- `ASYNC_NEW_TX`: Deferred execution in a separate transaction
- `ASYNC_SAME_TX`: Deferred within the current transaction

Synchronous executions run immediately and block the current processing thread on the same node, making them local and non-distributed. In contrast, asynchronous executions are scheduled for deferred processing and can be handled by any node in the cluster, enabling horizontal scalability and workload distribution, albeit with possibly somewhat higher latency.


#### Attaching Entities
If the processor requires access to the entity data, set `attachEntity` to `true`. This is usually the case.

### Calculation Nodes Tags

As described in the [Architecture](cyoda-cloud-architecture.md) section, the execution of processors and criteria is delegated to client compute nodes, i.e. your own infrastructure running your business logic. These nodes can be organized into groups and tagged based on their roles or capabilities. By optionally setting the `calculationNodesTags` property in a processor or criterion definition, you can direct execution to specific groups, giving you fine-grained control over workload distribution across your compute environment.

## Example: Payment Request Workflow

This workflow models the lifecycle of a payment request, covering validation, matching, approval, and notification handling.

It starts in the INVALID state, where the request is either amended or validated. 
If validation succeeds and a matching order exists, the request advances automatically to the SUBMITTED state. 
If not, it moves to PENDING, where it awaits a matching order or may be retried manually. 
Requests in SUBMITTED require an approval decision, leading either to APPROVED, which triggers
asynchronous processing like payment message creation and ACK notifications, or to DECLINED, 
which emits a rejection (NACK) notification. Manual amend and retry transitions at key 
stages allow users or systems to correct or re-evaluate the request.

The following section walks through the configuration step by step.

![Payment Request Workflow](./images/payment-request-workflow.png)

### Step 1: Workflow Metadata

```json
{
  "version": "1.0",
  "name": "Payment Request Workflow",
  "desc": "Payment request processing workflow with validation, approval, and notification states",
  "initialState": "INVALID",
  "active": true
}
```

### Step 2: Define States and Transitions

Start by defining the overall structure of states and transitions.

```json
{
  "version": "1.0",
  "name": "Payment Request Workflow",
  "desc": "Payment request processing workflow with validation, approval, and notification states",
  "initialState": "INVALID",
  "active": true,
  "states": {
    "INVALID": {
      "transitions": [
        {
          "name": "VALIDATE",
          "next": "PENDING",
          "manual": false,
          "disabled": false
        },
        {
          "name": "AMEND",
          "next": "INVALID",
          "manual": true,
          "disabled": false
        },
        {
          "name": "CANCEL",
          "next": "CANCELED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "PENDING": {
      "transitions": [
        {
          "name": "MATCH",
          "next": "SUBMITTED",
          "manual": false,
          "disabled": false
        },
        {
          "name": "RETRY",
          "next": "PENDING",
          "manual": true,
          "disabled": false
        },
        {
          "name": "CANCEL",
          "next": "CANCELED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "SUBMITTED": {
      "transitions": [
        {
          "name": "APPROVE",
          "next": "APPROVED",
          "manual": true,
          "disabled": false
        },
        {
          "name": "DENY",
          "next": "DECLINED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "APPROVED": {
      "transitions": []
    },
    "DECLINED": {
      "transitions": []
    },
    "CANCELED": {
      "transitions": []
    }
  }
}
```

### Step 3: Add Criteria

We add criteria to the `VALIDATE` and `MATCH` transitions:

```json
{
  "version": "1.0",
  "name": "Payment Request Workflow",
  "desc": "Payment request processing workflow with validation, approval, and notification states",
  "initialState": "INVALID",
  "active": true,
  "states": {
    "INVALID": {
      "transitions": [
        {
          "name": "VALIDATE",
          "next": "PENDING",
          "manual": false,
          "disabled": false,
          "criterion": {
            "type": "function",
            "function": {
              "name": "IsValid",
              "config": {
                "attachEntity": true
              }
            }
          }
        },
        {
          "name": "AMEND",
          "next": "INVALID",
          "manual": true,
          "disabled": false
        },
        {
          "name": "CANCEL",
          "next": "CANCELED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "PENDING": {
      "transitions": [
        {
          "name": "MATCH",
          "next": "SUBMITTED",
          "manual": false,
          "disabled": false,
          "criterion": {
            "type": "function",
            "function": {
              "name": "HasOrder",
              "config": {
                "attachEntity": true
              }
            }
          }
        },
        {
          "name": "RETRY",
          "next": "PENDING",
          "manual": true,
          "disabled": false
        },
        {
          "name": "CANCEL",
          "next": "CANCELED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "SUBMITTED": {
      "transitions": [
        {
          "name": "APPROVE",
          "next": "APPROVED",
          "manual": true,
          "disabled": false
        },
        {
          "name": "DENY",
          "next": "DECLINED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "APPROVED": {
      "transitions": []
    },
    "DECLINED": {
      "transitions": []
    },
    "CANCELED": {
      "transitions": []
    }
  }
}
```

### Step 4: Add Processors

We add two processors to the `APPROVE` transition in the `SUBMITTED` state, respectively, to finish the job.

```json
{
  "version": "1.0",
  "name": "Payment Request Workflow",
  "desc": "Payment request processing workflow with validation, approval, and notification states",
  "initialState": "INVALID",
  "active": true,
  "states": {
    "INVALID": {
      "transitions": [
        {
          "name": "VALIDATE",
          "next": "PENDING",
          "manual": false,
          "disabled": false,
          "criterion": {
            "type": "function",
            "function": {
              "name": "IsValid",
              "config": {
                "attachEntity": true
              }
            }
          }
        },
        {
          "name": "AMEND",
          "next": "INVALID",
          "manual": true,
          "disabled": false
        },
        {
          "name": "CANCEL",
          "next": "CANCELED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "PENDING": {
      "transitions": [
        {
          "name": "MATCH",
          "next": "SUBMITTED",
          "manual": false,
          "disabled": false,
          "criterion": {
            "type": "function",
            "function": {
              "name": "HasOrder",
              "config": {
                "attachEntity": true
              }
            }
          }
        },
        {
          "name": "RETRY",
          "next": "PENDING",
          "manual": true,
          "disabled": false
        },
        {
          "name": "CANCEL",
          "next": "CANCELED",
          "manual": true,
          "disabled": false
        }
      ]
    },
    "SUBMITTED": {
      "transitions": [
        {
          "name": "APPROVE",
          "next": "APPROVED",
          "manual": true,
          "disabled": false,
          "processors": [
            {
              "name": "Create Payment Message",
              "executionMode": "ASYNC_NEW_TX",
              "config": { "attachEntity": true }
            },
            {
              "name": "Send ACK Notification",
              "executionMode": "ASYNC_NEW_TX",
              "config": { "attachEntity": false }
            }
          ]
        },
        {
          "name": "DENY",
          "next": "DECLINED",
          "manual": true,
          "disabled": false,
          "processors": [
            {
              "name": "Send NACK Notification",
              "executionMode": "ASYNC_NEW_TX",
              "config": { "attachEntity": false }
            }
          ]
        }
      ]
    },
    "APPROVED": {
      "transitions": []
    },
    "DECLINED": {
      "transitions": []
    },
    "CANCELED": {
      "transitions": []
    }
  }
}
```

## Best Practices

- Use domain-specific state names
- Match transition granularity to business needs
- Define recovery and cancellation paths
- Prefer asynchronous processing for external dependencies
- Use self-transitions for triggering workflow automation on exit from the current state


## Platform Integration

Cyoda workflows integrate directly with:

- **Entity Models**: Determine which workflows apply to which data types
- **Execution Engine**: Drives state and transition logic
- **External Functions**: Implement validation and custom behavior
- **Event System**: Triggers automated transitions on event reception

