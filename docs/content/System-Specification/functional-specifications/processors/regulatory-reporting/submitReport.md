# submitReport Processor Specification

## 1. Component Overview
**Component Name**: submitReport
**Component Type**: CyodaProcessor
**Business Domain**: Regulatory Reporting
**Purpose**: Submits validated regulatory reports to DTCC GTR system with proper authentication and tracking
**Workflow Context**: RegulatoryReportWorkflow (submitting state)

## 2. Input Specifications
**Entity Type**: RegulatoryReport
**Required Fields**:
- `reportId`: string - Unique report identifier
- `reportContent`: string - Validated report content (XML)
- `reportType`: string - Type of regulatory report
- `validationStatus`: string - Report validation status
- `reportingEntity`: string - LEI of the reporting entity

**Optional Fields**:
- `submissionPriority`: string - Submission priority level
- `submissionDeadline`: ISO-8601 timestamp - Regulatory submission deadline
- `retryAttempts`: integer - Number of previous submission attempts

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "submission" - Tags for submission processing nodes
- `responseTimeoutMs`: 60000 - Maximum processing time (60 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-423456789abe" - Process parameter reference

**Context Data**:
- DTCC GTR connection configuration
- Authentication credentials
- Submission tracking requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "RegulatoryReport",
    "processingTimestamp": "2024-01-15T15:00:00Z",
    "submissionResults": {
      "submissionSuccessful": true,
      "dtccSubmissionId": "DTCC-20240115-001",
      "submissionTimestamp": "2024-01-15T15:00:00Z",
      "acknowledgmentExpected": "2024-01-15T17:00:00Z"
    },
    "submissionMetadata": {
      "reportSize": 2048576,
      "submissionDuration": 45000,
      "dtccResponseCode": "ACCEPTED",
      "trackingReference": "TRK-20240115-001"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "SUBMISSION_ERROR",
  "errorMessage": "Report submission failed",
  "details": {
    "submissionErrors": ["DTCC system unavailable"],
    "authenticationErrors": ["Invalid credentials"],
    "formatErrors": ["Report format not accepted"],
    "systemErrors": ["Network timeout"]
  }
}
```

**Side Effects**:
- Updates RegulatoryReport entity submission status
- Creates submission audit trail
- Stores DTCC submission reference
- Publishes ReportSubmitted event

## 4. Business Logic
**Processing Steps**:
1. Validate report readiness for submission
2. Authenticate with DTCC GTR system
3. Format report for DTCC submission requirements
4. Submit report to DTCC GTR endpoint
5. Process DTCC submission response
6. Update submission tracking information
7. Schedule acknowledgment monitoring

**Business Rules**:
- **Validation Required**: Report must have successful validation status
- **Authentication**: Valid DTCC credentials required for submission
- **Format Compliance**: Report must be in DTCC-accepted format
- **Deadline Compliance**: Submission must meet regulatory deadlines
- **Tracking**: All submissions must be tracked for acknowledgment

**Algorithms**:
- DTCC authentication using OAuth 2.0 or API key
- Report formatting using DTCC submission templates
- Submission tracking using unique reference generation
- Retry logic with exponential backoff for failures

## 5. Validation Rules
**Pre-processing Validations**:
- **Validation Status**: Report has successful validation status
- **Content Format**: Report content in correct DTCC format
- **Authentication**: DTCC credentials valid and current

**Post-processing Validations**:
- **Submission Confirmation**: DTCC submission confirmation received
- **Reference Assignment**: DTCC submission reference assigned
- **Tracking Setup**: Acknowledgment tracking configured

**Data Quality Checks**:
- **Content Integrity**: Report content not corrupted during submission
- **Size Validation**: Report size within DTCC limits
- **Format Verification**: Submitted format matches DTCC requirements

## 6. Error Handling
**Error Categories**:
- **SUBMISSION_ERROR**: DTCC submission processing failures
- **AUTHENTICATION_ERROR**: DTCC authentication failures
- **FORMAT_ERROR**: Report format or content issues
- **NETWORK_ERROR**: Network connectivity or timeout issues
- **TIMEOUT_ERROR**: Submission processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient network errors (max 3 retries with backoff)
- Authentication refresh for credential expiration
- Format correction for minor format issues

**Error Propagation**:
- Submission errors trigger transition to submission-failed state
- Error details stored for manual review and resubmission
- Critical errors escalated to regulatory reporting team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 45 seconds (95th percentile)
- **Throughput**: 10 submissions per minute
- **Availability**: 99.9% uptime during reporting windows

**Resource Requirements**:
- **CPU**: Medium intensity for formatting and network operations
- **Memory**: 512MB per concurrent submission
- **I/O**: High for report content and network transmission

**Scalability**:
- Horizontal scaling through submission node distribution
- Performance limited by DTCC GTR system capacity
- Sequential processing for same entity submissions

## 8. Dependencies
**Internal Dependencies**:
- **Authentication Service**: DTCC credential management
- **Format Service**: Report formatting for DTCC submission
- **Tracking Service**: Submission tracking and monitoring
- **Audit Service**: Submission audit trail logging

**External Dependencies**:
- **DTCC GTR System**: Regulatory report submission endpoint (SLA: 99.5% availability, 30s response)
- **Authentication Provider**: DTCC authentication service (SLA: 99.9% availability, 5s response)

**Data Dependencies**:
- DTCC GTR connection configuration
- Authentication credentials and certificates
- Report formatting templates
- Submission tracking configuration

## 9. Configuration Parameters
**Required Configuration**:
- `dtccEndpointUrl`: string - DTCC GTR submission endpoint
- `authenticationMethod`: string - Authentication method - Default: "oauth2"
- `maxReportSizeMB`: integer - Maximum report size - Default: 100

**Optional Configuration**:
- `submissionTimeoutSeconds`: integer - Submission timeout - Default: 45
- `retryAttempts`: integer - Maximum retry attempts - Default: 3
- `retryDelaySeconds`: integer - Retry delay - Default: 30

**Environment-Specific Configuration**:
- Development: Mock DTCC endpoint, test credentials
- Production: Live DTCC endpoint, production credentials

## 10. Integration Points
**API Contracts**:
- Input: RegulatoryReport entity with validated content
- Output: Submission results with DTCC references

**Data Exchange Formats**:
- **XML**: DTCC GTR report submission format
- **JSON**: Submission results and metadata
- **HTTPS**: Secure submission protocol

**Event Publishing**:
- **ReportSubmitted**: Published on successful submission with DTCC reference
- **SubmissionFailed**: Published on submission failure with error details
- **SubmissionRetry**: Published when retry attempts initiated

**Event Consumption**:
- **ReportValidated**: Triggers report submission process
- **CredentialsUpdated**: Updates DTCC authentication credentials
- **SubmissionScheduled**: Triggers scheduled submission processing
