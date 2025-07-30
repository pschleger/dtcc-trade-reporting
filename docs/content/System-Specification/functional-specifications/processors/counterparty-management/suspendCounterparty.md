# suspendCounterparty Processor Specification

### 1. Component Overview
**Component Name**: suspendCounterparty
**Component Type**: CyodaProcessor
**Business Domain**: Counterparty Management
**Purpose**: Suspends counterparty trading activities due to compliance, credit, or operational issues with proper authorization and impact assessment
**Workflow Context**: Used in risk management and compliance workflows for counterparty suspension

### 2. Input Specifications
**Entity Type**: CounterpartySuspensionRequest
**Required Fields**:
- `counterpartyId`: string - Unique identifier of the counterparty to suspend
- `suspensionReason`: string - Business reason for suspension (CREDIT_RISK, COMPLIANCE_ISSUE, OPERATIONAL_ISSUE)
- `suspensionType`: string - Type of suspension (FULL, PARTIAL, TRADING_ONLY)
- `authorizingUser`: string - User authorizing the suspension
- `requestTimestamp`: ISO-8601 timestamp - When suspension was requested

**Optional Fields**:
- `effectiveDate`: ISO-8601 timestamp - When suspension should take effect
- `expirationDate`: ISO-8601 timestamp - When suspension automatically expires
- `impactAssessment`: ImpactAssessment - Assessment of suspension impact
- `supportingDocuments`: array - Documentation supporting suspension decision

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 25000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Current counterparty status and trading activity
- Outstanding trades and positions with the counterparty
- Risk limits and exposure calculations

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "SuspendedCounterparty",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "suspensionId": "SUSP-CP-20240115-001",
      "effectiveTimestamp": "2024-01-15T10:30:00Z",
      "suspensionType": "FULL",
      "impactedTrades": 15,
      "notificationsSent": 8,
      "auditTrailId": "AUDIT-SUSP-789012"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "SUSPENSION_AUTHORIZATION_FAILED",
  "errorMessage": "Counterparty suspension failed due to insufficient authorization level",
  "details": {
    "requiredAuthLevel": "SENIOR_RISK_MANAGER",
    "currentAuthLevel": "RISK_ANALYST",
    "counterpartyRiskRating": "HIGH"
  }
}
```

**Side Effects**:
- Updates counterparty status to SUSPENDED in master data
- Blocks new trade creation with suspended counterparty
- Generates risk alerts and notifications to relevant teams
- Creates audit trail for regulatory compliance

### 4. Business Logic
**Processing Steps**:
1. Validate suspension request and authorization level
2. Assess current exposure and outstanding positions with counterparty
3. Determine suspension scope and impact on existing trades
4. Apply suspension status to counterparty master data
5. Block new trading activities and limit modifications
6. Generate notifications to trading, risk, and operations teams
7. Create detailed audit trail and compliance documentation
8. Schedule automatic review and expiration if applicable

**Business Rules**:
- **Authorization Levels**: Different suspension types require different authorization levels
- **Impact Assessment**: Must assess impact on outstanding trades and positions
- **Notification Requirements**: All relevant teams must be notified immediately
- **Regulatory Compliance**: Suspension must comply with regulatory reporting requirements
- **Automatic Expiration**: Temporary suspensions must have defined expiration dates

**Algorithms**:
- Risk exposure calculation for impact assessment
- Authorization level validation based on counterparty risk rating
- Notification routing based on suspension type and impact

### 5. Validation Rules
**Pre-processing Validations**:
- **Counterparty Existence**: Target counterparty must exist and be active
- **Authorization Level**: User must have sufficient authority for suspension type
- **Suspension Reason**: Must be valid business reason from approved list
- **Duplicate Prevention**: Counterparty cannot already be suspended

**Post-processing Validations**:
- **Status Update**: Counterparty status must be successfully updated to SUSPENDED
- **Trading Block**: New trading activities must be effectively blocked
- **Notification Delivery**: All required notifications must be sent successfully

**Data Quality Checks**:
- Consistency between suspension type and reason
- Validation of effective and expiration dates
- Completeness of impact assessment data

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Invalid suspension request or insufficient authorization
- **PROCESSING_ERROR**: Suspension execution or notification failures
- **SYSTEM_ERROR**: Database or external system connectivity issues
- **TIMEOUT_ERROR**: Suspension processing exceeded timeout limits

**Error Recovery**:
- Retry suspension operation up to 3 times for transient failures
- Escalate to senior risk management for authorization failures
- Manual intervention workflow for critical system failures

**Error Propagation**:
- Return detailed error information to requesting user
- Generate immediate alerts for risk management team
- Log all errors with high priority for audit and compliance

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 5000 milliseconds (95th percentile)
- **Throughput**: 50 suspensions per minute
- **Availability**: 99.95% uptime (critical risk management function)

**Resource Requirements**:
- **CPU**: High intensity for impact assessment and notification processing
- **Memory**: 512MB per concurrent suspension operation
- **I/O**: High for database updates and notification delivery

**Scalability**:
- Horizontal scaling through distributed processing nodes
- Performance scales with number of outstanding trades and notifications

### 8. Dependencies
**Internal Dependencies**:
- **Counterparty Repository**: Master counterparty data and status management
- **Trade Repository**: Outstanding trade and position data
- **Risk Management System**: Exposure calculations and risk assessments
- **Notification Service**: Alert and notification delivery

**External Dependencies**:
- **Regulatory Reporting System**: Compliance reporting for suspensions (SLA: 99.5%)
- **Trading Platform**: Trading activity blocking and restrictions (SLA: 99.9%)

**Data Dependencies**:
- Current counterparty master data and trading status
- Outstanding trade and position data
- User authorization levels and permissions

### 9. Configuration Parameters
**Required Configuration**:
- `authorizationMatrix`: object - Authorization levels by suspension type - Default: complex matrix
- `notificationTemplates`: object - Notification templates by suspension type
- `automaticExpirationEnabled`: boolean - Enable automatic expiration - Default: true

**Optional Configuration**:
- `impactAssessmentRequired`: boolean - Require impact assessment - Default: true
- `immediateEffective`: boolean - Make suspension immediately effective - Default: true

**Environment-Specific Configuration**:
- Development: Use test notification endpoints and simplified authorization
- Production: Use production notification systems and full authorization matrix

### 10. Integration Points
**API Contracts**:
- Input: CounterpartySuspensionRequest with suspension details and authorization
- Output: SuspendedCounterparty with suspension confirmation and impact data

**Data Exchange Formats**:
- **JSON**: Primary format for API requests and responses
- **XML**: Legacy system integration for trading platform

**Event Publishing**:
- **CounterpartySuspended**: Published when suspension completes successfully
- **CounterpartySuspensionFailed**: Published when suspension fails

**Event Consumption**:
- **RiskLimitBreached**: Triggers automatic counterparty suspension workflow
- **ComplianceIssueDetected**: Initiates compliance-based suspension process
