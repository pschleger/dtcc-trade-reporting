# updateCounterparty Processor Specification

### 1. Component Overview
**Component Name**: updateCounterparty
**Component Type**: CyodaProcessor
**Business Domain**: Counterparty Management
**Purpose**: Updates counterparty information with validation, approval workflow, and regulatory compliance checks
**Workflow Context**: Used in counterparty maintenance workflows for data updates and corrections

### 2. Input Specifications
**Entity Type**: CounterpartyUpdateRequest
**Required Fields**:
- `counterpartyId`: string - Unique identifier of the counterparty to update
- `updateFields`: object - Fields to be updated with new values
- `updateReason`: string - Business justification for the update
- `requestingUser`: string - User requesting the update
- `requestTimestamp`: ISO-8601 timestamp - When update was requested

**Optional Fields**:
- `approvalRequired`: boolean - Whether update requires approval workflow
- `effectiveDate`: ISO-8601 timestamp - When update should take effect
- `supportingDocuments`: array - Supporting documentation for update

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 20000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Current counterparty data for comparison
- Approval workflow configuration and rules
- Regulatory validation requirements and LEI verification

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "UpdatedCounterparty",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "updateId": "UPD-CP-20240115-001",
      "fieldsUpdated": ["legalName", "address", "contactInfo"],
      "approvalStatus": "APPROVED",
      "effectiveTimestamp": "2024-01-15T10:30:00Z",
      "auditTrailId": "AUDIT-CP-789012"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "UPDATE_VALIDATION_FAILED",
  "errorMessage": "Counterparty update failed validation due to invalid LEI format",
  "details": {
    "validationErrors": ["LEI format invalid", "Address incomplete"],
    "fieldErrors": {
      "lei": "Must be 20 character alphanumeric",
      "address.postalCode": "Required field missing"
    }
  }
}
```

**Side Effects**:
- Updates counterparty master data in repository
- Creates audit trail entry for the update
- Triggers approval workflow if required
- Notifies downstream systems of counterparty changes

### 4. Business Logic
**Processing Steps**:
1. Validate update request format and required fields
2. Retrieve current counterparty data for comparison
3. Perform field-level validation for updated values
4. Check if update requires approval based on business rules
5. Execute LEI validation for legal entity identifier changes
6. Apply update to counterparty master data
7. Create comprehensive audit trail entry
8. Notify relevant systems and users of the update

**Business Rules**:
- **LEI Validation**: Legal Entity Identifier must be valid and verified
- **Approval Requirements**: Material changes require approval workflow
- **Data Consistency**: Updates must maintain referential integrity
- **Regulatory Compliance**: Changes must comply with KYC/AML requirements
- **Effective Dating**: Updates can be scheduled for future effective dates

**Algorithms**:
- LEI format validation using ISO 17442 standard
- Field-level change detection and impact assessment
- Approval requirement determination based on change materiality

### 5. Validation Rules
**Pre-processing Validations**:
- **Counterparty Existence**: Target counterparty must exist and be active
- **Update Authorization**: Requesting user must have update permissions
- **Field Validation**: All updated fields must pass format and business rule validation
- **Change Detection**: At least one field must be different from current values

**Post-processing Validations**:
- **Data Integrity**: Updated data must maintain consistency across all fields
- **Reference Validation**: Foreign key relationships must remain valid
- **Regulatory Compliance**: Updated data must satisfy regulatory requirements

**Data Quality Checks**:
- LEI validation against GLEIF database
- Address validation and standardization
- Contact information format verification

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Field validation failures or business rule violations
- **PROCESSING_ERROR**: Update execution or approval workflow failures
- **SYSTEM_ERROR**: Database connectivity or external service issues
- **TIMEOUT_ERROR**: Update processing exceeded timeout limits

**Error Recovery**:
- Retry update operation up to 3 times for transient failures
- Rollback partial updates to maintain data consistency
- Escalate to manual review for complex validation failures

**Error Propagation**:
- Return detailed validation errors to requesting user
- Log all errors with correlation IDs for audit purposes
- Generate alerts for system administrators on persistent failures

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 3000 milliseconds (95th percentile)
- **Throughput**: 200 updates per minute
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity for validation and data processing
- **Memory**: 256MB per concurrent update operation
- **I/O**: Medium for database reads and writes

**Scalability**:
- Horizontal scaling through distributed processing nodes
- Performance scales linearly with number of fields updated

### 8. Dependencies
**Internal Dependencies**:
- **Counterparty Repository**: Master counterparty data storage and retrieval
- **Approval Workflow Service**: Approval process management
- **Audit Trail Service**: Change tracking and compliance logging

**External Dependencies**:
- **GLEIF LEI Database**: Legal Entity Identifier validation (SLA: 99.0%)
- **Address Validation Service**: Address standardization and verification (SLA: 99.5%)

**Data Dependencies**:
- Counterparty master data with current values
- User permission and authorization data
- Approval workflow configuration and rules

### 9. Configuration Parameters
**Required Configuration**:
- `approvalThresholdFields`: array - Fields requiring approval - Default: ["lei", "legalName"]
- `leiValidationEnabled`: boolean - Enable LEI validation - Default: true
- `auditTrailEnabled`: boolean - Enable audit trail creation - Default: true

**Optional Configuration**:
- `addressValidationEnabled`: boolean - Enable address validation - Default: true
- `notificationEnabled`: boolean - Send update notifications - Default: true

**Environment-Specific Configuration**:
- Development: Use test LEI validation and simplified approval
- Production: Use production LEI validation and full approval workflow

### 10. Integration Points
**API Contracts**:
- Input: CounterpartyUpdateRequest with fields to update and metadata
- Output: UpdatedCounterparty with update confirmation and audit information

**Data Exchange Formats**:
- **JSON**: Primary format for API requests and responses
- **XML**: Legacy system integration format

**Event Publishing**:
- **CounterpartyUpdated**: Published when update completes successfully
- **CounterpartyUpdateFailed**: Published when update fails validation

**Event Consumption**:
- **ApprovalCompleted**: Triggers final update application after approval
- **CounterpartyDataChanged**: External counterparty data change notifications
