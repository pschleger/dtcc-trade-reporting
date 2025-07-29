# archiveCancellation Processor Specification

### 1. Component Overview
**Component Name**: archiveCancellation
**Component Type**: CyodaProcessor
**Business Domain**: Cancellation Processing
**Purpose**: Archives completed trade cancellations for regulatory compliance and audit trail maintenance
**Workflow Context**: Final step in cancellation workflows after successful reporting and confirmation

### 2. Input Specifications
**Entity Type**: CompletedCancellation
**Required Fields**:
- `cancellationId`: string - Unique identifier of the cancellation
- `tradeId`: string - Unique identifier of the cancelled trade
- `cancellationDetails`: CancellationRecord - Complete cancellation execution details
- `reportingStatus`: ReportingStatus - DTCC submission and acknowledgment status
- `completionTimestamp`: ISO-8601 timestamp - When cancellation process completed
- `auditTrail`: array - Complete audit trail of cancellation process

**Optional Fields**:
- `regulatoryConfirmations`: array - Regulatory acknowledgments and confirmations
- `counterpartyAcknowledgments`: array - Counterparty confirmation records
- `attachments`: array - Supporting documents and evidence

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 15000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Regulatory retention policies and requirements
- Archive storage configuration and credentials
- Data classification and security requirements

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "ArchivedCancellation",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "archiveId": "ARCH-CANC-20240115-001",
      "archiveLocation": "s3://regulatory-archive/cancellations/2024/01/",
      "retentionPeriod": "7 years",
      "compressionRatio": 0.65,
      "checksumMD5": "d41d8cd98f00b204e9800998ecf8427e"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "ARCHIVAL_FAILED",
  "errorMessage": "Failed to archive cancellation due to storage system unavailability",
  "details": {
    "storageSystem": "regulatory-archive",
    "errorType": "CONNECTIVITY_ERROR",
    "retryAttempts": 3
  }
}
```

**Side Effects**:
- Moves cancellation data to long-term regulatory archive storage
- Updates cancellation status to ARCHIVED in operational database
- Creates archive index entry for future retrieval
- Removes cancellation data from active operational systems

### 4. Business Logic
**Processing Steps**:
1. Validate cancellation completion status and regulatory reporting confirmation
2. Compile complete cancellation package including all related documents
3. Apply data classification and security labeling based on regulatory requirements
4. Compress and encrypt cancellation package for long-term storage
5. Transfer package to regulatory archive storage with redundancy
6. Create searchable index entry with metadata for future retrieval
7. Update operational systems to reflect archived status
8. Generate archival confirmation and audit log entry

**Business Rules**:
- **Retention Period**: Cancellation records must be retained for minimum 7 years per regulatory requirements
- **Data Integrity**: All archived data must maintain cryptographic integrity verification
- **Access Control**: Archived data access must be logged and authorized
- **Completeness Verification**: Archive package must include all related documents and audit trails
- **Regulatory Compliance**: Archive format must support regulatory examination and discovery

**Algorithms**:
- Data compression using industry-standard algorithms (gzip/lz4)
- AES-256 encryption for sensitive regulatory data
- SHA-256 checksums for data integrity verification

### 5. Validation Rules
**Pre-processing Validations**:
- **Completion Status**: Cancellation must be in COMPLETED status
- **Reporting Confirmation**: DTCC reporting must be acknowledged or confirmed
- **Data Completeness**: All required documents and audit trails must be present
- **Regulatory Compliance**: All regulatory requirements must be satisfied

**Post-processing Validations**:
- **Archive Integrity**: Verify successful storage and data integrity
- **Index Creation**: Confirm searchable index entry creation
- **Access Verification**: Validate archive retrieval mechanisms
- **Cleanup Confirmation**: Verify removal from operational systems

**Data Quality Checks**:
- Completeness verification of all cancellation-related records
- Consistency check between archived data and operational records
- Validation of encryption and compression success

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Incomplete cancellation data or missing regulatory confirmations
- **PROCESSING_ERROR**: Archive package creation or compression failures
- **SYSTEM_ERROR**: Storage system connectivity or capacity issues
- **TIMEOUT_ERROR**: Archive operation exceeded timeout limits

**Error Recovery**:
- Retry archive operation up to 3 times with exponential backoff
- Fallback to alternative archive storage locations
- Manual intervention workflow for persistent storage failures

**Error Propagation**:
- Return detailed error information to calling workflow
- Generate alerts for archive system administrators
- Escalate to compliance team for regulatory deadline risks

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 10000 milliseconds (95th percentile)
- **Throughput**: 50 archives per minute
- **Availability**: 99.5% uptime

**Resource Requirements**:
- **CPU**: High intensity for compression and encryption operations
- **Memory**: 1GB per concurrent archive operation
- **I/O**: Very high for large data transfers to archive storage

**Scalability**:
- Horizontal scaling through distributed archive processing nodes
- Performance scales with archive package size and compression ratio

### 8. Dependencies
**Internal Dependencies**:
- **Cancellation Repository**: Source of cancellation data and status
- **Document Management System**: Access to related documents and attachments
- **Audit Trail Service**: Complete audit history for archival

**External Dependencies**:
- **Archive Storage System**: Long-term regulatory storage (SLA: 99.9%)
- **Encryption Service**: Data encryption and key management (SLA: 99.95%)
- **Index Service**: Searchable archive index (SLA: 99.5%)

**Data Dependencies**:
- Regulatory retention policies and requirements
- Data classification schemas and security requirements
- Archive storage configuration and access credentials

### 9. Configuration Parameters
**Required Configuration**:
- `retentionPeriodYears`: integer - Regulatory retention period - Default: 7
- `compressionLevel`: integer - Compression level (1-9) - Default: 6
- `encryptionEnabled`: boolean - Enable data encryption - Default: true

**Optional Configuration**:
- `archiveRedundancy`: integer - Number of archive copies - Default: 2
- `indexingEnabled`: boolean - Create searchable index - Default: true

**Environment-Specific Configuration**:
- Development: Use test archive storage with shorter retention
- Production: Use production archive storage with full regulatory retention

### 10. Integration Points
**API Contracts**:
- Input: CompletedCancellation entity with full cancellation lifecycle data
- Output: ArchivedCancellation entity with archive location and metadata

**Data Exchange Formats**:
- **Compressed Archive**: Primary format for long-term storage
- **JSON Metadata**: Archive index and retrieval information

**Event Publishing**:
- **CancellationArchived**: Published when archival completes successfully
- **CancellationArchivalFailed**: Published when archival fails

**Event Consumption**:
- **CancellationReportConfirmed**: Triggers cancellation archival workflow
