# correctReport Processor Specification

## 1. Component Overview
**Component Name**: correctReport
**Component Type**: CyodaProcessor
**Business Domain**: Regulatory Reporting
**Purpose**: Corrects rejected regulatory reports based on DTCC feedback and resubmits for compliance
**Workflow Context**: RegulatoryReportWorkflow (correcting state)

## 2. Input Specifications
**Entity Type**: RegulatoryReport
**Required Fields**:
- `reportId`: string - Unique report identifier
- `originalReportContent`: string - Original rejected report content
- `dtccRejectionDetails`: object - DTCC rejection feedback and error details
- `rejectionTimestamp`: ISO-8601 timestamp - When rejection was received
- `correctionInstructions`: object - Instructions for report correction

**Optional Fields**:
- `correctionPriority`: string - Priority level for correction processing
- `correctionDeadline`: ISO-8601 timestamp - Deadline for corrected submission
- `manualCorrections`: object - Manual correction overrides

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "correction" - Tags for correction processing nodes
- `responseTimeoutMs`: 60000 - Maximum processing time (60 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-423456789abf" - Process parameter reference

**Context Data**:
- DTCC error code mappings
- Correction rule configuration
- Report template updates

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "RegulatoryReport",
    "processingTimestamp": "2024-01-15T15:30:00Z",
    "correctionResults": {
      "reportCorrected": true,
      "correctedReportContent": "corrected-xml-content",
      "correctionsApplied": 3,
      "validationPassed": true
    },
    "correctionMetadata": {
      "correctionDuration": 45000,
      "errorsCorrected": ["LEI format", "Date format", "Amount precision"],
      "correctionMethod": "automated",
      "revalidationRequired": true
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "CORRECTION_ERROR",
  "errorMessage": "Report correction failed",
  "details": {
    "correctionErrors": ["Unable to parse DTCC feedback"],
    "validationErrors": ["Corrected report still invalid"],
    "dataErrors": ["Source data unavailable for correction"],
    "systemErrors": ["Correction service timeout"]
  }
}
```

**Side Effects**:
- Updates RegulatoryReport entity with corrected content
- Creates correction audit trail
- Triggers revalidation of corrected report
- Publishes ReportCorrected event

## 4. Business Logic
**Processing Steps**:
1. Parse DTCC rejection feedback and error details
2. Identify specific errors and correction requirements
3. Apply automated corrections based on error types
4. Retrieve updated source data if required
5. Regenerate affected report sections
6. Validate corrected report against DTCC requirements
7. Prepare corrected report for resubmission

**Business Rules**:
- **Error Analysis**: All DTCC errors must be addressed in correction
- **Data Currency**: Use most current data available for corrections
- **Validation Required**: Corrected report must pass full validation
- **Audit Trail**: Complete correction audit trail required
- **Deadline Compliance**: Corrections must meet regulatory deadlines

**Algorithms**:
- DTCC error code parsing and categorization
- Automated correction application using rule engine
- Data refresh and report regeneration
- Validation using updated DTCC schemas

## 5. Validation Rules
**Pre-processing Validations**:
- **Rejection Details**: DTCC rejection feedback complete and parseable
- **Original Report**: Original report content accessible
- **Correction Rules**: Correction rules available for identified errors

**Post-processing Validations**:
- **Error Resolution**: All identified errors addressed
- **Report Validity**: Corrected report passes DTCC validation
- **Data Consistency**: Corrected data internally consistent

**Data Quality Checks**:
- **Correction Completeness**: All required corrections applied
- **Data Accuracy**: Corrected data accurate and current
- **Format Compliance**: Corrected report meets DTCC format requirements

## 6. Error Handling
**Error Categories**:
- **CORRECTION_ERROR**: Report correction logic failures
- **VALIDATION_ERROR**: Corrected report validation failures
- **DATA_ERROR**: Source data access or quality issues
- **PARSING_ERROR**: DTCC feedback parsing failures
- **TIMEOUT_ERROR**: Correction processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient data access errors (max 3 retries)
- Manual correction queue for complex error scenarios
- Escalation to regulatory reporting team for unresolvable errors

**Error Propagation**:
- Correction errors trigger transition to correction-failed state
- Error details stored for manual review and intervention
- Critical errors escalated to regulatory reporting team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 45 seconds (95th percentile)
- **Throughput**: 15 corrections per minute
- **Availability**: 99.9% uptime during reporting windows

**Resource Requirements**:
- **CPU**: High intensity for parsing, correction, and validation
- **Memory**: 1GB per concurrent correction
- **I/O**: High for report content and data access

**Scalability**:
- Horizontal scaling through correction processing nodes
- Performance varies with correction complexity and report size
- Parallel processing for independent correction components

## 8. Dependencies
**Internal Dependencies**:
- **Validation Service**: Report validation and schema checking
- **Data Service**: Source data access for corrections
- **Template Service**: Updated report templates
- **Configuration Service**: Correction rule configuration

**External Dependencies**:
- **DTCC GTR Service**: Current validation schemas and error codes (SLA: 99.5% availability, 3s response)
- **Market Data Service**: Current market data for corrections (SLA: 99.5% availability, 2s response)

**Data Dependencies**:
- DTCC error code mappings and correction rules
- Current report templates and schemas
- Source trade and position data
- Validation rule configuration

## 9. Configuration Parameters
**Required Configuration**:
- `enableAutomaticCorrection`: boolean - Enable automatic correction - Default: true
- `correctionTimeoutMinutes`: integer - Correction processing timeout - Default: 1
- `revalidationRequired`: boolean - Require revalidation after correction - Default: true

**Optional Configuration**:
- `maxCorrectionAttempts`: integer - Maximum correction attempts - Default: 3
- `manualReviewThreshold`: integer - Threshold for manual review - Default: 5
- `correctionAuditEnabled`: boolean - Enable detailed correction audit - Default: true

**Environment-Specific Configuration**:
- Development: Mock DTCC feedback, relaxed validation
- Production: Live DTCC feedback, strict validation

## 10. Integration Points
**API Contracts**:
- Input: RegulatoryReport entity with DTCC rejection details
- Output: Correction results with corrected report content

**Data Exchange Formats**:
- **XML**: DTCC report format for corrections
- **JSON**: Correction metadata and results

**Event Publishing**:
- **ReportCorrected**: Published on successful correction with details
- **CorrectionFailed**: Published on correction failure with error details
- **ManualReviewRequired**: Published when manual intervention needed

**Event Consumption**:
- **ReportRejected**: Triggers report correction process
- **CorrectionRulesUpdated**: Updates correction rule configuration
- **SchemaUpdated**: Updates validation schemas for corrections
