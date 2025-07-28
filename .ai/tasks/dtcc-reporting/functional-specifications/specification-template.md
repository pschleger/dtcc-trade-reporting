# Component Specification Template

## Processor Specification Template

### 1. Component Overview
**Component Name**: [ProcessorName]
**Component Type**: CyodaProcessor
**Business Domain**: [Domain Category]
**Purpose**: [Brief description of the processor's business purpose]
**Workflow Context**: [Which workflows use this processor]

### 2. Input Specifications
**Entity Type**: [Primary entity type being processed]
**Required Fields**:
- `field1`: [Type] - [Description]
- `field2`: [Type] - [Description]

**Optional Fields**:
- `optionalField1`: [Type] - [Description]

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- [Description of additional context data required]

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "[EntityType]",
    "processingTimestamp": "ISO-8601 timestamp",
    "additionalData": {}
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "[ErrorCode]",
  "errorMessage": "[Description]",
  "details": {}
}
```

**Side Effects**:
- [Description of any state changes or external system interactions]

### 4. Business Logic
**Processing Steps**:
1. [Step 1 description]
2. [Step 2 description]
3. [Step 3 description]

**Business Rules**:
- [Rule 1]: [Description]
- [Rule 2]: [Description]

**Algorithms**:
- [Algorithm description if applicable]

### 5. Validation Rules
**Pre-processing Validations**:
- [Validation 1]: [Description and error condition]
- [Validation 2]: [Description and error condition]

**Post-processing Validations**:
- [Validation 1]: [Description and error condition]

**Data Quality Checks**:
- [Check 1]: [Description]

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input data validation failures
- **PROCESSING_ERROR**: Business logic processing failures
- **SYSTEM_ERROR**: Technical system failures
- **TIMEOUT_ERROR**: Processing timeout exceeded

**Error Recovery**:
- [Description of retry mechanisms]
- [Description of fallback procedures]

**Error Propagation**:
- [How errors are communicated to calling workflows]

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: [X] milliseconds (95th percentile)
- **Throughput**: [X] transactions per second
- **Availability**: [X]% uptime

**Resource Requirements**:
- **CPU**: [Description]
- **Memory**: [Description]
- **I/O**: [Description]

**Scalability**:
- [Horizontal scaling characteristics]
- [Performance degradation patterns]

### 8. Dependencies
**Internal Dependencies**:
- [Service/Component 1]: [Purpose]
- [Service/Component 2]: [Purpose]

**External Dependencies**:
- [External System 1]: [Purpose and SLA]
- [External System 2]: [Purpose and SLA]

**Data Dependencies**:
- [Required reference data]
- [Master data requirements]

### 9. Configuration Parameters
**Required Configuration**:
- `parameter1`: [Type] - [Description] - Default: [value]
- `parameter2`: [Type] - [Description] - Default: [value]

**Optional Configuration**:
- `optionalParam1`: [Type] - [Description] - Default: [value]

**Environment-Specific Configuration**:
- [Development environment settings]
- [Production environment settings]

### 10. Integration Points
**API Contracts**:
- [Input API specification]
- [Output API specification]

**Data Exchange Formats**:
- [Format 1]: [Usage]
- [Format 2]: [Usage]

**Event Publishing**:
- [Event 1]: [When published and payload]

**Event Consumption**:
- [Event 1]: [When consumed and handling]

---

## Criterion Specification Template

### 1. Component Overview
**Component Name**: [CriterionName]
**Component Type**: CyodaCriterion
**Business Domain**: [Domain Category]
**Purpose**: [Brief description of the criterion's evaluation purpose]
**Workflow Context**: [Which workflows use this criterion]

### 2. Input Parameters
**Entity Type**: [Primary entity type being evaluated]
**Required Fields**:
- `field1`: [Type] - [Description]
- `field2`: [Type] - [Description]

**Optional Fields**:
- `optionalField1`: [Type] - [Description]

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to evaluation context
- `calculationNodesTags`: string - Tags for distributed evaluation nodes
- `responseTimeoutMs`: integer - Maximum evaluation time in milliseconds
- `context`: string - Evaluation context identifier

**Evaluation Context**:
- [Description of additional context data for evaluation]

### 3. Evaluation Logic
**Decision Algorithm**:
```
IF [condition1] AND [condition2] THEN
    RETURN true
ELSE IF [condition3] THEN
    RETURN false
ELSE
    [default behavior]
```

**Boolean Logic**:
- [Description of the logical evaluation process]

**Calculation Methods**:
- [Any calculations required for evaluation]

### 4. Success Conditions
**Positive Evaluation Criteria**:
- [Condition 1]: [Description]
- [Condition 2]: [Description]

**Success Scenarios**:
- [Scenario 1]: [Description and expected result]
- [Scenario 2]: [Description and expected result]

### 5. Failure Conditions
**Negative Evaluation Criteria**:
- [Condition 1]: [Description]
- [Condition 2]: [Description]

**Failure Scenarios**:
- [Scenario 1]: [Description and expected result]
- [Scenario 2]: [Description and expected result]

### 6. Edge Cases
**Boundary Conditions**:
- [Edge case 1]: [Description and handling]
- [Edge case 2]: [Description and handling]

**Special Scenarios**:
- [Special case 1]: [Description and evaluation result]

**Data Absence Handling**:
- [How to handle missing required data]

### 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: [X] milliseconds (95th percentile)
- **Throughput**: [X] evaluations per second
- **Availability**: [X]% uptime

**Resource Requirements**:
- **CPU**: [Description]
- **Memory**: [Description]

### 8. Dependencies
**Internal Dependencies**:
- [Service/Component 1]: [Purpose]
- [Service/Component 2]: [Purpose]

**External Dependencies**:
- [External System 1]: [Purpose and SLA]

**Data Dependencies**:
- [Required reference data]
- [Master data requirements]

### 9. Configuration
**Configurable Thresholds**:
- `threshold1`: [Type] - [Description] - Default: [value]
- `threshold2`: [Type] - [Description] - Default: [value]

**Evaluation Parameters**:
- `parameter1`: [Type] - [Description] - Default: [value]

**Environment-Specific Settings**:
- [Development environment settings]
- [Production environment settings]

### 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- [Default evaluation result on error]
- [Retry mechanisms]

**Error Propagation**:
- [How evaluation errors are communicated]

---

## Usage Guidelines

### Template Customization
1. Replace all `[placeholder]` values with actual component-specific information
2. Remove sections that are not applicable to the specific component
3. Add additional sections as needed for complex components
4. Ensure all business rules and technical requirements are clearly documented

### Validation Checklist
- [ ] All placeholders replaced with actual values
- [ ] Business logic clearly documented
- [ ] Error scenarios comprehensively covered
- [ ] Performance requirements specified
- [ ] Dependencies identified and documented
- [ ] Configuration parameters defined
- [ ] Integration points specified

### Review Process
1. **Technical Review**: Verify technical accuracy and implementation feasibility
2. **Business Review**: Validate business logic and requirement alignment
3. **Architecture Review**: Ensure consistency with system architecture
4. **Performance Review**: Validate performance requirements and scalability
