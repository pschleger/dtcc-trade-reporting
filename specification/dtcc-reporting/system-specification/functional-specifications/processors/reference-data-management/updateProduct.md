# updateProduct Processor Specification

### 1. Component Overview
**Component Name**: updateProduct
**Component Type**: CyodaProcessor
**Business Domain**: Reference Data Management
**Purpose**: Updates product specifications with validation, approval workflow, and impact assessment on existing trades
**Workflow Context**: Used in product maintenance workflows for specification updates and corrections

### 2. Input Specifications
**Entity Type**: ProductUpdateRequest
**Required Fields**:
- `productId`: string - Unique identifier of the product to update
- `updateFields`: object - Fields to be updated with new values
- `updateReason`: string - Business justification for the update
- `requestingUser`: string - User requesting the update
- `requestTimestamp`: ISO-8601 timestamp - When update was requested

**Optional Fields**:
- `effectiveDate`: ISO-8601 timestamp - When update should take effect
- `impactAssessment`: ImpactAssessment - Assessment of update impact on existing trades
- `approvalRequired`: boolean - Whether update requires approval workflow
- `supportingDocuments`: array - Supporting documentation for update

**Configuration Parameters**:
- `attachEntity`: boolean - Whether to attach entity to processing context
- `calculationNodesTags`: string - Tags for distributed processing nodes
- `responseTimeoutMs`: integer - Maximum processing time in milliseconds (default: 25000)
- `processParamId`: string - Time UUID for process parameter reference

**Context Data**:
- Current product specifications and characteristics
- Existing trades and positions using the product
- Approval workflow configuration and impact thresholds

### 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "UpdatedProduct",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "additionalData": {
      "updateId": "UPD-PROD-20240115-001",
      "fieldsUpdated": ["riskWeighting", "marginRequirement"],
      "impactedTrades": 45,
      "approvalStatus": "APPROVED",
      "effectiveTimestamp": "2024-01-20T00:00:00Z"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "UPDATE_IMPACT_EXCESSIVE",
  "errorMessage": "Product update rejected due to excessive impact on existing trades",
  "details": {
    "impactedTrades": 1250,
    "estimatedPnLImpact": 2500000,
    "riskLimitBreaches": ["VaR limit", "Concentration limit"]
  }
}
```

**Side Effects**:
- Updates product master data with new specifications
- Triggers revaluation of existing trades using the product
- Creates audit trail for regulatory compliance
- Notifies trading and risk management teams of changes

### 4. Business Logic
**Processing Steps**:
1. Validate product update request and field specifications
2. Retrieve current product data and assess proposed changes
3. Perform impact assessment on existing trades and positions
4. Determine if approval workflow is required based on impact thresholds
5. Execute approval workflow if required
6. Apply updates to product master data
7. Trigger revaluation and risk recalculation for impacted trades
8. Generate notifications and audit trail

**Business Rules**:
- **Impact Thresholds**: Material changes require approval and impact assessment
- **Effective Dating**: Updates can be scheduled for future effective dates
- **Trade Impact**: Existing trades must be revalued after product updates
- **Risk Limits**: Updates must not cause risk limit breaches
- **Regulatory Compliance**: Changes must maintain regulatory compliance

**Algorithms**:
- Impact assessment calculation for existing trade portfolios
- Risk recalculation using updated product specifications
- Approval requirement determination based on change materiality

### 5. Validation Rules
**Pre-processing Validations**:
- **Product Existence**: Target product must exist and be in ACTIVE status
- **Update Authorization**: User must have permissions for requested changes
- **Field Validation**: Updated fields must pass format and business rule validation
- **Change Detection**: At least one field must be different from current values

**Post-processing Validations**:
- **Data Integrity**: Updated product data must maintain consistency
- **Trade Revaluation**: Impacted trades must be successfully revalued
- **Risk Compliance**: Updated product must not cause risk limit breaches
- **Regulatory Compliance**: Changes must maintain regulatory compliance

**Data Quality Checks**:
- Product specification consistency and completeness
- Risk parameter validation against market standards
- Regulatory classification accuracy

### 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Invalid field values or business rule violations
- **PROCESSING_ERROR**: Update execution or approval workflow failures
- **SYSTEM_ERROR**: Database or external system connectivity issues
- **TIMEOUT_ERROR**: Update processing exceeded timeout limits

**Error Recovery**:
- Retry update operation up to 3 times for transient failures
- Rollback partial updates to maintain data consistency
- Escalate to product committee for complex approval failures

**Error Propagation**:
- Return detailed validation errors to requesting user
- Generate alerts for product management and risk teams
- Log all errors with correlation IDs for audit purposes

### 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 8000 milliseconds (95th percentile)
- **Throughput**: 50 updates per hour
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for impact assessment and revaluation
- **Memory**: 512MB per concurrent update operation
- **I/O**: High for database operations and trade revaluation

**Scalability**:
- Horizontal scaling through distributed processing nodes
- Performance scales with number of impacted trades and complexity

### 8. Dependencies
**Internal Dependencies**:
- **Product Repository**: Master product data storage and management
- **Trade Repository**: Existing trade data for impact assessment
- **Risk Management System**: Risk calculation and limit monitoring
- **Approval Workflow Service**: Product change approval process

**External Dependencies**:
- **Market Data Service**: Current market data for revaluation (SLA: 99.0%)
- **Regulatory Database**: Compliance requirements and validation (SLA: 99.5%)

**Data Dependencies**:
- Current product master data and specifications
- Existing trade and position data using the product
- Risk limit and approval threshold configuration

### 9. Configuration Parameters
**Required Configuration**:
- `approvalThresholds`: object - Thresholds requiring approval by field type
- `impactAssessmentEnabled`: boolean - Enable impact assessment - Default: true
- `automaticRevaluationEnabled`: boolean - Enable automatic trade revaluation - Default: true

**Optional Configuration**:
- `maxImpactedTrades`: integer - Maximum trades that can be impacted - Default: 1000
- `notificationEnabled`: boolean - Send update notifications - Default: true

**Environment-Specific Configuration**:
- Development: Use simplified approval and test revaluation
- Production: Use full approval workflow and production revaluation

### 10. Integration Points
**API Contracts**:
- Input: ProductUpdateRequest with fields to update and metadata
- Output: UpdatedProduct with update confirmation and impact data

**Data Exchange Formats**:
- **JSON**: Primary format for API requests and responses
- **XML**: Legacy system integration format

**Event Publishing**:
- **ProductUpdated**: Published when update completes successfully
- **ProductUpdateFailed**: Published when update fails validation

**Event Consumption**:
- **ApprovalCompleted**: Triggers final product update after approval
- **MarketDataChanged**: External market data changes affecting product specifications
