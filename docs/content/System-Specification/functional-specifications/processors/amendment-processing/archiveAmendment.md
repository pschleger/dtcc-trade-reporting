# archiveAmendment Processor Specification

## 1. Component Overview
**Component Name**: archiveAmendment
**Component Type**: CyodaProcessor
**Business Domain**: Amendment Processing
**Purpose**: Archives completed trade amendments with audit trail, supporting documents, and regulatory compliance for long-term retention
**Workflow Context**: AmendmentWorkflow (archiving state)

## 2. Input Specifications
**Entity Type**: TradeAmendment
**Required Fields**:
- `amendmentId`: string - Unique amendment request identifier
- `originalTradeId`: string - Original trade identifier that was amended
- `amendedTradeId`: string - Trade identifier after amendment
- `completionStatus`: string - Amendment completion status (COMPLETED, FAILED, CANCELLED)
- `completionDate`: ISO-8601 date - Amendment completion date
- `archivalReason`: string - Reason for archival (COMPLETION, RETENTION_POLICY, REGULATORY)

**Optional Fields**:
- `retentionPeriod`: integer - Retention period in years
- `archivalNotes`: string - Additional notes for archival
- `legalHoldFlag`: boolean - Whether amendment is under legal hold
- `encryptionRequired`: boolean - Whether encryption is required for archival

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "archival" - Tags for archival processing nodes
- `responseTimeoutMs`: 120000 - Maximum processing time (2 minutes)
- `processParamId`: "01932b4e-7890-7123-8456-323456789vwx" - Process parameter reference

**Context Data**:
- Amendment audit trail data
- Supporting documents and reports
- Regulatory retention requirements
- Archival storage configuration

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeAmendment",
    "processingTimestamp": "2024-01-15T17:00:00Z",
    "archivalResults": {
      "archivalId": "AMD_ARCH_2024_001",
      "archivalLocation": "/archive/amendments/2024/AMD_ARCH_2024_001",
      "archivalSize": 5242880,
      "compressionRatio": 0.75,
      "encryptionStatus": "ENCRYPTED",
      "checksumMD5": "d41d8cd98f00b204e9800998ecf8427e"
    },
    "archivedComponents": {
      "amendmentData": "ARCHIVED",
      "auditTrail": "ARCHIVED",
      "supportingDocuments": "ARCHIVED",
      "reports": "ARCHIVED",
      "approvalDocuments": "ARCHIVED"
    },
    "retentionDetails": {
      "retentionPeriod": 7,
      "retentionExpiry": "2031-01-15",
      "legalHoldStatus": false,
      "destructionScheduled": true
    },
    "complianceStatus": {
      "regulatoryCompliance": "COMPLIANT",
      "dataProtectionCompliance": "COMPLIANT",
      "auditTrailComplete": true
    },
    "accessControl": {
      "accessLevel": "RESTRICTED",
      "authorizedRoles": ["COMPLIANCE_OFFICER", "AUDIT_MANAGER"],
      "accessLoggingEnabled": true
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "AMENDMENT_ARCHIVAL_FAILED",
  "errorMessage": "Amendment archival failed due to inaudit trail",
  "details": {
    "amendmentId": "AMD-001",
    "missingComponents": ["approval_documents", "impact_assessment"],
    "archivalReason": "COMPLETION"
  }
}
```

**Side Effects**:
- Creates compressed and encrypted archive of amendment data
- Updates amendment entity with archival status and location
- Removes amendment data from active systems
- Creates archival audit trail entry
- Schedules retention period monitoring

## 4. Business Logic
**Processing Steps**:
1. Validate amendment archival request and completion status
2. Gather all amendment-related data and documents
3. Verify audit trail completeness and integrity
4. Collect supporting documents and reports
5. Determine retention period based on regulatory requirements
6. Create compressed archive with all amendment components
7. Apply encryption if required by policy or regulation
8. Store archive in designated long-term storage location
9. Update amendment entity with archival metadata
10. Create archival audit trail and schedule retention monitoring

**Business Rules**:
- **Completion Requirement**: Only completed amendments can be archived
- **Audit Trail Completeness**: Complete audit trail required for archival
- **Retention Compliance**: Archival must comply with regulatory retention periods
- **Data Integrity**: All archived data must maintain integrity
- **Access Control**: Archived data must have appropriate access controls

**Algorithms**:
- Data compression algorithms for storage optimization
- Encryption algorithms for sensitive data protection
- Integrity checking using checksums and digital signatures
- Retention period calculation based on regulatory requirements

## 5. Validation Rules
**Pre-processing Validations**:
- **Amendment Completion**: Amendment must be in completed state
- **Audit Trail Completeness**: Complete audit trail must exist
- **Document Availability**: All supporting documents must be available
- **Retention Policy**: Retention requirements must be determined

**Post-processing Validations**:
- **Archive Integrity**: Archive must be complete and uncorrupted
- **Encryption Validation**: Encryption must be properly applied if required
- **Storage Verification**: Archive must be successfully stored
- **Metadata Accuracy**: All archival metadata must be accurate

**Data Quality Checks**:
- **Data Completeness**: All amendment data must be included
- **Document Integrity**: All documents must be complete and readable
- **Checksum Validation**: Archive integrity must be verified

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Amendment archival validation failures
- **DATA_INCOMPLETE_ERROR**: Required amendment data missing
- **STORAGE_ERROR**: Archive storage failures
- **ENCRYPTION_ERROR**: Data encryption failures
- **TIMEOUT_ERROR**: Archival timeout exceeded

**Error Recovery**:
- Retry mechanism for transient storage failures
- Partial archival with missing component tracking
- Manual intervention procedures for critical failures

**Error Propagation**:
- Detailed error information provided to calling workflows
- Amendment status updated with archival failure details
- Error notifications sent to compliance teams

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 120000 milliseconds (95th percentile)
- **Throughput**: 50 archival operations per hour
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Moderate computational requirements for compression and encryption
- **Memory**: 1GB for large amendment datasets and processing
- **I/O**: High disk I/O for archive creation and storage

**Scalability**:
- Horizontal scaling through archival node distribution
- Storage optimization through compression and deduplication
- Efficient archival processes for high-volume operations

## 8. Dependencies
**Internal Dependencies**:
- **AmendmentService**: Amendment data and audit trail retrieval
- **DocumentService**: Supporting document retrieval
- **StorageService**: Archive storage and management
- **EncryptionService**: Data encryption and key management
- **RetentionService**: Retention policy management

**External Dependencies**:
- **Archive Storage System**: Long-term storage infrastructure (SLA: 99.9%)
- **Encryption Key Management**: Key management service (SLA: 99.9%)

**Data Dependencies**:
- Complete amendment audit trail
- Supporting documents and reports
- Regulatory retention requirements
- Archival storage configuration

## 9. Configuration Parameters
**Required Configuration**:
- `archivalTimeoutMs`: integer - Maximum archival time - Default: 120000
- `archiveStorageLocation`: string - Base path for archive storage - Default: "/archive/amendments"
- `compressionEnabled`: boolean - Enable data compression - Default: true
- `defaultRetentionYears`: integer - Default retention period - Default: 7

**Optional Configuration**:
- `encryptionEnabled`: boolean - Enable data encryption - Default: true
- `checksumValidation`: boolean - Enable checksum validation - Default: true
- `accessLoggingEnabled`: boolean - Enable access logging - Default: true

**Environment-Specific Configuration**:
- **Development**: Reduced retention periods and simplified archival
- **Production**: Full retention periods and complete archival

## 10. Integration Points
**API Contracts**:
- **Input**: Amendment archival request with completion status and retention requirements
- **Output**: Archival results with location, integrity verification, and retention details

**Data Exchange Formats**:
- **Binary**: Compressed and encrypted archive format
- **JSON**: Archival metadata and status information
- **XML**: Regulatory compliance documentation

**Event Publishing**:
- **AmendmentArchived**: Published when archival completes successfully
- **AmendmentArchivalFailed**: Published when archival fails
- **RetentionScheduled**: Published when retention monitoring is scheduled

**Event Consumption**:
- **AmendmentCompleted**: Triggers amendment archival process
- **RetentionPolicyUpdated**: Updates retention requirements for amendments
