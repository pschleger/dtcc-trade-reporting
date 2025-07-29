# deprecateProduct Processor Specification

### 1. Component Overview
**Component Name**: deprecateProduct
**Component Type**: CyodaProcessor
**Business Domain**: Reference Data Management
**Purpose**: Deprecates financial products and handles impact on existing trades with proper transition planning
**Workflow Context**: Used in product lifecycle management workflows for product discontinuation

### 2. Input Specifications
**Entity Type**: ProductDeprecationRequest
**Required Fields**:
- `productId`: string - Unique identifier of the product to deprecate
- `deprecationReason`: string - Business reason for deprecation
- `effectiveDate`: ISO-8601 timestamp - When deprecation takes effect
- `authorizingUser`: string - User authorizing the deprecation
- `requestTimestamp`: ISO-8601 timestamp - When deprecation was requested

**Optional Fields**:
- `transitionPlan`: TransitionPlan - Plan for handling existing trades
- `replacementProduct`: string - Replacement product identifier if applicable
- `gracePeriod`: integer - Grace period in days for existing trades
- `supportingDocuments`: array - Documentation supporting deprecation decision

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 30000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Current product usage and existing trade inventory
- Regulatory requirements for product deprecation
- Risk management policies for deprecated products

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "DeprecatedProduct",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "deprecationId": "DEPR-PROD-20240115-001",
      "effectiveTimestamp": "2024-03-15T00:00:00Z",
      "impactedTrades": 125,
      "transitionPlanId": "TRANS-PLAN-001",
      "gracePeriodDays": 90
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "DEPRECATION_BLOCKED_ACTIVE_TRADES",
  "errorMessage": "Product deprecation blocked due to high volume of active trades without transition plan",
  "details": {
    "activeTrades": 500,
    "totalNotional": 50000000,
    "requiredActions": ["Create transition plan", "Notify counterparties"]
  }
}
```

**Side Effects**:
- Updates product status to DEPRECATED in product repository
- Blocks new trade creation using deprecated product
- Initiates transition workflow for existing trades
- Generates notifications to trading and operations teams

### 4. Business Logic
**Processing Steps**:
1. Validate deprecation request and authorization level
2. Assess impact on existing trades and market positions
3. Verify transition plan adequacy for existing trade volume
4. Apply deprecation status to product master data
5. Block new trading activities using deprecated product
6. Initiate transition workflow for existing trades
7. Generate notifications to all relevant stakeholders
8. Create comprehensive audit trail and compliance documentation

**Business Rules**:
- **Impact Assessment**: Must assess impact on all existing trades and positions
- **Transition Planning**: High-volume products require formal transition plans
- **Grace Period**: Existing trades may have grace period before forced closure
- **Regulatory Compliance**: Deprecation must comply with regulatory requirements
- **Counterparty Notification**: All counterparties must be notified of deprecation

**Algorithms**:
- Trade impact assessment based on notional amounts and maturity dates
- Transition timeline calculation based on trade characteristics
- Risk exposure calculation for deprecated product portfolio

### 5. Validation Rules
**Pre-processing Validations**:
- **Product Status**: Product must be in ACTIVE status
- **Authorization Level**: User must have authority for product deprecation
- **Transition Plan**: High-impact deprecations require transition plans
- **Effective Date**: Deprecation date must be in the future with adequate notice

**Post-processing Validations**:
- **Status Update**: Product status must be successfully updated to DEPRECATED
- **Trading Block**: New trading must be effectively blocked
- **Transition Initiation**: Transition workflow must be successfully initiated
- **Notification Delivery**: All required notifications must be sent

**Data Quality Checks**:
- Completeness of existing trade inventory
- Accuracy of impact assessment calculations
- Consistency of transition plan with existing trades

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Invalid deprecation request or insufficient transition planning
- **PROCESSING_ERROR**: Deprecation execution or transition workflow failures
- **SYSTEM_ERROR**: Database or external system connectivity issues
- **TIMEOUT_ERROR**: Deprecation processing exceeded timeout limits

**Error Recovery**:
- Retry deprecation operation up to 3 times for transient failures
- Escalate to product committee for complex transition issues
- Manual intervention workflow for critical system failures

**Error Propagation**:
- Return detailed error information to requesting user
- Generate alerts for product management and risk teams
- Log all errors with correlation IDs for audit purposes

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 15000 milliseconds (95th percentile)
- **Throughput**: 20 deprecations per hour
- **Availability**: 99.5% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for impact assessment and transition planning
- **Memory**: 1GB per concurrent deprecation operation
- **I/O**: High for database operations and notification delivery

**Scalability**:
- Horizontal scaling through distributed processing nodes
- Performance scales with number of existing trades and complexity

### 8. Dependencies
**Internal Dependencies**:
- **Product Repository**: Master product data and status management
- **Trade Repository**: Existing trade data for impact assessment
- **Transition Management System**: Trade transition and migration workflows
- **Notification Service**: Stakeholder notification and communication

**External Dependencies**:
- **Regulatory Database**: Compliance requirements for product deprecation (SLA: 99.5%)
- **Counterparty Systems**: External notification and communication (SLA: 99.0%)

**Data Dependencies**:
- Current product master data and usage statistics
- Complete inventory of existing trades using the product
- Regulatory requirements for product lifecycle management

### 9. Configuration Parameters
**Required Configuration**:
- `transitionPlanThreshold`: integer - Trade count requiring transition plan - Default: 100
- `gracePeriodDays`: integer - Default grace period for existing trades - Default: 90
- `notificationLeadDays`: integer - Advance notice required for deprecation - Default: 30

**Optional Configuration**:
- `automaticTransitionEnabled`: boolean - Enable automatic transition for simple trades - Default: false
- `forcedClosureEnabled`: boolean - Allow forced closure after grace period - Default: true

**Environment-Specific Configuration**:
- Development: Use shortened grace periods and simplified transition
- Production: Use full grace periods and comprehensive transition planning

### 10. Integration Points
**API Contracts**:
- Input: ProductDeprecationRequest with deprecation details and transition plan
- Output: DeprecatedProduct with deprecation confirmation and transition data

**Data Exchange Formats**:
- **JSON**: Primary format for API requests and responses
- **XML**: Legacy system integration for trading platform

**Event Publishing**:
- **ProductDeprecated**: Published when deprecation completes successfully
- **ProductDeprecationFailed**: Published when deprecation fails

**Event Consumption**:
- **ProductCommitteeDecision**: Triggers product deprecation workflow
- **RegulatoryRequirement**: External regulatory requirements for product deprecation
