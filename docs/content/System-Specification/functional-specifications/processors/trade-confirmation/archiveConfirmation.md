# archiveConfirmation Processor Specification

## 1. Component Overview
**Component Name**: archiveConfirmation
**Component Type**: CyodaProcessor
**Business Domain**: Trade Confirmation Processing
**Purpose**: Archives processed trade confirmation messages for regulatory compliance and audit trail maintenance
**Workflow Context**: TradeConfirmationWorkflow (processed state)

## 2. Input Specifications
**Entity Type**: TradeConfirmation
**Required Fields**:
- `confirmationId`: string - Unique confirmation identifier
- `processingStatus`: string - Final processing status ("processed" or "rejected")
- `processingTimestamp`: ISO-8601 timestamp - When processing completed
- `createdTradeId`: string - ID of created trade entity (if successful)
- `fpmlMessage`: string - Original FpML message content

**Optional Fields**:
- `processingErrors`: array - List of processing errors if any
- `dataQualityMetrics`: object - Quality assessment results
- `auditTrail`: array - Complete processing audit trail

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "archival" - Tags for archival processing nodes
- `responseTimeoutMs`: 15000 - Maximum processing time (15 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-123456789abe" - Process parameter reference

**Context Data**:
- Archive storage configuration
- Retention policy settings
- Compliance requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeConfirmation",
    "processingTimestamp": "2024-01-15T10:45:00Z",
    "archiveDetails": {
      "archiveId": "ARC-20240115-001",
      "archiveLocation": "s3://dtcc-archive/confirmations/2024/01/15/",
      "retentionDate": "2031-01-15",
      "compressionRatio": 0.75
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "ARCHIVAL_ERROR",
  "errorMessage": "Confirmation archival failed",
  "details": {
    "storageErrors": ["Storage service unavailable"],
    "compressionErrors": ["Compression failed"],
    "metadataErrors": ["Archive metadata creation failed"]
  }
}
```

**Side Effects**:
- Stores confirmation data in long-term archive storage
- Updates entity status to "archived"
- Creates archive metadata for retrieval
- Publishes ConfirmationArchived event

## 4. Business Logic
**Processing Steps**:
1. Validate confirmation is ready for archival
2. Compress confirmation data for storage efficiency
3. Generate archive metadata with retention information
4. Store compressed data in designated archive location
5. Update entity status and archive references
6. Create audit trail entry for archival action
7. Verify successful storage and metadata creation

**Business Rules**:
- **Retention Period**: 7 years minimum retention for regulatory compliance
- **Data Integrity**: Archive must maintain complete data integrity
- **Compression**: Use industry-standard compression for storage efficiency
- **Metadata**: Complete metadata for future retrieval and compliance
- **Access Control**: Secure storage with appropriate access controls

**Algorithms**:
- GZIP compression for data storage efficiency
- SHA-256 checksums for data integrity verification
- Hierarchical storage organization by date and entity type

## 5. Validation Rules
**Pre-processing Validations**:
- **Processing Complete**: Confirmation must have final processing status
- **Data Completeness**: All required data elements present
- **Storage Availability**: Archive storage system accessible

**Post-processing Validations**:
- **Storage Verification**: Confirm successful data storage
- **Integrity Check**: Verify data integrity through checksums
- **Metadata Creation**: Confirm archive metadata properly created

**Data Quality Checks**:
- **Compression Verification**: Ensure compression successful and reversible
- **Retention Calculation**: Verify correct retention date calculation
- **Access Permissions**: Confirm appropriate storage permissions set

## 6. Error Handling
**Error Categories**:
- **ARCHIVAL_ERROR**: Storage or compression failures
- **VALIDATION_ERROR**: Pre-archival validation failures
- **SYSTEM_ERROR**: Storage system or infrastructure failures
- **TIMEOUT_ERROR**: Archival processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient storage errors (max 3 retries)
- Alternative storage location for primary storage failures
- Manual intervention queue for persistent archival failures

**Error Propagation**:
- Archival errors logged for operational review
- Failed archival triggers manual review workflow
- Critical storage errors escalated to infrastructure team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 10 seconds (95th percentile)
- **Throughput**: 200 archival operations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity for compression operations
- **Memory**: 256MB per concurrent archival
- **I/O**: High for storage operations

**Scalability**:
- Horizontal scaling through multiple archive nodes
- Performance varies with data size and compression ratio
- Storage bandwidth may become bottleneck at scale

## 8. Dependencies
**Internal Dependencies**:
- **Storage Service**: Long-term archive storage system
- **Compression Service**: Data compression utilities
- **Metadata Service**: Archive metadata management

**External Dependencies**:
- **Archive Storage**: AWS S3 or equivalent (SLA: 99.9% availability)
- **Backup Service**: Secondary backup storage (daily sync)

**Data Dependencies**:
- Archive storage configuration
- Retention policy definitions
- Compression algorithm settings

## 9. Configuration Parameters
**Required Configuration**:
- `retentionYears`: integer - Retention period in years - Default: 7
- `compressionEnabled`: boolean - Enable data compression - Default: true
- `archiveLocation`: string - Base archive storage location

**Optional Configuration**:
- `compressionLevel`: integer - Compression level (1-9) - Default: 6
- `checksumEnabled`: boolean - Enable integrity checksums - Default: true
- `encryptionEnabled`: boolean - Enable archive encryption - Default: true

**Environment-Specific Configuration**:
- Development: Local file system storage, reduced retention
- Production: Cloud storage, full retention period

## 10. Integration Points
**API Contracts**:
- Input: TradeConfirmation entity with processing completion status
- Output: Archive confirmation with storage location and metadata

**Data Exchange Formats**:
- **Compressed Binary**: Archived confirmation data
- **JSON**: Archive metadata format
- **XML**: Original FpML message preservation

**Event Publishing**:
- **ConfirmationArchived**: Published on successful archival with archive details
- **ArchivalFailed**: Published on archival failure with error details

**Event Consumption**:
- **ProcessingCompleted**: Triggers archival process
- **RetentionPolicyUpdated**: Updates retention calculations
