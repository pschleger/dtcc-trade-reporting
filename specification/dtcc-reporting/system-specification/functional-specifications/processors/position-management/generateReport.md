# generateReport Processor Specification

## 1. Component Overview
**Component Name**: generateReport
**Component Type**: CyodaProcessor
**Business Domain**: Position Management
**Purpose**: Generates position reports for regulatory submission and internal risk management
**Workflow Context**: PositionWorkflow (reporting state), RegulatoryReportingWorkflow (position report generation)

## 2. Input Specifications
**Entity Type**: Position
**Required Fields**:
- `positionId`: string - Unique position identifier
- `reportType`: string - Type of report (REGULATORY, RISK, INTERNAL)
- `reportingDate`: ISO-8601 date - Report generation date
- `counterpartyId`: string - Counterparty LEI identifier
- `productType`: string - Financial product type
- `currency`: string - Position currency (ISO 4217)
- `reportingJurisdiction`: string - Regulatory jurisdiction (US, EU, UK)

**Optional Fields**:
- `reportingPeriod`: object - Start and end dates for reporting period
- `aggregationLevel`: string - Level of aggregation (TRADE, COUNTERPARTY, PRODUCT)
- `includeRiskMetrics`: boolean - Include risk calculations in report
- `customFields`: object - Additional fields for specific report types

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "reporting" - Tags for report generation nodes
- `responseTimeoutMs`: 120000 - Maximum processing time (2 minutes)
- `processParamId`: "01932b4e-7890-7123-8456-323456789ghi" - Process parameter reference

**Context Data**:
- Position data for report generation
- Regulatory templates and formats
- Risk calculation parameters
- Report distribution configuration

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Position",
    "processingTimestamp": "2024-01-15T17:00:00Z",
    "reportDetails": {
      "reportId": "POS-RPT-2024-001",
      "reportType": "REGULATORY",
      "reportFormat": "XML",
      "reportSize": 2048576,
      "recordCount": 150,
      "reportLocation": "/reports/position/2024/01/POS-RPT-2024-001.xml"
    },
    "validationResults": {
      "schemaValidation": "PASSED",
      "businessRuleValidation": "PASSED",
      "dataQualityChecks": "PASSED",
      "warnings": []
    },
    "submissionDetails": {
      "readyForSubmission": true,
      "submissionDeadline": "2024-01-16T23:59:59Z",
      "regulatoryReference": "DTCC-POS-2024-001"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "REPORT_GENERATION_FAILED",
  "errorMessage": "Position report generation failed due to incomplete position data",
  "details": {
    "positionId": "POS-001",
    "missingFields": ["marketValue", "riskMetrics"],
    "reportType": "REGULATORY"
  }
}
```

**Side Effects**:
- Creates report file in designated storage location
- Updates report generation audit trail
- Publishes report ready event for submission workflow
- Updates position entity with report generation status

## 4. Business Logic
**Processing Steps**:
1. Validate report generation request and position data completeness
2. Retrieve position data and related trade information
3. Determine report format and template based on type and jurisdiction
4. Aggregate position data according to reporting requirements
5. Calculate required risk metrics and exposures
6. Apply regulatory formatting and validation rules
7. Generate report in specified format (XML, CSV, JSON)
8. Perform schema and business rule validation
9. Store report in designated location with metadata
10. Update audit trail and publish completion events

**Business Rules**:
- **Report Completeness**: All required fields must be present for regulatory reports
- **Data Currency**: Position data must be current as of reporting date
- **Regulatory Compliance**: Reports must comply with jurisdiction-specific requirements
- **Audit Trail**: All report generation activities must be audited
- **Data Quality**: Reports must pass validation before being marked ready

**Algorithms**:
- Position aggregation algorithms for different reporting levels
- Risk metric calculation for regulatory requirements
- Data transformation for regulatory format compliance
- Validation algorithms for schema and business rule compliance

## 5. Validation Rules
**Pre-processing Validations**:
- **Position Data Completeness**: All required position fields must be populated
- **Report Type Validation**: Report type must be valid for the jurisdiction
- **Date Validation**: Reporting date must be valid business date
- **Authorization**: User must have authorization to generate specified report type

**Post-processing Validations**:
- **Schema Validation**: Generated report must conform to regulatory schema
- **Business Rule Validation**: Report content must satisfy business rules
- **Data Consistency**: Aggregated values must reconcile with source data
- **File Integrity**: Generated report file must be complete and uncorrupted

**Data Quality Checks**:
- **Numerical Accuracy**: All calculations must be mathematically correct
- **Currency Consistency**: All monetary amounts in consistent currency
- **Reference Data Validation**: All reference data must be current and valid

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Input data validation failures
- **DATA_INCOMPLETE_ERROR**: Required position data missing
- **FORMAT_ERROR**: Report formatting failures
- **SYSTEM_ERROR**: Technical system failures
- **TIMEOUT_ERROR**: Report generation timeout exceeded

**Error Recovery**:
- Retry mechanism for transient system failures
- Partial report generation for non-critical data issues
- Alternative format generation if primary format fails

**Error Propagation**:
- Detailed error information provided to calling workflows
- Position state updated with report generation failure status
- Error notifications sent to operations teams

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 120000 milliseconds (95th percentile)
- **Throughput**: 100 reports per hour
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Moderate computational requirements for data aggregation
- **Memory**: 1GB for large position datasets and report generation
- **I/O**: High disk I/O for report file creation and storage

**Scalability**:
- Horizontal scaling through report generation node distribution
- Performance optimization through data caching
- Parallel processing for large report generation

## 8. Dependencies
**Internal Dependencies**:
- **PositionService**: Position data retrieval
- **ReportTemplateService**: Report format templates
- **ValidationService**: Schema and business rule validation
- **StorageService**: Report file storage and management
- **AuditService**: Report generation audit trail

**External Dependencies**:
- **Regulatory Schema Repository**: Current regulatory schemas (SLA: 99.9%)
- **Reference Data Service**: Current reference data (SLA: 99.5%)

**Data Dependencies**:
- Current position data
- Regulatory report templates
- Reference data for validation
- Risk calculation parameters

## 9. Configuration Parameters
**Required Configuration**:
- `reportGenerationTimeoutMs`: integer - Maximum generation time - Default: 120000
- `reportStorageLocation`: string - Base path for report storage - Default: "/reports/position"
- `enableSchemaValidation`: boolean - Enable schema validation - Default: true
- `maxReportSizeMB`: integer - Maximum report size limit - Default: 100

**Optional Configuration**:
- `enableParallelProcessing`: boolean - Enable parallel processing - Default: true
- `reportRetentionDays`: integer - Report retention period - Default: 2555
- `compressionEnabled`: boolean - Enable report compression - Default: true

**Environment-Specific Configuration**:
- **Development**: Reduced validation and smaller report limits
- **Production**: Full validation and production report size limits

## 10. Integration Points
**API Contracts**:
- **Input**: Position report generation request with type and parameters
- **Output**: Generated report with validation results and metadata

**Data Exchange Formats**:
- **XML**: Primary format for regulatory submissions
- **CSV**: Alternative format for internal reporting
- **JSON**: Format for API-based report consumption

**Event Publishing**:
- **ReportGenerated**: Published when report generation completes successfully
- **ReportGenerationFailed**: Published when report generation fails

**Event Consumption**:
- **PositionCalculated**: Triggers report generation for calculated positions
- **ReportingScheduled**: Triggers scheduled report generation
