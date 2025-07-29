# generateAmendmentReport Processor Specification

## 1. Component Overview
**Component Name**: generateAmendmentReport
**Component Type**: CyodaProcessor
**Business Domain**: Amendment Processing
**Purpose**: Generates regulatory and internal reports for trade amendments, including DTCC GTR amendment reports and audit documentation
**Workflow Context**: AmendmentWorkflow (reporting state), RegulatoryReportingWorkflow (amendment reporting)

## 2. Input Specifications
**Entity Type**: TradeAmendment
**Required Fields**:
- `amendmentId`: string - Unique amendment request identifier
- `originalTradeId`: string - Original trade identifier that was amended
- `amendedTradeId`: string - Trade identifier after amendment
- `reportType`: string - Type of report (REGULATORY, AUDIT, INTERNAL)
- `reportingJurisdiction`: string - Regulatory jurisdiction (US, EU, UK)
- `effectiveDate`: ISO-8601 date - Amendment effective date

**Optional Fields**:
- `reportingPeriod`: object - Start and end dates for reporting period
- `includeImpactAnalysis`: boolean - Include impact analysis in report
- `customReportFields`: object - Additional fields for specific report types
- `distributionList`: array - Report distribution recipients

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "amendment-reporting" - Tags for amendment reporting nodes
- `responseTimeoutMs`: 90000 - Maximum processing time (90 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789stu" - Process parameter reference

**Context Data**:
- Amendment audit trail data
- Original and amended trade data
- Regulatory reporting templates
- Impact assessment results

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeAmendment",
    "processingTimestamp": "2024-01-15T16:00:00Z",
    "reportDetails": {
      "reportId": "AMD-RPT-2024-001",
      "reportType": "REGULATORY",
      "reportFormat": "XML",
      "reportSize": 1048576,
      "reportLocation": "/reports/amendments/2024/01/AMD-RPT-2024-001.xml",
      "regulatoryReference": "DTCC-AMD-2024-001"
    },
    "amendmentSummary": {
      "originalTradeId": "TRD-001",
      "amendedTradeId": "TRD-001",
      "amendmentType": "ECONOMIC",
      "fieldsChanged": ["notionalAmount", "maturityDate"],
      "effectiveDate": "2024-01-15",
      "approvalReference": "APPROVAL_2024_001"
    },
    "impactSummary": {
      "positionImpact": 2000000.00,
      "riskImpact": 15000.00,
      "regulatoryImpact": "REPORTING_REQUIRED",
      "counterpartyImpact": "CONSENT_OBTAINED"
    },
    "validationResults": {
      "schemaValidation": "PASSED",
      "businessRuleValidation": "PASSED",
      "regulatoryCompliance": "COMPLIANT"
    },
    "submissionDetails": {
      "readyForSubmission": true,
      "submissionDeadline": "2024-01-16T23:59:59Z",
      "submissionMethod": "DTCC_GTR"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "AMENDMENT_REPORT_GENERATION_FAILED",
  "errorMessage": "Amendment report generation failed due to missing audit trail data",
  "details": {
    "amendmentId": "AMD-001",
    "missingData": ["approval_details", "impact_assessment"],
    "reportType": "REGULATORY"
  }
}
```

**Side Effects**:
- Creates amendment report file in designated storage location
- Updates amendment entity with report generation status
- Publishes report ready event for submission workflow
- Creates report generation audit trail

## 4. Business Logic
**Processing Steps**:
1. Validate amendment report generation request
2. Retrieve amendment data and audit trail information
3. Gather original and amended trade data for comparison
4. Retrieve impact assessment and approval details
5. Determine report format and template based on jurisdiction
6. Generate amendment report with before/after comparison
7. Include regulatory compliance and impact analysis
8. Perform schema and business rule validation
9. Store report in designated location with metadata
10. Update audit trail and publish completion events

**Business Rules**:
- **Report Completeness**: All required amendment data must be present
- **Regulatory Compliance**: Reports must comply with jurisdiction requirements
- **Audit Trail**: Complete audit trail must be included in reports
- **Timing Requirements**: Reports must be generated within regulatory deadlines
- **Data Accuracy**: All reported data must be accurate and validated

**Algorithms**:
- Amendment data aggregation for comprehensive reporting
- Before/after comparison generation for impact visualization
- Regulatory format transformation based on jurisdiction
- Validation algorithms for schema and business rule compliance

## 5. Validation Rules
**Pre-processing Validations**:
- **Amendment Data Completeness**: All required amendment data must be available
- **Audit Trail Availability**: Complete audit trail must exist
- **Report Type Validation**: Report type must be valid for jurisdiction
- **Effective Date Validation**: Amendment effective date must be valid

**Post-processing Validations**:
- **Schema Validation**: Generated report must conform to regulatory schema
- **Business Rule Validation**: Report content must satisfy business rules
- **Data Consistency**: All reported values must be consistent
- **File Integrity**: Generated report file must be complete and uncorrupted

**Data Quality Checks**:
- **Amendment Accuracy**: All amendment details must be accurate
- **Impact Calculation**: Impact analysis must be mathematically correct
- **Reference Data Validation**: All reference data must be current

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Amendment data validation failures
- **DATA_INCOMPLETE_ERROR**: Required amendment data missing
- **FORMAT_ERROR**: Report formatting failures
- **SYSTEM_ERROR**: Technical system failures
- **TIMEOUT_ERROR**: Report generation timeout exceeded

**Error Recovery**:
- Retry mechanism for transient system failures
- Partial report generation for non-critical data issues
- Alternative format generation if primary format fails

**Error Propagation**:
- Detailed error information provided to calling workflows
- Amendment status updated with report generation failure
- Error notifications sent to operations teams

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 90000 milliseconds (95th percentile)
- **Throughput**: 80 reports per hour
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Moderate computational requirements for report generation
- **Memory**: 512MB for large amendment datasets and report formatting
- **I/O**: High disk I/O for report file creation and storage

**Scalability**:
- Horizontal scaling through report generation node distribution
- Performance optimization through template caching
- Parallel processing for large report generation

## 8. Dependencies
**Internal Dependencies**:
- **AmendmentService**: Amendment data and audit trail retrieval
- **TradeService**: Original and amended trade data
- **ReportTemplateService**: Amendment report templates
- **ValidationService**: Schema and business rule validation
- **StorageService**: Report file storage and management

**External Dependencies**:
- **Regulatory Schema Repository**: Current regulatory schemas (SLA: 99.9%)
- **DTCC GTR**: Regulatory reporting submission (SLA: 99.5%)

**Data Dependencies**:
- Amendment audit trail data
- Original and amended trade data
- Regulatory report templates
- Impact assessment results

## 9. Configuration Parameters
**Required Configuration**:
- `reportGenerationTimeoutMs`: integer - Maximum generation time - Default: 90000
- `reportStorageLocation`: string - Base path for report storage - Default: "/reports/amendments"
- `enableSchemaValidation`: boolean - Enable schema validation - Default: true
- `maxReportSizeMB`: integer - Maximum report size limit - Default: 50

**Optional Configuration**:
- `includeDetailedAudit`: boolean - Include detailed audit information - Default: true
- `reportRetentionDays`: integer - Report retention period - Default: 2555
- `compressionEnabled`: boolean - Enable report compression - Default: true

**Environment-Specific Configuration**:
- **Development**: Reduced validation and smaller report limits
- **Production**: Full validation and production report size limits

## 10. Integration Points
**API Contracts**:
- **Input**: Amendment report generation request with type and parameters
- **Output**: Generated amendment report with validation results and metadata

**Data Exchange Formats**:
- **XML**: Primary format for regulatory submissions
- **JSON**: Format for internal reporting and API consumption
- **PDF**: Format for human-readable audit reports

**Event Publishing**:
- **AmendmentReportGenerated**: Published when report generation completes successfully
- **AmendmentReportGenerationFailed**: Published when report generation fails

**Event Consumption**:
- **AmendmentApplied**: Triggers amendment report generation
- **RegulatoryReportingScheduled**: Triggers scheduled amendment reporting
