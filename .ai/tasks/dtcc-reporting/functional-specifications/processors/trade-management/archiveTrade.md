# archiveTrade Processor Specification

## 1. Component Overview
**Component Name**: archiveTrade
**Component Type**: CyodaProcessor
**Business Domain**: Trade Management
**Purpose**: Archives completed or cancelled trades for regulatory compliance and audit trail maintenance
**Workflow Context**: TradeWorkflow (archiving state)

## 2. Input Specifications
**Entity Type**: Trade
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `tradeStatus`: string - Final trade status (matured, cancelled, terminated)
- `completionTimestamp`: ISO-8601 timestamp - When trade reached final status
- `archivalReason`: string - Reason for archival (maturity, cancellation, etc.)
- `finalPositions`: object - Final position data for the trade

**Optional Fields**:
- `settlementDetails`: object - Final settlement information
- `regulatoryReports`: array - Associated regulatory reports
- `auditTrail`: array - Complete trade processing audit trail
- `retentionOverride`: object - Custom retention period settings

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "archival" - Tags for archival processing nodes
- `responseTimeoutMs`: 45000 - Maximum processing time (45 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-223456789abh" - Process parameter reference

**Context Data**:
- Archive storage configuration
- Retention policy settings
- Regulatory compliance requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Trade",
    "processingTimestamp": "2024-01-15T16:30:00Z",
    "archivalResults": {
      "tradeArchived": true,
      "archiveLocation": "s3://dtcc-archive/trades/2024/01/15/",
      "archiveId": "ARC-TRD-20240115-001",
      "retentionDate": "2031-01-15",
      "compressionRatio": 0.72
    },
    "archivalMetadata": {
      "dataSize": 1048576,
      "archivalDuration": 35000,
      "integrityChecksum": "sha256:abc123...",
      "encryptionApplied": true
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "ARCHIVAL_ERROR",
  "errorMessage": "Trade archival failed",
  "details": {
    "storageErrors": ["Archive storage service unavailable"],
    "compressionErrors": ["Data compression failed"],
    "encryptionErrors": ["Encryption key unavailable"],
    "validationErrors": ["Trade not eligible for archival"]
  }
}
```

**Side Effects**:
- Updates Trade entity status to archived
- Stores trade data in long-term archive storage
- Creates archival metadata for retrieval
- Publishes TradeArchived event

## 4. Business Logic
**Processing Steps**:
1. Validate trade eligibility for archival
2. Gather complete trade data and related information
3. Compress trade data for storage efficiency
4. Encrypt sensitive trade information
5. Store archived data in designated storage location
6. Generate archive metadata and retrieval information
7. Update trade entity with archival status and references

**Business Rules**:
- **Archival Eligibility**: Trade must be in final status (matured, cancelled, terminated)
- **Data Completeness**: All trade data and audit trail must be complete
- **Retention Period**: 7 years minimum retention for regulatory compliance
- **Data Security**: Sensitive data must be encrypted in archive
- **Integrity**: Archive must maintain complete data integrity

**Algorithms**:
- GZIP compression for storage efficiency
- AES-256 encryption for sensitive data protection
- SHA-256 checksums for data integrity verification
- Hierarchical storage organization by date and status

## 5. Validation Rules
**Pre-processing Validations**:
- **Final Status**: Trade must be in archival-eligible status
- **Data Completeness**: All required trade data present
- **Storage Availability**: Archive storage system accessible

**Post-processing Validations**:
- **Storage Verification**: Confirm successful data storage
- **Integrity Check**: Verify data integrity through checksums
- **Metadata Creation**: Confirm archive metadata properly created

**Data Quality Checks**:
- **Compression Verification**: Ensure compression successful and reversible
- **Encryption Verification**: Confirm encryption applied correctly
- **Retention Calculation**: Verify correct retention date calculation

## 6. Error Handling
**Error Categories**:
- **ARCHIVAL_ERROR**: Storage, compression, or encryption failures
- **VALIDATION_ERROR**: Pre-archival validation failures
- **SYSTEM_ERROR**: Storage system or infrastructure failures
- **SECURITY_ERROR**: Encryption or security-related failures
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
- **Response Time**: 35 seconds (95th percentile)
- **Throughput**: 50 archival operations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for compression and encryption operations
- **Memory**: 1GB per concurrent archival
- **I/O**: Very high for storage operations

**Scalability**:
- Horizontal scaling through multiple archive nodes
- Performance varies with trade data size and complexity
- Storage bandwidth may become bottleneck at scale

## 8. Dependencies
**Internal Dependencies**:
- **Storage Service**: Long-term archive storage system
- **Encryption Service**: Data encryption and key management
- **Compression Service**: Data compression utilities
- **Metadata Service**: Archive metadata management

**External Dependencies**:
- **Archive Storage**: AWS S3 or equivalent (SLA: 99.9% availability)
- **Key Management Service**: Encryption key management (SLA: 99.95% availability)

**Data Dependencies**:
- Archive storage configuration
- Retention policy definitions
- Encryption key configuration
- Compression algorithm settings

## 9. Configuration Parameters
**Required Configuration**:
- `retentionYears`: integer - Retention period in years - Default: 7
- `encryptionEnabled`: boolean - Enable data encryption - Default: true
- `compressionEnabled`: boolean - Enable data compression - Default: true

**Optional Configuration**:
- `compressionLevel`: integer - Compression level (1-9) - Default: 6
- `encryptionAlgorithm`: string - Encryption algorithm - Default: "AES-256"
- `checksumEnabled`: boolean - Enable integrity checksums - Default: true

**Environment-Specific Configuration**:
- Development: Local file system storage, reduced retention
- Production: Cloud storage, full retention period

## 10. Integration Points
**API Contracts**:
- Input: Trade entity with final status and complete data
- Output: Archival confirmation with storage location and metadata

**Data Exchange Formats**:
- **Compressed Binary**: Archived trade data
- **JSON**: Archive metadata format
- **Encrypted**: Sensitive trade information

**Event Publishing**:
- **TradeArchived**: Published on successful archival with archive details
- **ArchivalFailed**: Published on archival failure with error details

**Event Consumption**:
- **TradeCompleted**: Triggers trade archival process
- **RetentionPolicyUpdated**: Updates retention calculations
