---
layout: layout.njk
title: Home
---

# DTCC Trade Reporting Documentation

This documentation provides the full specifications, architecture, and implementation details of the system supporting OTC derivatives position keeping and regulatory reporting to the DTCC.

## Overview

This system is designed as an event-driven architecture that:

- Accepts FpML trade confirmation messages
- Builds and maintains trade positions
- Manages all GTR (Global Trade Repository) reporting requirements
- Includes feeds for capturing and holding relevant static data needed for reporting
- Handles counterparty static data management

## Documentation Structure

### [Background](/content/Background/)

The core background documents that define the system requirements and design:

- **[How We Got Here](/content/Background/how-we-got-here/)** - Project background and context
- **[Design Principles](/content/Background/cyoda-design-principles/)** - Cyoda design principles and guidelines
- **[Workflow Configuration Guide](/content/Background/workflow-config-guide/)** - Guide for configuring entity workflows

### [System Specification](/content/System-Specification/)

Detailed technical specifications including:

- **Architecture** - System architecture and design patterns
- **Business Logic** - Business rules and requirements
- **Entities** - Data models and entity definitions
- **Events** - Event specifications and handling
- **Functional Specifications** - Detailed functional requirements
- **Implementation Guide** - Implementation guidelines and best practices
- **Requirements** - System requirements and constraints
- **Standards** - Coding and design standards
- **Workflows** - Business process workflows

### [External Interfaces](/content/System-Specification/external-interfaces/)

Documentation for system integrations:

- External interface specifications
- Integration testing guides
- Interface interaction diagrams
- Security and compliance requirements

## Key Features

- **Event-Driven Architecture** - Built on a robust event-driven foundation
- **Regulatory Compliance** - Full DTCC GTR reporting compliance
- **FpML Support** - Native support for FpML trade confirmation messages
- **Position Management** - Complete trade position tracking and management
- **Static Data Management** - Integrated counterparty and reference data management

## Getting Started

1. Start with the [Background documentation](/content/Background/how-we-got-here/) for an overview
2. Review the [Design Principles](/content/Background/cyoda-design-principles/) to understand the architectural approach
3. Explore the [System Specification](/content/System-Specification/) for detailed technical information
4. Check the [Entity documentation](/content/System-Specification/entities/) for data structure definitions

## Business Logic Encapsulation

The entire system's business logic is encapsulated in:

- **Entities** - Core business objects and their properties
- **Workflow Configuration** - Entity lifecycle and state management
- **Functional Requirements** - Processor and criterion specifications for each workflow step

This approach ensures that business rules are clearly defined, maintainable, and traceable throughout the system implementation.
