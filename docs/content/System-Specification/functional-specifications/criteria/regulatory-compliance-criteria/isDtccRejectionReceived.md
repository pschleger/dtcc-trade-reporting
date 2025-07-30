# isDtccRejectionReceived Criterion Specification

## 1. Component Overview
**Component Name**: isDtccRejectionReceived
**Component Type**: CyodaCriterion
**Business Domain**: Regulatory Reporting
**Purpose**: Evaluates whether a DTCC rejection response has been received for a submitted regulatory report requiring correction and resubmission
**Workflow Context**: Regulatory reporting workflows requiring DTCC response processing and rejection handling procedures

## 2. Input Parameters
**Entity Type**: Entity
**Required Fields**:
- `reportId`: string - Unique regulatory report identifier
- `submissionStatus`: string - Current submission status
- `dtccResponseStatus`: string - DTCC response status (ACKNOWLEDGED, REJECTED, PENDING)
- `responseTimestamp`: ISO-8601 timestamp - DTCC response timestamp

**Optional Fields**:
- `rejectionCode`: string - DTCC rejection code
- `rejectionReason`: string - Detailed rejection reason from DTCC
- `correctionRequired`: boolean - Whether correction is required
- `resubmissionDeadline`: ISO-8601 timestamp - Deadline for resubmission

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "dtcc-response" - Tags for DTCC response evaluation nodes
- `responseTimeoutMs`: 3000 - Maximum evaluation time (3 seconds)
- `context`: "dtcc-rejection-check" - Evaluation context identifier

**Evaluation Context**:
- DTCC response processing and validation
- Rejection code interpretation and handling
- Resubmission deadline management

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF reportId == null OR reportId.isEmpty() THEN
    RETURN false
END IF

IF dtccResponseStatus == "REJECTED" AND
   responseTimestamp != null THEN
    
    IF rejectionCode != null AND !rejectionCode.isEmpty() THEN
        RETURN true
    ELSE IF rejectionReason != null AND !rejectionReason.isEmpty() THEN
        RETURN true
    ELSE
        RETURN true  // Rejection status is sufficient
    END IF
ELSE
    RETURN false
END IF
```

**Boolean Logic**:
- Primary evaluation checks DTCC response status for rejection
- Secondary evaluation validates response timestamp presence
- Tertiary evaluation considers rejection details for completeness
- Report identifier validation for context
- Comprehensive rejection detection across multiple indicators

**Calculation Methods**:
- DTCC response status enumeration validation
- Response timestamp validation and format checking
- Rejection code and reason validation
- Report identifier format validation

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Rejection Status**: dtccResponseStatus equals "REJECTED"
- **Response Received**: responseTimestamp is present and valid
- **Valid Report ID**: reportId is present and valid
- **Rejection Details**: rejectionCode or rejectionReason provided (optional)

**Success Scenarios**:
- **Standard Rejection**: DTCC rejection with code and reason
- **Simple Rejection**: DTCC rejection with minimal details
- **Detailed Rejection**: DTCC rejection with complete error information
- **Timely Rejection**: DTCC rejection received within expected timeframe

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Rejection**: dtccResponseStatus not equal to "REJECTED"
- **Acknowledged**: dtccResponseStatus equals "ACKNOWLEDGED"
- **Pending Response**: dtccResponseStatus equals "PENDING"
- **Missing Response**: responseTimestamp is null or invalid
- **Invalid Report ID**: reportId is null or empty

**Failure Scenarios**:
- **Successful Submission**: DTCC acknowledged the report
- **Pending Response**: DTCC response not yet received
- **No Response**: No DTCC response received
- **Invalid Report**: Report identifier not valid
- **System Error**: Response processing error

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Rejection**: Some parts rejected, others accepted
- **Delayed Response**: DTCC response received after expected timeframe
- **Duplicate Responses**: Multiple DTCC responses for same report
- **Response Format**: Non-standard DTCC response format

**Special Scenarios**:
- **System Rejection**: Technical rejection vs business rejection
- **Timeout Rejection**: Rejection due to submission timeout
- **Format Rejection**: Rejection due to format errors
- **Data Rejection**: Rejection due to data validation errors
- **Emergency Rejection**: Urgent rejection requiring immediate action

**Data Absence Handling**:
- Missing reportId defaults to false evaluation
- Missing dtccResponseStatus defaults to "PENDING"
- Missing responseTimestamp defaults to false evaluation
- Missing rejection details are acceptable for basic rejection detection

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 400 milliseconds (95th percentile)
- **Throughput**: 2500 evaluations per second
- **Availability**: 99.95% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for status validation and string processing
- **Memory**: 10MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **DTCC Integration Service**: DTCC response processing and validation
- **Report Service**: Report status tracking and management
- **Notification Service**: Rejection notification and alerting

**External Dependencies**:
- **DTCC GTR**: DTCC Global Trade Repository response system
- **Message Queue**: DTCC response message processing

**Data Dependencies**:
- DTCC response message data
- Report submission tracking data
- Rejection code reference data
- Response timestamp validation

## 9. Configuration
**Configurable Thresholds**:
- `requireRejectionDetails`: boolean - Require rejection code or reason - Default: false
- `validateResponseTiming`: boolean - Validate response timing - Default: true
- `maxResponseDelayHours`: integer - Maximum expected response delay - Default: 24

**Evaluation Parameters**:
- `strictStatusValidation`: boolean - Enable strict status validation - Default: true
- `allowPartialRejection`: boolean - Allow partial rejection detection - Default: false
- `requireTimestamp`: boolean - Require response timestamp - Default: true

**Environment-Specific Settings**:
- Development: Relaxed validation for testing
- Production: Strict DTCC response validation

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required response fields not accessible
- **INVALID_STATUS**: Response status field validation errors
- **TIMESTAMP_ERROR**: Response timestamp validation failures
- **SERVICE_ERROR**: DTCC integration service failures
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit

**Error Recovery**:
- Default to false evaluation on data errors
- Fallback to basic status check on service errors
- Skip timestamp validation on timing errors
- Retry mechanism for service failures (max 2 retries)

**Error Propagation**:
- Evaluation errors logged with report context
- Failed evaluations trigger manual review
- Service errors escalated to operations team
- DTCC integration errors reported to regulatory compliance team
