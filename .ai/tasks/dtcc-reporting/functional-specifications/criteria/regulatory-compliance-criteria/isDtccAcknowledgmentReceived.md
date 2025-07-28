# isDtccAcknowledgmentReceived Criterion Specification

## 1. Component Overview
**Component Name**: isDtccAcknowledgmentReceived
**Component Type**: CyodaCriterion
**Business Domain**: Regulatory Compliance
**Purpose**: Evaluates whether DTCC acknowledgment has been received for submitted regulatory reports
**Workflow Context**: RegulatoryReportWorkflow (submitted state transitions)

## 2. Input Parameters
**Entity Type**: RegulatoryReport
**Required Fields**:
- `submissionStatus`: string - Current submission status
- `dtccResponse`: object - DTCC response details
- `submissionTimestamp`: ISO-8601 timestamp - When report was submitted
- `acknowledgmentTimestamp`: ISO-8601 timestamp - When acknowledgment received

**Optional Fields**:
- `dtccMessageId`: string - DTCC message identifier for acknowledgment
- `acknowledgmentDetails`: object - Detailed acknowledgment information
- `responseCode`: string - DTCC response code

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to evaluation context
- `calculationNodesTags`: "regulatory" - Tags for regulatory evaluation nodes
- `responseTimeoutMs`: 5000 - Maximum evaluation time (5 seconds)
- `context`: "dtcc-acknowledgment-check" - Evaluation context identifier

**Evaluation Context**:
- DTCC response code mappings
- Acknowledgment timeout thresholds
- Regulatory compliance requirements

## 3. Evaluation Logic
**Decision Algorithm**:
```
IF dtccResponse != null AND
   dtccResponse.status == "ACKNOWLEDGED" AND
   acknowledgmentTimestamp != null AND
   acknowledgmentTimestamp > submissionTimestamp THEN
    RETURN true
ELSE IF dtccResponse != null AND
        dtccResponse.status == "REJECTED" THEN
    RETURN false
ELSE IF submissionTimestamp + acknowledgmentTimeout < currentTime THEN
    RETURN false (timeout)
ELSE
    RETURN false (pending or unknown)
```

**Boolean Logic**:
- Primary evaluation based on DTCC response status
- Secondary evaluation validates acknowledgment timing
- Tertiary evaluation considers timeout scenarios
- Default behavior for pending acknowledgments

**Calculation Methods**:
- Response status validation
- Timestamp comparison for acknowledgment timing
- Timeout calculation based on submission time

## 4. Success Conditions
**Positive Evaluation Criteria**:
- **Acknowledgment Received**: dtccResponse.status equals "ACKNOWLEDGED"
- **Valid Response**: dtccResponse contains valid acknowledgment data
- **Proper Timing**: acknowledgmentTimestamp after submissionTimestamp
- **Complete Data**: All required acknowledgment fields present

**Success Scenarios**:
- **Standard Acknowledgment**: DTCC acknowledges report within normal timeframe
- **Delayed Acknowledgment**: DTCC acknowledges report after extended processing
- **Corrected Acknowledgment**: DTCC acknowledges after initial rejection and correction

## 5. Failure Conditions
**Negative Evaluation Criteria**:
- **No Response**: dtccResponse is null or empty
- **Rejection Received**: dtccResponse.status equals "REJECTED"
- **Timeout Exceeded**: No acknowledgment within expected timeframe
- **Invalid Response**: dtccResponse contains invalid or incomplete data

**Failure Scenarios**:
- **DTCC Rejection**: DTCC explicitly rejects the submitted report
- **Acknowledgment Timeout**: No response received within regulatory timeframe
- **System Failure**: DTCC system issues preventing acknowledgment
- **Invalid Submission**: Report submission failed or was not received

## 6. Edge Cases
**Boundary Conditions**:
- **Partial Acknowledgment**: Acknowledgment received but incomplete
- **Duplicate Acknowledgments**: Multiple acknowledgments for same report
- **Timestamp Issues**: Acknowledgment timestamp before submission timestamp
- **Status Ambiguity**: DTCC response status not clearly acknowledged or rejected

**Special Scenarios**:
- **System Maintenance**: DTCC system maintenance affecting acknowledgments
- **Regulatory Changes**: Changes in DTCC acknowledgment procedures
- **Emergency Processing**: Expedited acknowledgment for critical reports

**Data Absence Handling**:
- Missing dtccResponse defaults to false (no acknowledgment)
- Missing acknowledgmentTimestamp defaults to false (incomplete acknowledgment)
- Missing submissionTimestamp prevents proper evaluation

## 7. Performance Requirements
**SLA Requirements**:
- **Evaluation Time**: 2 seconds (95th percentile)
- **Throughput**: 1000 evaluations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Low intensity for timestamp and status evaluation
- **Memory**: 32MB per concurrent evaluation

## 8. Dependencies
**Internal Dependencies**:
- **DTCC Integration Service**: DTCC response processing and status tracking
- **Configuration Service**: Acknowledgment timeout and validation rules

**External Dependencies**:
- **DTCC GTR System**: Source of acknowledgment responses (monitored externally)

**Data Dependencies**:
- DTCC response code mappings
- Acknowledgment timeout configuration
- Regulatory compliance requirements

## 9. Configuration
**Configurable Thresholds**:
- `acknowledgmentTimeoutHours`: integer - Maximum time to wait for acknowledgment - Default: 24
- `validResponseCodes`: array - Valid DTCC response codes - Default: ["ACKNOWLEDGED", "ACCEPTED"]
- `retryEvaluationMinutes`: integer - Retry evaluation interval - Default: 60

**Evaluation Parameters**:
- `strictTiming`: boolean - Enable strict timestamp validation - Default: true
- `allowPartialAcknowledgment`: boolean - Accept partial acknowledgments - Default: false
- `timeoutGracePeriod`: integer - Grace period beyond timeout (hours) - Default: 2

**Environment-Specific Settings**:
- Development: Extended timeouts, relaxed validation
- Production: Standard timeouts, strict validation

## 10. Error Handling
**Evaluation Errors**:
- **DATA_UNAVAILABLE**: Required DTCC response data not accessible
- **EVALUATION_TIMEOUT**: Evaluation exceeded timeout limit
- **TIMESTAMP_ERROR**: Invalid or inconsistent timestamp data
- **SYSTEM_ERROR**: Technical evaluation failure

**Error Recovery**:
- Default to false on data unavailability (no acknowledgment confirmed)
- Retry mechanism for transient system errors (max 2 retries)
- Fallback to basic status checking for complex scenarios

**Error Propagation**:
- Evaluation errors logged with submission context
- Failed evaluations trigger manual review
- Critical errors escalated to regulatory reporting team
