# generateCancellationReport Processor Specification

### 1. Component Overview
**Component Name**: generateCancellationReport
**Component Type**: CyodaProcessor
**Business Domain**: Cancellation Processing
**Purpose**: Generates regulatory reports for trade cancellations to ensure compliance with DTCC reporting requirements
**Workflow Context**: Used in cancellation workflows after successful trade cancellation execution

### 2. Input Specifications
**Entity Type**: CancelledTrade
**Required Fields**:
- `tradeId`: string - Unique identifier of the cancelled trade
- `cancellationId`: string - Unique identifier of the cancellation request
- `originalTrade`: Trade - Complete original trade details
- `cancellationReason`: string - Business reason for cancellation
- `cancellationTimestamp`: ISO-8601 timestamp - When cancellation was executed
- `authorizingUser`: string - User who authorized the cancellation
- `impactAssessment`: ImpactAssessment - Assessment of cancellation impact

**Optional Fields**:
- `regulatoryReferences`: array - Related regulatory reference numbers
- `counterpartyNotifications`: array - Counterparty notification records

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 30000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Regulatory reporting templates and schemas
- DTCC submission endpoints and credentials
- Report formatting and validation rules

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "CancellationReport",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "reportId": "CANC-RPT-20240115-001",
      "dtccSubmissionId": "DTCC-CANC-789012",
      "reportFormat": "XML",
      "reportSize": 2048,
      "validationStatus": "PASSED"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "REPORT_GENERATION_FAILED",
  "errorMessage": "Failed to generate cancellation report due to missing required data",
  "details": {
    "missingFields": ["originalTrade.productId"],
    "validationErrors": ["Invalid cancellation reason code"]
  }
}
```

**Side Effects**:
- Creates cancellation report record in reporting database
- Generates XML/JSON report file for DTCC submission
- Updates cancellation workflow status
- Triggers report validation workflow

### 4. Business Logic
**Processing Steps**:
1. Validate input cancellation data completeness and accuracy
2. Retrieve original trade details and regulatory classification
3. Apply DTCC cancellation reporting templates based on product type
4. Generate structured report with all required regulatory fields
5. Perform report validation against DTCC schemas
6. Store report in regulatory reporting database
7. Prepare report for submission to DTCC GTR

**Business Rules**:
- **Mandatory Reporting**: All derivative trade cancellations must be reported to DTCC within T+1
- **Report Completeness**: Report must include original trade details, cancellation reason, and impact assessment
- **Regulatory Classification**: Report format depends on product type (IRS, CDS, FX, etc.)
- **Counterparty Information**: Both counterparty LEIs must be included in cancellation report
- **Timing Requirements**: Cancellation reports must be submitted within regulatory deadlines

**Algorithms**:
- DTCC report template selection based on product taxonomy
- Regulatory field mapping from internal trade model to DTCC schema
- Report validation using DTCC-provided XSD schemas

### 5. Validation Rules
**Pre-processing Validations**:
- **Trade Existence**: Original trade must exist and be in CANCELLED status
- **Cancellation Authorization**: Cancellation must be properly authorized
- **Data Completeness**: All required fields for reporting must be present
- **Regulatory Classification**: Product must have valid regulatory classification

**Post-processing Validations**:
- **Schema Compliance**: Generated report must pass DTCC schema validation
- **Business Rule Compliance**: Report must satisfy all DTCC business rules
- **Data Consistency**: Report data must be consistent with original trade and cancellation

**Data Quality Checks**:
- LEI validation for all counterparties
- Product identifier validation against reference data
- Timestamp format and timezone compliance

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input data validation failures or schema violations
- **PROCESSING_ERROR**: Report generation or template processing failures
- **SYSTEM_ERROR**: Database or external system connectivity issues
- **TIMEOUT_ERROR**: Report generation exceeded timeout limits

**Error Recovery**:
- Retry report generation up to 3 times with exponential backoff
- Fallback to manual report generation workflow for critical failures
- Automatic escalation to operations team for persistent failures

**Error Propagation**:
- Return detailed error information to calling cancellation workflow
- Log all errors with correlation IDs for troubleshooting
- Trigger alert notifications for regulatory deadline risks

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 5000 milliseconds (95th percentile)
- **Throughput**: 100 reports per minute
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity for XML/JSON generation and validation
- **Memory**: 512MB per concurrent report generation
- **I/O**: High for database writes and file system operations

**Scalability**:
- Horizontal scaling through distributed processing nodes
- Performance degrades linearly with report complexity and size

### 8. Dependencies
**Internal Dependencies**:
- **Trade Repository**: Access to original trade data and cancellation records
- **Reference Data Service**: Product taxonomy and regulatory classification data
- **Regulatory Reporting Database**: Storage for generated reports

**External Dependencies**:
- **DTCC GTR**: Schema validation and submission endpoints (SLA: 99.5%)
- **LEI Database**: Legal Entity Identifier validation (SLA: 99.0%)

**Data Dependencies**:
- DTCC reporting templates and schemas
- Product reference data with regulatory classifications
- Counterparty master data with LEI information

### 9. Configuration Parameters
**Required Configuration**:
- `dtccSchemaVersion`: string - DTCC schema version for validation - Default: "2.0"
- `reportFormat`: string - Output format (XML/JSON) - Default: "XML"
- `regulatoryDeadlineHours`: integer - Hours until regulatory deadline - Default: 24

**Optional Configuration**:
- `includeAttachments`: boolean - Include supporting documents - Default: false
- `compressionEnabled`: boolean - Enable report compression - Default: true

**Environment-Specific Configuration**:
- Development: Use DTCC test schemas and sandbox endpoints
- Production: Use DTCC production schemas and live endpoints

### 10. Integration Points
**API Contracts**:
- Input: CancelledTrade entity with complete cancellation details
- Output: CancellationReport entity with DTCC submission metadata

**Data Exchange Formats**:
- **XML**: Primary format for DTCC submission
- **JSON**: Internal processing and API responses

**Event Publishing**:
- **CancellationReportGenerated**: Published when report generation completes successfully
- **CancellationReportFailed**: Published when report generation fails

**Event Consumption**:
- **CancellationExecuted**: Triggers cancellation report generation workflow
