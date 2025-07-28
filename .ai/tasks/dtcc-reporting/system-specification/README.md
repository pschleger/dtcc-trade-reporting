# DTCC Regulatory Reporting System Specification

This directory contains the complete system specification for the DTCC Regulatory Reporting System, organized by topic area for better navigation and maintenance.

## Directory Structure

### 📋 Root Level
- **system-description.md** - High-level system overview and business context

### 🏗️ architecture/
System architecture and design documentation
- **system-architecture.md** - Comprehensive system architecture document
- **system-architecture-diagram.mmd** - Mermaid system architecture diagram
- **entity-driven-architecture.md** - Cyoda EDBMS entity-driven architecture principles

### 🏢 entities/
Entity definitions, relationships, and data models
- **entity-overview.md** - Complete entity landscape and categorization
- **entity-relationship-diagram.mmd** - Mermaid entity relationship diagram

### 📊 business/
Business rules, use cases, and domain logic
- **business-use-cases.md** - Detailed business use case specifications
- **decision-points-business-rules.md** - Business decision points and rules
- **general-rules.md** - General business rules and constraints
- **use-case-relationship-hierarchy.md** - Use case relationships and hierarchy

### ⚡ events/
Event-driven architecture and processing flows
- **event-driven-architecture.md** - Event-driven architecture principles and patterns
- **event-flows-position-reporting.md** - Position reporting event flows
- **event-flows-trade-processing.md** - Trade processing event flows

### 📏 standards/
Naming conventions and design standards
- **naming-conventions.md** - Entity naming and design principles

### ⏱️ requirements/
System requirements and service level agreements
- **timing-requirements-slas.md** - Performance requirements and SLAs

## Document Relationships

### Core Foundation Documents
1. **system-description.md** - Start here for system overview
2. **architecture/entity-driven-architecture.md** - Cyoda EDBMS architecture principles
3. **architecture/system-architecture.md** - System design and components
4. **entities/entity-overview.md** - Business entity landscape

### Design and Implementation Guides
- **standards/naming-conventions.md** - Follow for consistent naming
- **events/event-driven-architecture.md** - Event processing patterns
- **business/general-rules.md** - Business constraints and rules

### Detailed Specifications
- **business/** - Business logic and use cases
- **events/** - Event flows and processing details
- **requirements/** - Performance and timing requirements

## Navigation Guide

### For System Architects
1. Start with `system-description.md`
2. Review `architecture/entity-driven-architecture.md`
3. Study `architecture/system-architecture.md`
4. Examine `entities/entity-overview.md`
5. Review `events/event-driven-architecture.md`

### For Business Analysts
1. Begin with `system-description.md`
2. Review `business/business-use-cases.md`
3. Study `business/decision-points-business-rules.md`
4. Examine `entities/entity-overview.md`

### For Developers
1. Start with `standards/naming-conventions.md`
2. Review `entities/entity-overview.md`
3. Study `events/event-driven-architecture.md`
4. Examine specific event flows in `events/`

### For Operations Teams
1. Begin with `system-description.md`
2. Review `requirements/timing-requirements-slas.md`
3. Study `architecture/system-architecture.md`
4. Examine monitoring aspects in architecture docs

## Document Status

| Document | Status | Last Updated |
|----------|--------|--------------|
| system-description.md | ✅ Complete | Updated 2024-11-27 |
| architecture/entity-driven-architecture.md | ✅ Complete | New 2024-11-27 |
| architecture/system-architecture.md | ✅ Complete | Updated 2024-11-27 |
| entities/entity-overview.md | ✅ Complete | 2024-11-27 |
| events/event-driven-architecture.md | ✅ Complete | 2024-11-27 |
| standards/naming-conventions.md | ✅ Complete | 2024-11-27 |
| business/business-use-cases.md | ✅ Complete | Previous |
| business/decision-points-business-rules.md | ✅ Complete | Previous |
| business/general-rules.md | ✅ Complete | Previous |
| business/use-case-relationship-hierarchy.md | ✅ Complete | Previous |
| events/event-flows-position-reporting.md | ✅ Complete | Previous |
| events/event-flows-trade-processing.md | ✅ Complete | Previous |
| requirements/timing-requirements-slas.md | ✅ Complete | Previous |

## Diagram Files

The specification includes Mermaid diagram files (.mmd) that can be rendered using:
- Mermaid CLI tools
- GitHub/GitLab native rendering
- Mermaid Live Editor (https://mermaid.live)
- IDE plugins (VS Code, IntelliJ)

### Available Diagrams
- **architecture/system-architecture-diagram.mmd** - System component architecture
- **entities/entity-relationship-diagram.mmd** - Entity relationships and data model

## Maintenance Guidelines

### Adding New Documents
- Place documents in the appropriate topic directory
- Update this README with document descriptions
- Follow naming conventions from `standards/naming-conventions.md`
- Update the Document Status table

### Updating Existing Documents
- Maintain backward compatibility where possible
- Update the "Last Updated" date in the status table
- Consider impact on related documents
- Update cross-references as needed

### Directory Guidelines
- Keep directories focused on single topic areas
- Avoid deep nesting (max 2 levels recommended)
- Use descriptive directory names
- Maintain consistent naming patterns

## Related Documentation

### External References
- **[Cyoda Design Principles](../cyoda-design-principles.md)** - Platform architectural patterns
- **[Workflow Configuration Guide](../workflow-config-guide.md)** - Workflow JSON schema specification

### Schema Definitions
- **[Schema Directory](../schema/)** - JSON schema definitions for entities and workflows
