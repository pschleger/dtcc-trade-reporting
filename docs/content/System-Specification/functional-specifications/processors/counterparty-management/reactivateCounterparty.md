# reactivateCounterparty Processor Specification

### 1. Component Overview
**Component Name**: reactivateCounterparty
**Component Type**: CyodaProcessor
**Business Domain**: Counterparty Management
**Purpose**: Reactivates suspended counterparty after issue resolution with proper verification and authorization
**Workflow Context**: Used in counterparty lifecycle management workflows for reactivation after suspension

### 2. Input Specifications
**Entity Type**: CounterpartyReactivationRequest
**Required Fields**:
- `counterpartyId`: string - Unique identifier of the suspended counterparty
- `reactivationReason`: string - Business reason for reactivation
- `issueResolution`: string - Description of how original suspension issue was resolved
- `authorizingUser`: string - User authorizing the reactivation
- `requestTimestamp`: ISO-8601 timestamp - When reactivation was requested

**Optional Fields**:
- `effectiveDate`: ISO-8601 timestamp - When reactivation should take effect
- `tradingLimits`: TradingLimits - New or modified trading limits post-reactivation
- `monitoringPeriod`: integer - Enhanced monitoring period in days
- `supportingDocuments`: array - Documentation supporting reactivation decision

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 20000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Original suspension details and reasons
- Current counterparty risk assessment and status
- Resolution verification and compliance checks

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "ReactivatedCounterparty",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "reactivationId": "REACT-CP-20240115-001",
      "effectiveTimestamp": "2024-01-15T10:30:00Z",
      "newTradingLimits": {
        "maxNotional": 10000000,
        "maxTrades": 100
      },
      "monitoringPeriodDays": 30,
      "auditTrailId": "AUDIT-REACT-789012"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "REACTIVATION_VERIFICATION_FAILED",
  "errorMessage": "Counterparty reactivation failed due to unresolved compliance issues",
  "details": {
    "unresolvedIssues": ["KYC documentation incomplete", "Credit rating below threshold"],
    "requiredActions": ["Update KYC documents", "Obtain credit approval"]
  }
}
```

**Side Effects**:
- Updates counterparty status from SUSPENDED to ACTIVE
- Restores trading capabilities with any new limits
- Initiates enhanced monitoring period if specified
- Generates notifications to relevant teams about reactivation

### 4. Business Logic
**Processing Steps**:
1. Validate reactivation request and verify counterparty is suspended
2. Verify resolution of original suspension issues
3. Perform fresh risk assessment and compliance checks
4. Validate authorization level for reactivation decision
5. Apply new trading limits and monitoring requirements
6. Update counterparty status to ACTIVE in master data
7. Restore trading platform access and capabilities
8. Generate notifications and create audit trail

**Business Rules**:
- **Issue Resolution**: Original suspension issues must be fully resolved
- **Authorization Requirements**: Reactivation requires same or higher authorization as suspension
- **Risk Assessment**: Fresh risk assessment must be performed before reactivation
- **Enhanced Monitoring**: Reactivated counterparties may require enhanced monitoring
- **Trading Limits**: New trading limits may be imposed post-reactivation

**Algorithms**:
- Issue resolution verification against original suspension criteria
- Risk score recalculation using current market and credit data
- Trading limit calculation based on updated risk assessment

### 5. Validation Rules
**Pre-processing Validations**:
- **Counterparty Status**: Counterparty must be in SUSPENDED status
- **Authorization Level**: User must have sufficient authority for reactivation
- **Issue Resolution**: All suspension issues must be marked as resolved
- **Documentation**: Required supporting documentation must be provided

**Post-processing Validations**:
- **Status Update**: Counterparty status must be successfully updated to ACTIVE
- **Trading Access**: Trading platform access must be restored
- **Limit Application**: New trading limits must be properly applied

**Data Quality Checks**:
- Consistency between reactivation reason and issue resolution
- Validation of new trading limits against risk parameters
- Completeness of monitoring and compliance requirements

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Invalid reactivation request or unresolved issues
- **PROCESSING_ERROR**: Reactivation execution or system update failures
- **SYSTEM_ERROR**: Database or trading platform connectivity issues
- **TIMEOUT_ERROR**: Reactivation processing exceeded timeout limits

**Error Recovery**:
- Retry reactivation operation up to 3 times for transient failures
- Escalate to senior risk management for complex resolution issues
- Manual intervention workflow for trading platform integration failures

**Error Propagation**:
- Return detailed error information to requesting user
- Generate alerts for risk management and compliance teams
- Log all errors with correlation IDs for audit purposes

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 4000 milliseconds (95th percentile)
- **Throughput**: 30 reactivations per minute
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity for risk assessment and validation
- **Memory**: 384MB per concurrent reactivation operation
- **I/O**: Medium for database updates and external system integration

**Scalability**:
- Horizontal scaling through distributed processing nodes
- Performance scales with complexity of risk assessment and verification

### 8. Dependencies
**Internal Dependencies**:
- **Counterparty Repository**: Master counterparty data and status management
- **Risk Assessment Service**: Fresh risk scoring and evaluation
- **Compliance Service**: Issue resolution verification and compliance checks

**External Dependencies**:
- **Trading Platform**: Trading access restoration and limit application (SLA: 99.9%)
- **Credit Rating Service**: Updated credit assessments (SLA: 99.0%)
- **Regulatory Database**: Compliance status verification (SLA: 99.5%)

**Data Dependencies**:
- Original suspension details and resolution status
- Current counterparty risk and compliance data
- User authorization levels and permissions

### 9. Configuration Parameters
**Required Configuration**:
- `authorizationMatrix`: object - Authorization levels for reactivation
- `defaultMonitoringPeriod`: integer - Default enhanced monitoring period - Default: 30
- `riskAssessmentRequired`: boolean - Require fresh risk assessment - Default: true

**Optional Configuration**:
- `tradingLimitReduction`: number - Percentage reduction in trading limits - Default: 0.2
- `automaticLimitRestore`: boolean - Automatically restore original limits after monitoring - Default: false

**Environment-Specific Configuration**:
- Development: Use simplified risk assessment and test trading platform
- Production: Use full risk assessment and production trading platform

### 10. Integration Points
**API Contracts**:
- Input: CounterpartyReactivationRequest with reactivation details and authorization
- Output: ReactivatedCounterparty with reactivation confirmation and new parameters

**Data Exchange Formats**:
- **JSON**: Primary format for API requests and responses
- **XML**: Legacy system integration for trading platform

**Event Publishing**:
- **CounterpartyReactivated**: Published when reactivation completes successfully
- **CounterpartyReactivationFailed**: Published when reactivation fails

**Event Consumption**:
- **IssueResolved**: Triggers counterparty reactivation workflow
- **MonitoringPeriodExpired**: Triggers automatic limit restoration if configured
