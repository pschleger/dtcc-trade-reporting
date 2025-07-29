# Schema Versioning Strategy

## Overview

This document defines the versioning strategy for business entity schemas in the DTCC Regulatory Reporting System. The strategy ensures backward compatibility, supports schema evolution, and provides clear migration paths for system upgrades.

## Versioning Principles

### 1. Semantic Versioning
Schema versions follow semantic versioning (MAJOR.MINOR.PATCH):
- **MAJOR**: Breaking changes that require data migration
- **MINOR**: Backward-compatible additions (new optional fields)
- **PATCH**: Bug fixes and clarifications without structural changes

### 2. Backward Compatibility
- New versions must be backward compatible within the same major version
- Optional fields can be added without version increment
- Required fields additions require minor version increment
- Field removal or type changes require major version increment

### 3. Schema Evolution
- Schemas evolve to meet changing business and regulatory requirements
- Evolution is planned and coordinated across all affected systems
- Migration scripts are provided for major version changes
- Deprecation notices precede breaking changes by at least one minor version

## Version Identification

### Schema File Naming
```
EntityName.json                    # Current version (latest)
EntityName-v1.0.json              # Specific version
EntityName-v1.1.json              # Minor update
EntityName-v2.0.json              # Major update
```

### Schema Metadata
Each schema includes version information:
```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://cyoda.com/cloud/event/business/EntityName.json",
  "title": "EntityName",
  "version": "1.2.0",
  "lastModified": "2024-01-15",
  "description": "Entity description with version notes"
}
```

### Entity Version Tracking
Business entities track schema version:
```json
{
  "metadata": {
    "schemaVersion": "1.2.0",
    "migrationHistory": [
      {
        "fromVersion": "1.1.0",
        "toVersion": "1.2.0",
        "migrationDate": "2024-01-15T10:00:00Z",
        "migrationScript": "migrate_1_1_to_1_2.sql"
      }
    ]
  }
}
```

## Change Categories

### 1. Non-Breaking Changes (Patch/Minor)
**Allowed without version increment:**
- Adding optional fields
- Expanding enum values
- Adding new validation patterns (less restrictive)
- Documentation updates
- Example additions

**Minor version increment required:**
- Adding new required fields with default values
- Adding new entity types
- Adding new workflow states
- Expanding field constraints (more permissive)

### 2. Breaking Changes (Major)
**Require major version increment:**
- Removing fields
- Changing field types
- Making optional fields required
- Removing enum values
- Changing validation patterns (more restrictive)
- Renaming fields
- Restructuring nested objects

### 3. Deprecation Process
**Phase 1: Deprecation Notice (Minor Version)**
- Mark fields as deprecated in schema
- Add deprecation warnings in documentation
- Provide migration guidance
- Continue supporting deprecated fields

**Phase 2: Removal (Major Version)**
- Remove deprecated fields
- Update validation rules
- Provide migration scripts
- Update documentation

## Migration Strategies

### 1. Automatic Migration
For simple changes that can be automated:
```sql
-- Example: Adding default value for new required field
UPDATE entities 
SET new_field = 'DEFAULT_VALUE' 
WHERE new_field IS NULL;
```

### 2. Data Transformation
For complex structural changes:
```javascript
// Example: Restructuring nested object
function migrateEntity(entity) {
  if (entity.schemaVersion === '1.1.0') {
    // Transform old structure to new structure
    entity.newStructure = {
      field1: entity.oldField1,
      field2: entity.oldField2
    };
    delete entity.oldField1;
    delete entity.oldField2;
    entity.schemaVersion = '1.2.0';
  }
  return entity;
}
```

### 3. Manual Migration
For business-critical changes requiring review:
- Export affected entities
- Apply business rules and validations
- Review and approve changes
- Import updated entities
- Validate migration success

## Version Compatibility Matrix

### Current Supported Versions
| Entity Type | Current Version | Supported Versions | Deprecated Versions |
|-------------|----------------|-------------------|-------------------|
| Counterparty | 1.0.0 | 1.0.0 | None |
| Product | 1.0.0 | 1.0.0 | None |
| Trade | 1.0.0 | 1.0.0 | None |
| Position | 1.0.0 | 1.0.0 | None |
| RegulatoryReport | 1.0.0 | 1.0.0 | None |

### API Compatibility
- REST APIs support multiple schema versions via Accept headers
- GraphQL APIs use schema stitching for version compatibility
- Event streams include schema version in message metadata
- Database views provide version-specific data access

## Schema Registry

### Central Repository
- All schema versions stored in central registry
- Version history and change logs maintained
- Automated validation of schema changes
- Impact analysis for breaking changes

### Access Control
- Schema modifications require approval workflow
- Version releases follow change management process
- Production deployments use approved versions only
- Development environments can use preview versions

### Documentation
- Each version includes comprehensive documentation
- Migration guides provided for major versions
- Breaking change notifications sent to stakeholders
- API documentation updated with version information

## Testing Strategy

### Schema Validation Testing
- Automated tests for each schema version
- Backward compatibility validation
- Forward compatibility testing where possible
- Cross-version data migration testing

### Integration Testing
- End-to-end testing with multiple schema versions
- API compatibility testing
- Event processing with mixed versions
- Database migration testing

### Performance Testing
- Migration performance benchmarking
- Version-specific query performance
- Memory usage with multiple versions
- Concurrent access testing

## Deployment Strategy

### Blue-Green Deployment
- Deploy new schema version to green environment
- Validate with subset of production data
- Switch traffic gradually
- Rollback capability maintained

### Rolling Updates
- Update schema version incrementally
- Monitor system health during rollout
- Pause deployment if issues detected
- Complete rollout after validation

### Canary Releases
- Deploy to small percentage of traffic
- Monitor key metrics and error rates
- Expand deployment based on success criteria
- Full rollout after validation period

## Monitoring and Alerting

### Version Usage Metrics
- Track schema version distribution
- Monitor migration progress
- Alert on deprecated version usage
- Report on version adoption rates

### Error Monitoring
- Schema validation failures by version
- Migration errors and rollbacks
- Performance degradation alerts
- Compatibility issue notifications

### Compliance Reporting
- Schema version compliance status
- Migration completion reports
- Audit trail for schema changes
- Regulatory impact assessments

## Governance Process

### Schema Change Committee
- Representatives from development, operations, and business
- Review and approve schema changes
- Assess impact on existing systems
- Coordinate migration timelines

### Change Request Process
1. Submit schema change request with justification
2. Impact analysis and compatibility assessment
3. Committee review and approval/rejection
4. Implementation planning and scheduling
5. Testing and validation
6. Production deployment
7. Post-deployment monitoring

### Documentation Requirements
- Change rationale and business justification
- Technical impact analysis
- Migration plan and timeline
- Testing strategy and results
- Rollback procedures
- Communication plan

## Best Practices

### Schema Design
- Design for extensibility from the start
- Use optional fields for new features
- Avoid deeply nested structures
- Provide clear field descriptions
- Include validation examples

### Version Management
- Plan version releases in advance
- Coordinate with dependent systems
- Maintain multiple versions during transition
- Provide clear deprecation timelines
- Document all changes thoroughly

### Migration Planning
- Test migrations in non-production environments
- Provide rollback procedures
- Monitor system performance during migration
- Communicate changes to all stakeholders
- Validate data integrity after migration

### Communication
- Announce schema changes well in advance
- Provide migration guides and tools
- Offer training for new schema features
- Maintain FAQ for common issues
- Establish support channels for migration help

## Tools and Automation

### Schema Management Tools
- JSON Schema validators
- Version comparison utilities
- Migration script generators
- Compatibility checkers
- Documentation generators

### CI/CD Integration
- Automated schema validation in build pipeline
- Version compatibility checks
- Migration testing automation
- Deployment approval workflows
- Rollback automation

### Monitoring Tools
- Schema version dashboards
- Migration progress tracking
- Error rate monitoring
- Performance impact analysis
- Compliance reporting

This versioning strategy ensures that schema evolution is managed systematically while maintaining system stability and data integrity.
