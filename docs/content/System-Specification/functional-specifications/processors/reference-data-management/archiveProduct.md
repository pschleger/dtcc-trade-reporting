# archiveProduct Processor Specification

### 1. Component Overview
**Component Name**: archiveProduct
**Component Type**: CyodaProcessor
**Business Domain**: Reference Data Management
**Purpose**: Archives deprecated products for regulatory compliance while preserving historical trade relationships and specifications
**Workflow Context**: Final step in product lifecycle management after deprecation and trade transition completion

### 2. Input Specifications
**Entity Type**: ProductArchivalRequest
**Required Fields**:
- `productId`: string - Unique identifier of the deprecated product to archive
- `archivalReason`: string - Business reason for archival
- `finalStatus`: string - Final product status before archival (DEPRECATED, DISCONTINUED)
- `authorizingUser`: string - User authorizing the archival
- `requestTimestamp`: ISO-8601 timestamp - When archival was requested

**Optional Fields**:
- `effectiveDate`: ISO-8601 timestamp - When archival should take effect
- `retentionPeriod`: integer - Custom retention period in years
- `historicalTradeCount`: integer - Number of historical trades using this product
- `supportingDocuments`: array - Documentation supporting archival decision

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 25000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Complete product historical data and specifications
- Historical trade relationships and usage patterns
- Regulatory retention requirements for product data

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "ArchivedProduct",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "archiveId": "ARCH-PROD-20240115-001",
      "archiveLocation": "s3://regulatory-archive/products/2024/01/",
      "retentionPeriodYears": 15,
      "historicalTradesArchived": 2500,
      "checksumMD5": "a1b2c3d4e5f6789012345678901234567890"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "ARCHIVAL_BLOCKED_ACTIVE_TRADES",
  "errorMessage": "Product archival blocked due to remaining active trades",
  "details": {
    "activeTrades": 5,
    "oldestTradeDate": "2023-12-01T00:00:00Z",
    "requiredActions": ["Complete trade transitions", "Verify all trades closed"]
  }
}
```

**Side Effects**:
- Moves product data to long-term regulatory archive storage
- Updates product status to ARCHIVED in operational database
- Preserves historical trade relationships for regulatory reporting
- Removes product from active operational systems

### 4. Business Logic
**Processing Steps**:
1. Validate archival request and verify product eligibility
2. Confirm no active trades remain using the product
3. Compile complete product data package including historical specifications
4. Verify regulatory retention requirements and compliance
5. Create compressed and encrypted archive package
6. Transfer package to regulatory archive storage with redundancy
7. Update operational systems to reflect archived status
8. Generate archival confirmation and audit trail

**Business Rules**:
- **No Active Trades**: All trades using the product must be closed or transitioned
- **Retention Period**: Minimum 15-year retention for regulatory compliance
- **Data Completeness**: All historical specifications and relationships must be preserved
- **Access Control**: Archived data access must be logged and authorized
- **Regulatory Compliance**: Archive must support regulatory examination and discovery

**Algorithms**:
- Active trade verification across all trading systems
- Historical data compilation and specification versioning
- Data compression and encryption for long-term storage

### 5. Validation Rules
**Pre-processing Validations**:
- **Product Status**: Product must be in DEPRECATED or DISCONTINUED status
- **Trade Closure**: No active trades using the product
- **Authorization Level**: User must have sufficient authority for archival
- **Regulatory Compliance**: All regulatory requirements must be satisfied

**Post-processing Validations**:
- **Archive Integrity**: Verify successful storage and data integrity
- **Historical Preservation**: Confirm all historical data preserved
- **Access Verification**: Validate archive retrieval mechanisms
- **Cleanup Confirmation**: Verify removal from operational systems

**Data Quality Checks**:
- Completeness verification of all product-related records
- Consistency check between archived data and operational records
- Validation of encryption and compression success

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Active trades remaining or incomplete data
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
- **Response Time**: 12000 milliseconds (95th percentile)
- **Throughput**: 25 archives per hour
- **Availability**: 99.5% uptime

**Resource Requirements**:
- **CPU**: High intensity for data compilation and compression
- **Memory**: 1GB per concurrent archive operation
- **I/O**: Very high for historical data transfers

**Scalability**:
- Horizontal scaling through distributed archive processing nodes
- Performance scales with historical data volume and specification complexity

### 8. Dependencies
**Internal Dependencies**:
- **Product Repository**: Complete product data and specifications
- **Trade Repository**: Historical trade data and relationships
- **Archive Management System**: Long-term storage and retrieval

**External Dependencies**:
- **Archive Storage System**: Long-term regulatory storage (SLA: 99.9%)
- **Encryption Service**: Data encryption and key management (SLA: 99.95%)
- **Regulatory Database**: Compliance verification (SLA: 99.5%)

**Data Dependencies**:
- Complete product master data and historical specifications
- All historical trade data using the product
- Regulatory retention policies and requirements

### 9. Configuration Parameters
**Required Configuration**:
- `retentionPeriodYears`: integer - Regulatory retention period - Default: 15
- `compressionLevel`: integer - Compression level (1-9) - Default: 6
- `encryptionEnabled`: boolean - Enable data encryption - Default: true

**Optional Configuration**:
- `archiveRedundancy`: integer - Number of archive copies - Default: 2
- `historicalDataDepth`: integer - Years of historical data to include - Default: 25

**Environment-Specific Configuration**:
- Development: Use test archive storage with shorter retention
- Production: Use production archive storage with full regulatory retention

### 10. Integration Points
**API Contracts**:
- Input: ProductArchivalRequest with archival details and authorization
- Output: ArchivedProduct with archive location and metadata

**Data Exchange Formats**:
- **Compressed Archive**: Primary format for long-term storage
- **JSON Metadata**: Archive index and retrieval information

**Event Publishing**:
- **ProductArchived**: Published when archival completes successfully
- **ProductArchivalFailed**: Published when archival fails

**Event Consumption**:
- **ProductDeprecationCompleted**: Triggers product archival workflow
- **RetentionPeriodExpired**: Triggers automatic archival for deprecated products
