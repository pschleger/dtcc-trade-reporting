# generateReport Processor Specification

## 1. Component Overview
**Component Name**: generateReport
**Component Type**: CyodaProcessor
**Business Domain**: Regulatory Reporting
**Purpose**: Generates regulatory reports for submission to DTCC GTR based on trade and position data
**Workflow Context**: RegulatoryReportWorkflow (generating state)

## 2. Input Specifications
**Entity Type**: RegulatoryReport
**Required Fields**:
- `reportType`: string - Type of regulatory report (e.g., "TRADE_REPORT", "POSITION_REPORT")
- `reportingDate`: ISO-8601 date - Business date for the report
- `reportingEntity`: string - LEI of the reporting entity
- `sourceData`: object - Trade or position data for report generation
- `regulatoryRequirements`: object - Specific regulatory requirements and rules

**Optional Fields**:
- `reportingPeriod`: object - Start and end dates for period reports
- `amendmentFlag`: boolean - Indicates if this is an amendment to previous report
- `originalReportId`: string - Reference to original report if amendment
- `customFields`: object - Additional fields for specific report types

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "reporting" - Tags for report generation nodes
- `responseTimeoutMs`: 120000 - Maximum processing time (2 minutes)
- `processParamId`: "01932b4e-7890-7123-8456-423456789abc" - Process parameter reference

**Context Data**:
- DTCC GTR reporting schemas and templates
- Regulatory rule configuration
- Entity master data for report population

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "RegulatoryReport",
    "processingTimestamp": "2024-01-15T14:30:00Z",
    "generatedReport": {
      "reportId": "RPT-20240115-001",
      "reportContent": "base64-encoded-xml-content",
      "reportFormat": "DTCC_GTR_XML_v2.1",
      "recordCount": 1250,
      "reportSize": 2048576
    },
    "generationMetadata": {
      "sourceRecords": 1250,
      "processingDuration": 45000,
      "dataQualityScore": 0.99,
      "validationStatus": "PASSED"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "GENERATION_ERROR",
  "errorMessage": "Report generation failed",
  "details": {
    "generationErrors": ["Missing required trade data"],
    "dataQualityIssues": ["Invalid counterparty LEI codes"],
    "systemErrors": ["Template service unavailable"]
  }
}
```

**Side Effects**:
- Creates report content in specified format
- Updates RegulatoryReport entity with generation status
- Stores generated report in secure repository
- Publishes ReportGenerated event for downstream processing

## 4. Business Logic
**Processing Steps**:
1. Retrieve source data based on report type and date range
2. Apply regulatory filtering and aggregation rules
3. Transform data to DTCC GTR required format
4. Populate report template with transformed data
5. Apply business logic for regulatory calculations
6. Validate generated report against DTCC schema
7. Compress and encode report content for storage
8. Generate report metadata and quality metrics

**Business Rules**:
- **Data Completeness**: All required fields must be populated per DTCC requirements
- **Data Accuracy**: Amounts and dates must be accurate and consistent
- **Regulatory Compliance**: Reports must comply with current DTCC GTR rules
- **Timeliness**: Reports generated within regulatory deadlines
- **Data Privacy**: Sensitive data masked or excluded per privacy requirements

**Algorithms**:
- Position aggregation using trade-level data
- Regulatory calculation engine for derived fields
- Data transformation using configurable mapping rules
- Report compression using industry-standard algorithms

## 5. Validation Rules
**Pre-processing Validations**:
- **Source Data**: Required source data available and accessible
- **Date Range**: Reporting date range valid and within acceptable bounds
- **Entity Status**: Reporting entity active and authorized for reporting

**Post-processing Validations**:
- **Schema Compliance**: Generated report validates against DTCC schema
- **Data Integrity**: Report data internally consistent and complete
- **Size Limits**: Report size within DTCC submission limits

**Data Quality Checks**:
- **Completeness**: All required data elements present
- **Accuracy**: Data values within expected ranges and formats
- **Consistency**: Cross-field validation and referential integrity

## 6. Error Handling
**Error Categories**:
- **GENERATION_ERROR**: Report generation logic failures
- **DATA_ERROR**: Source data quality or availability issues
- **VALIDATION_ERROR**: Generated report validation failures
- **SYSTEM_ERROR**: Template service or storage system failures
- **TIMEOUT_ERROR**: Report generation timeout exceeded

**Error Recovery**:
- Retry mechanism for transient system errors (max 3 retries)
- Partial report generation for non-critical data elements
- Fallback to previous day's template for template service failures

**Error Propagation**:
- Generation errors trigger transition to generation-failed state
- Error details stored for manual review and correction
- Critical errors escalated to regulatory reporting team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 90 seconds (95th percentile)
- **Throughput**: 10 reports per minute
- **Availability**: 99.95% uptime during reporting windows

**Resource Requirements**:
- **CPU**: High intensity for data aggregation and transformation
- **Memory**: 4GB per concurrent report generation
- **I/O**: Very high for large dataset processing and report storage

**Scalability**:
- Horizontal scaling through calculation node distribution
- Performance varies significantly with report size and complexity
- Parallel processing for large position reports

## 8. Dependencies
**Internal Dependencies**:
- **Data Service**: Trade and position source data
- **Template Service**: DTCC report templates and schemas
- **Storage Service**: Secure report repository
- **Validation Service**: Report validation and quality checks

**External Dependencies**:
- **DTCC GTR Service**: Schema definitions and validation rules (cached locally)
- **Regulatory Rule Engine**: Current regulatory requirements (daily updates)

**Data Dependencies**:
- Trade and position master data
- DTCC GTR reporting schemas (version 2.1)
- Regulatory rule configuration
- Entity master data for report headers

## 9. Configuration Parameters
**Required Configuration**:
- `reportTemplateVersion`: string - DTCC template version - Default: "2.1"
- `maxReportSizeMB`: integer - Maximum report size - Default: 100
- `dataQualityThreshold`: decimal - Minimum data quality - Default: 0.98

**Optional Configuration**:
- `enableCompression`: boolean - Enable report compression - Default: true
- `parallelProcessing`: boolean - Enable parallel processing - Default: true
- `retentionDays`: integer - Report retention period - Default: 2555 (7 years)

**Environment-Specific Configuration**:
- Development: Smaller datasets, relaxed quality thresholds
- Production: Full datasets, strict quality requirements

## 10. Integration Points
**API Contracts**:
- Input: RegulatoryReport entity with source data references
- Output: Generated report content with metadata

**Data Exchange Formats**:
- **XML**: DTCC GTR report format
- **JSON**: Internal metadata format
- **Base64**: Encoded report content for storage

**Event Publishing**:
- **ReportGenerated**: Published on successful generation with report metadata
- **GenerationFailed**: Published on generation failure with error details
- **DataQualityAlert**: Published when data quality below threshold

**Event Consumption**:
- **GenerationRequested**: Triggers report generation process
- **SourceDataUpdated**: Updates cached source data for report generation
