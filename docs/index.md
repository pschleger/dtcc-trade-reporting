---
layout: layout.njk
title: Home
---

# DTCC Trade Reporting Documentation

Welcome to the comprehensive documentation for the DTCC Trade Reporting system. This documentation covers the complete specification, architecture, and implementation details for an OTC derivatives trade position keeping and DTCC regulatory reporting system.

## Overview

This system is designed as an event-driven architecture that:

- Accepts FpML trade confirmation messages
- Builds and maintains trade positions
- Manages all GTR (Global Trade Repository) reporting requirements
- Includes feeds for capturing and holding relevant static data needed for reporting
- Handles counterparty static data management

## Documentation Structure

### [Specification](/content/specification/)

The core specification documents that define the system requirements and design:

- **[DTCC Reporting](/content/specification/dtcc-reporting/)** - Task decomposition and system overview
- **[Design Principles](/content/specification/cyoda-design-principles/)** - Cyoda design principles and guidelines
- **[Workflow Configuration Guide](/content/specification/workflow-config-guide/)** - Guide for configuring entity workflows

### [System Specification](/content/specification/system-specification/)

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

### [External Interfaces](/content/specification/external-interfaces/)

Documentation for system integrations:

- External interface specifications
- Integration testing guides
- Interface interaction diagrams
- Security and compliance requirements

### [Schema](/content/specification/schema/)

JSON Schema definitions for:

- **Common** - Shared schema components
- **Entity** - Entity-related schemas
- **Model** - Data model schemas
- **Processing** - Processing-related schemas
- **Search** - Search functionality schemas

## Key Features

- **Event-Driven Architecture** - Built on a robust event-driven foundation
- **Regulatory Compliance** - Full DTCC GTR reporting compliance
- **FpML Support** - Native support for FpML trade confirmation messages
- **Position Management** - Comprehensive trade position tracking and management
- **Static Data Management** - Integrated counterparty and reference data management

## Getting Started

1. Start with the [DTCC Reporting specification](/content/specification/dtcc-reporting/) for an overview
2. Review the [Design Principles](/content/specification/cyoda-design-principles/) to understand the architectural approach
3. Explore the [System Specification](/content/specification/system-specification/) for detailed technical information
4. Check the [Schema documentation](/content/specification/schema/) for data structure definitions

## Business Logic Encapsulation

The entire system's business logic is encapsulated in:

- **Entities** - Core business objects and their properties
- **Workflow Configuration** - Entity lifecycle and state management
- **Functional Requirements** - Processor and criterion specifications for each workflow step

This approach ensures that business rules are clearly defined, maintainable, and traceable throughout the system implementation.
