# Cyoda Application Architecture

At the heart of Cyoda lies the Entity Database Management System (EDBMS), a system built around the idea that every piece of persisted data is an **Entity**. These entities function as finite state machines governed by workflows, forming the foundation of every Cyoda application.

## TL;DR

> [!INFO] Core Principles of the Cyoda EDBMS
> - All persisted data is an **_Entity_**.
> - Each _Entity_ follows a known, tree-structured data model.
> - _Entities_ encapsulate structured information and a **_State_**.
> - The _Entity_ **Lifecycle** is defined by a **_Workflow_**.
> - Mutations occur via explicit state **_Transitions_**.
> - _Transitions_ are guarded by **_Criteria_**, and may be **automated** or **manual**.
> - Upon entering a _State_, the first valid automated _Transition_ is executed immediately within the same **_Transaction_**.
> - This **recurses** until no further automated transitions apply, reaching a **stable state**.
> - Each _Transition_ can invoke attached **_Processes_**, executed when moving to the next state.
> - _Processes_ may be **synchronous** or **asynchronous**.
> - _Processes_ may run within the **same** or a **separate** _Transaction_.

## What Is an Entity?

An **Entity** in Cyoda represents a real-world object with state that evolves over time—a customer, an order, a message. It is the central unit of modeling, interaction, and persistence.

**Core properties of entities:**
- **Persistent**: Entities have lifecycles beyond transient operations.
- **Stateful**: They progress through well-defined states.
- **Contextual**: Their current state encodes meaningful operational context.

Cyoda frames every challenge as an entity-centric problem. The system's responsibility is to create, observe, evolve, and act upon entities.

## Entities: The Backbone of Cyoda

Cyoda is more than a data platform. It is a **processing platform** that unifies:

- **Data**: structured facts
- **State**: current position in a lifecycle
- **Behavior**: rules and transformations that govern change

This unification offers:
- **Decoupling**: Business logic remains local to the entity
- **Composability**: Entities reference and react to one another
- **Auditability**: Every state change is logged and inspectable

## What Is a Workflow?

A **Workflow** models the behavior of an entity as a **finite-state machine (FSM)**. It specifies:

- **States**: possible conditions of the entity
- **Transitions**: permitted changes between states
- **Triggers and Processes**: events and logic tied to transitions

Workflows encode business logic in a visual, structured, and deterministic way.

## Event-Driven Architecture in Cyoda

Cyoda applications respond to events:
- **Events** (e.g., "file uploaded") initiate transitions
- **Transitions** invoke **Processes** (e.g., validation, notifications)
- Everything is orchestrated via the entity's **Workflow**

**Key benefits:**
- **Loose coupling** between components
- **Responsiveness** to business stimuli
- **Horizontal scalability**

## The Cyoda Approach

To build with Cyoda:
- Model the domain using **Entities**
- Capture behavior in **Workflows**
- Use **Events** to trigger change
- Persist everything—data, state, transitions—in the **Entity Database**

## Guiding Practices

- Think in terms of **stateful entities**, not just data records
- Favor **FSM workflows** over imperative logic
- Use **events** to trigger logic and state transitions
- Keep business **rules close to the entity**
- **Visualize workflows** to align teams and stakeholders

## Further Reading

- https://github.com/Cyoda-platform/cyoda-docs/tree/main/dist/resources
- [Entity Workflows for Event-Driven Architectures](https://medium.com/@paul_42036/entity-workflows-for-event-driven-architectures-4d491cf898a5)
- [What’s an Entity Database?](https://medium.com/@paul_42036/whats-an-entity-database-11f8538b631a)
