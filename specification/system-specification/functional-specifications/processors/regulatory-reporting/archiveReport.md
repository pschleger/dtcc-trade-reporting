# archiveReport Processor Specification

## 1. Component Overview
**Component Name**: archiveReport
**Component Type**: CyodaProcessor
**Business Domain**: Regulatory Reporting
**Purpose**: Archives submitted regulatory reports for long-term storage and regulatory compliance requirements
**Workflow Context**: RegulatoryReportWorkflow (archiving state)

## 2. Input Specifications
**Entity Type**: RegulatoryReport
**Required Fields**:
- `reportId`: string - Unique identifier of the submitted report
- `submissionTimestamp`: ISO-8601 timestamp - When report was submitted to DTCC
- `reportContent`: string - Base64-encoded report content
- `reportMetadata`: object - Report metadata including type, period, entity
- `submissionStatus`: string - Final submission status ("ACCEPTED", "REJECTED", "ACKNOWLEDGED")
- `dtccResponse`: object - Complete DTCC response including acknowledgment details

**Optional Fields**:
- `correctionHistory`: array - History of corrections if applicable
- `relatedReports`: array - References to related or superseded reports
- `retentionPeriod`: integer - Custom retention period in years (default: 7)
- `archiveNotes`: string - Additional notes for archival purposes

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "archival" - Tags for archival processing nodes
- `responseTimeoutMs`: 60000 - Maximum processing time (1 minute)
- `processParamId`: "01932b4e-7890-7123-8456-423456789def" - Process parameter reference

**Context Data**:
- Regulatory retention requirements and policies
- Archive storage configuration and credentials
- Data classification and security requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "RegulatoryReport",
    "processingTimestamp": "2024-01-15T16:45:00Z",
    "archivalDetails": {
      "archiveId": "ARC-20240115-001",
      "archiveLocation": "s3://regulatory-archive/2024/01/15/",
      "retentionUntil": "2031-01-15T23:59:59Z",
      "compressionRatio": 0.75,
      "checksumMD5": "d41d8cd98f00b204e9800998ecf8427e"
    },
    "complianceStatus": "ARCHIVED_COMPLIANT"
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "ARCHIVAL_FAILED",
  "errorMessage": "Failed to archive regulatory report",
  "details": {
    "reportId": "RPT-20240115-001",
    "failureReason": "Storage system unavailable",
    "retryable": true
  }
}
```

**Side Effects**:
- Report data stored in long-term archive storage
- Archive metadata recorded in compliance database
- Retention schedule entry created
- Original report marked as archived in operational database

## 4. Business Logic
**Processing Steps**:
1. Validate report is in final state (submitted and acknowledged)
2. Prepare report data for archival (compression, encryption)
3. Generate archive metadata and retention schedule
4. Store report in long-term archive storage
5. Record archival transaction in compliance database
6. Update operational database with archive reference
7. Verify archive integrity and accessibility

**Business Rules**:
- **Retention Period**: Minimum 7 years from submission date per regulatory requirements
- **Data Integrity**: Archive must maintain bit-perfect copy of original report
- **Encryption**: All archived data must be encrypted at rest using AES-256
- **Immutability**: Archived reports cannot be modified, only superseded
- **Accessibility**: Archived reports must be retrievable within 24 hours for regulatory requests

**Algorithms**:
- GZIP compression for report content optimization
- SHA-256 checksum calculation for integrity verification
- Hierarchical storage management for cost optimization

## 5. Validation Rules
**Pre-processing Validations**:
- **Report Status**: Report must have final submission status (not pending)
- **DTCC Response**: Valid DTCC acknowledgment or rejection must be present
- **Content Integrity**: Report content must match original submission checksum
- **Metadata Completeness**: All required metadata fields must be populated

**Post-processing Validations**:
- **Archive Integrity**: Stored archive must pass checksum verification
- **Metadata Consistency**: Archive metadata must match source report
- **Retention Schedule**: Retention period must comply with regulatory minimums

**Data Quality Checks**:
- **Storage Verification**: Confirm successful storage in archive system
- **Retrieval Test**: Verify archived report can be successfully retrieved

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input validation failures or incomplete data
- **STORAGE_ERROR**: Archive storage system failures or capacity issues
- **ENCRYPTION_ERROR**: Data encryption or security processing failures
- **COMPLIANCE_ERROR**: Regulatory compliance requirement violations
- **SYSTEM_ERROR**: Technical system failures or connectivity issues

**Error Recovery**:
- **Retry Logic**: Automatic retry with exponential backoff for transient failures
- **Alternative Storage**: Failover to secondary archive storage if primary unavailable
- **Manual Intervention**: Alert operations team for non-recoverable failures

**Error Propagation**:
- **Workflow Notification**: Notify parent workflow of archival status
- **Compliance Alert**: Generate compliance alerts for archival failures
- **Audit Trail**: Record all archival attempts and outcomes for audit purposes

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 30 seconds (95th percentile) for standard reports
- **Throughput**: 100 reports per minute during peak archival periods
- **Availability**: 99.9% uptime for archival operations

**Resource Requirements**:
- **CPU**: Moderate for compression and encryption operations
- **Memory**: 512MB per concurrent archival operation
- **Storage**: Long-term archive storage with 99.999% durability
- **Network**: Reliable connectivity to archive storage systems

**Scalability**:
- **Horizontal Scaling**: Support parallel archival operations across multiple nodes
- **Storage Scaling**: Automatic scaling of archive storage capacity
- **Performance Degradation**: Graceful degradation under high load conditions

## 8. Dependencies
**Internal Dependencies**:
- **Archive Storage Service**: Long-term storage system for regulatory data
- **Encryption Service**: Data encryption and key management
- **Compliance Database**: Metadata and retention schedule storage
- **Audit Service**: Archival transaction logging and audit trail

**External Dependencies**:
- **Cloud Storage Provider**: AWS S3 or equivalent for archive storage (99.9% SLA)
- **HSM Service**: Hardware security module for encryption keys (99.95% SLA)

**Data Dependencies**:
- **Regulatory Retention Policies**: Current retention requirements by jurisdiction
- **Data Classification Rules**: Security and handling requirements for report types

## 9. Configuration Parameters
**Required Configuration**:
- `archiveStorageEndpoint`: string - Archive storage system endpoint
- `encryptionKeyId`: string - Encryption key identifier for report data
- `defaultRetentionYears`: integer - Default retention period - Default: 7
- `compressionEnabled`: boolean - Enable report compression - Default: true

**Optional Configuration**:
- `checksumAlgorithm`: string - Checksum algorithm for integrity - Default: "SHA-256"
- `compressionLevel`: integer - GZIP compression level (1-9) - Default: 6
- `archiveReplicationFactor`: integer - Number of archive copies - Default: 3
- `retrievalTestEnabled`: boolean - Enable post-archive retrieval test - Default: true

**Environment-Specific Configuration**:
- **Development**: Local file system storage with reduced retention
- **Production**: Cloud storage with full regulatory compliance and encryption

## 10. Integration Points
**API Contracts**:
- **Input**: RegulatoryReport entity with submission status and DTCC response
- **Output**: Archive confirmation with location and retention details

**Data Exchange Formats**:
- **Archive Format**: Compressed and encrypted binary format
- **Metadata Format**: JSON metadata with archive details and retention schedule

**Event Publishing**:
- **ReportArchived**: Published when report successfully archived with archive details
- **ArchivalFailed**: Published when archival fails with error details and retry information

**Event Consumption**:
- **ReportSubmitted**: Triggers archival process for successfully submitted reports
- **RetentionExpired**: Handles retention period expiration and data purging
