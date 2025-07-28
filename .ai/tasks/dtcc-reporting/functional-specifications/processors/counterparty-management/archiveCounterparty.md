# archiveCounterparty Processor Specification

### 1. Component Overview
**Component Name**: archiveCounterparty
**Component Type**: CyodaProcessor
**Business Domain**: Counterparty Management
**Purpose**: Archives decommissioned counterparty data for regulatory compliance while maintaining historical trade relationships
**Workflow Context**: Final step in counterparty lifecycle management for inactive or terminated counterparties

### 2. Input Specifications
**Entity Type**: CounterpartyArchivalRequest
**Required Fields**:
- `counterpartyId`: string - Unique identifier of the counterparty to archive
- `archivalReason`: string - Business reason for archival (BUSINESS_CLOSURE, RELATIONSHIP_TERMINATION, REGULATORY_REQUIREMENT)
- `finalStatus`: string - Final counterparty status before archival
- `authorizingUser`: string - User authorizing the archival
- `requestTimestamp`: ISO-8601 timestamp - When archival was requested

**Optional Fields**:
- `effectiveDate`: ISO-8601 timestamp - When archival should take effect
- `retentionPeriod`: integer - Custom retention period in years
- `historicalTradeCount`: integer - Number of historical trades with counterparty
- `supportingDocuments`: array - Documentation supporting archival decision

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 30000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Complete counterparty historical data and relationships
- Outstanding obligations and settlement status
- Regulatory retention requirements and compliance data

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "ArchivedCounterparty",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "archiveId": "ARCH-CP-20240115-001",
      "archiveLocation": "s3://regulatory-archive/counterparties/2024/01/",
      "retentionPeriodYears": 10,
      "historicalTradesArchived": 1250,
      "checksumMD5": "e3b0c44298fc1c149afbf4c8996fb924"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "ARCHIVAL_BLOCKED_OUTSTANDING_OBLIGATIONS",
  "errorMessage": "Counterparty archival blocked due to outstanding settlement obligations",
  "details": {
    "outstandingTrades": 3,
    "unsettledAmount": 150000,
    "oldestUnsettledDate": "2024-01-10T00:00:00Z"
  }
}
```

**Side Effects**:
- Moves counterparty data to long-term regulatory archive storage
- Updates counterparty status to ARCHIVED in operational database
- Preserves historical trade relationships for regulatory reporting
- Removes counterparty from active operational systems

### 4. Business Logic
**Processing Steps**:
1. Validate archival request and verify counterparty eligibility
2. Check for outstanding trades, settlements, and obligations
3. Compile complete counterparty data package including historical relationships
4. Verify regulatory retention requirements and compliance
5. Create compressed and encrypted archive package
6. Transfer package to regulatory archive storage with redundancy
7. Update operational systems to reflect archived status
8. Generate archival confirmation and audit trail

**Business Rules**:
- **Outstanding Obligations**: No outstanding trades or settlements allowed before archival
- **Retention Period**: Minimum 10-year retention for regulatory compliance
- **Data Completeness**: All historical data and relationships must be preserved
- **Access Control**: Archived data access must be logged and authorized
- **Regulatory Compliance**: Archive must support regulatory examination and discovery

**Algorithms**:
- Outstanding obligation verification across all trading systems
- Historical data compilation and relationship mapping
- Data compression and encryption for long-term storage

### 5. Validation Rules
**Pre-processing Validations**:
- **Counterparty Status**: Counterparty must be in INACTIVE or TERMINATED status
- **Outstanding Obligations**: No pending trades, settlements, or obligations
- **Authorization Level**: User must have sufficient authority for archival
- **Regulatory Compliance**: All regulatory requirements must be satisfied

**Post-processing Validations**:
- **Archive Integrity**: Verify successful storage and data integrity
- **Historical Preservation**: Confirm all historical relationships preserved
- **Access Verification**: Validate archive retrieval mechanisms
- **Cleanup Confirmation**: Verify removal from operational systems

**Data Quality Checks**:
- Completeness verification of all counterparty-related records
- Consistency check between archived data and operational records
- Validation of encryption and compression success

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Outstanding obligations or incomplete data
- **PROCESSING_ERROR**: Archive package creation or compression failures
- **SYSTEM_ERROR**: Storage system connectivity or capacity issues
- **TIMEOUT_ERROR**: Archive operation exceeded timeout limits

**Error Recovery**:
- Retry archive operation up to 3 times with exponential backoff
- Fallback to alternative archive storage locations
- Manual intervention workflow for persistent storage failures

**Error Propagation**:
- Return detailed error information to requesting user
- Generate alerts for archive system administrators
- Escalate to compliance team for regulatory deadline risks

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 15000 milliseconds (95th percentile)
- **Throughput**: 20 archives per minute
- **Availability**: 99.5% uptime

**Resource Requirements**:
- **CPU**: High intensity for data compilation and compression
- **Memory**: 2GB per concurrent archive operation
- **I/O**: Very high for large historical data transfers

**Scalability**:
- Horizontal scaling through distributed archive processing nodes
- Performance scales with historical data volume and relationship complexity

### 8. Dependencies
**Internal Dependencies**:
- **Counterparty Repository**: Complete counterparty data and relationships
- **Trade Repository**: Historical trade data and settlement records
- **Settlement System**: Outstanding obligation verification

**External Dependencies**:
- **Archive Storage System**: Long-term regulatory storage (SLA: 99.9%)
- **Encryption Service**: Data encryption and key management (SLA: 99.95%)
- **Regulatory Database**: Compliance verification (SLA: 99.5%)

**Data Dependencies**:
- Complete counterparty master data and historical relationships
- All historical trade and settlement data
- Regulatory retention policies and requirements

### 9. Configuration Parameters
**Required Configuration**:
- `retentionPeriodYears`: integer - Regulatory retention period - Default: 10
- `compressionLevel`: integer - Compression level (1-9) - Default: 6
- `encryptionEnabled`: boolean - Enable data encryption - Default: true

**Optional Configuration**:
- `archiveRedundancy`: integer - Number of archive copies - Default: 2
- `historicalDataDepth`: integer - Years of historical data to include - Default: 20

**Environment-Specific Configuration**:
- Development: Use test archive storage with shorter retention
- Production: Use production archive storage with full regulatory retention

### 10. Integration Points
**API Contracts**:
- Input: CounterpartyArchivalRequest with archival details and authorization
- Output: ArchivedCounterparty with archive location and metadata

**Data Exchange Formats**:
- **Compressed Archive**: Primary format for long-term storage
- **JSON Metadata**: Archive index and retrieval information

**Event Publishing**:
- **CounterpartyArchived**: Published when archival completes successfully
- **CounterpartyArchivalFailed**: Published when archival fails

**Event Consumption**:
- **CounterpartyDecommissioned**: Triggers counterparty archival workflow
- **RetentionPeriodExpired**: Triggers automatic archival for inactive counterparties
