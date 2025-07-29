# processMaturity Processor Specification

## 1. Component Overview
**Component Name**: processMaturity
**Component Type**: CyodaProcessor
**Business Domain**: Trade Management
**Purpose**: Processes trade maturity events and lifecycle completion with settlement and regulatory compliance
**Workflow Context**: TradeWorkflow (maturing state)

## 2. Input Specifications
**Entity Type**: Trade
**Required Fields**:
- `tradeId`: string - Unique trade identifier
- `maturityDate`: ISO-8601 date - Trade maturity date
- `currentDate`: ISO-8601 date - Current business date
- `tradeStatus`: string - Current trade status
- `notionalAmount`: decimal - Trade notional amount

**Optional Fields**:
- `settlementInstructions`: object - Settlement processing instructions
- `maturityOverride`: boolean - Manual maturity processing override
- `earlyTerminationFlag`: boolean - Early termination indicator

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "maturity" - Tags for maturity processing nodes
- `responseTimeoutMs`: 60000 - Maximum processing time (60 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-223456789abg" - Process parameter reference

**Context Data**:
- Settlement system configuration
- Maturity processing rules
- Regulatory reporting requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Trade",
    "processingTimestamp": "2024-01-15T16:00:00Z",
    "maturityResults": {
      "maturityProcessed": true,
      "originalStatus": "active",
      "maturedStatus": "matured",
      "settlementAmount": 10000000.00,
      "maturityTimestamp": "2024-01-15T16:00:00Z"
    },
    "settlementDetails": {
      "settlementRequired": true,
      "settlementInstructions": "processed",
      "regulatoryReporting": "completed"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "MATURITY_ERROR",
  "errorMessage": "Trade maturity processing failed",
  "details": {
    "validationErrors": ["Trade not eligible for maturity"],
    "settlementErrors": ["Settlement system unavailable"],
    "systemErrors": ["Position service timeout"]
  }
}
```

**Side Effects**:
- Updates Trade entity status to matured
- Processes settlement instructions
- Triggers final position calculations
- Generates maturity regulatory reports
- Publishes TradeMatured event

## 4. Business Logic
**Processing Steps**:
1. Validate trade eligibility for maturity processing
2. Calculate final settlement amounts and obligations
3. Process settlement instructions and payments
4. Update trade status and lifecycle information
5. Generate final position and exposure calculations
6. Create regulatory maturity notifications
7. Archive trade with maturity completion data

**Business Rules**:
- **Maturity Eligibility**: Trade must be active and at or past maturity date
- **Settlement Processing**: All settlement obligations must be calculated and processed
- **Regulatory Compliance**: Maturity must be reported per regulatory requirements
- **Position Impact**: Final positions must be calculated and reconciled
- **Audit Trail**: Complete maturity processing audit trail required

**Algorithms**:
- Settlement amount calculation using trade economics
- Final position calculation including maturity impact
- Regulatory reporting determination using rule engine

## 5. Validation Rules
**Pre-processing Validations**:
- **Maturity Eligibility**: Trade status allows maturity processing
- **Date Validation**: Current date at or after maturity date
- **Settlement Readiness**: Settlement systems operational and accessible

**Post-processing Validations**:
- **Status Update**: Trade status updated correctly to matured
- **Settlement Completion**: Settlement processing completed successfully
- **Position Update**: Final positions calculated and updated

**Data Quality Checks**:
- **Settlement Accuracy**: Settlement amounts calculated correctly
- **Regulatory Compliance**: All regulatory requirements satisfied
- **Audit Completeness**: Complete audit trail created

## 6. Error Handling
**Error Categories**:
- **MATURITY_ERROR**: Maturity processing logic failures
- **SETTLEMENT_ERROR**: Settlement system or calculation failures
- **VALIDATION_ERROR**: Maturity eligibility validation failures
- **SYSTEM_ERROR**: External system or infrastructure failures
- **TIMEOUT_ERROR**: Maturity processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient settlement system errors (max 3 retries)
- Rollback mechanism for partial maturity processing failures
- Manual intervention queue for complex maturity scenarios

**Error Propagation**:
- Maturity errors trigger transition to maturity-failed state
- Error details stored for manual review and reprocessing
- Critical errors escalated to trading operations team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 45 seconds (95th percentile)
- **Throughput**: 10 maturities per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for settlement and position calculations
- **Memory**: 2GB per concurrent maturity processing
- **I/O**: Very high for settlement system integration

**Scalability**:
- Horizontal scaling through maturity processing nodes
- Performance varies with trade complexity and settlement requirements
- Parallel processing for independent maturity components

## 8. Dependencies
**Internal Dependencies**:
- **Settlement Service**: Settlement calculation and processing
- **Position Service**: Final position calculation and updates
- **Regulatory Service**: Maturity reporting requirements
- **Audit Service**: Maturity processing audit trail

**External Dependencies**:
- **Settlement System**: External settlement processing (SLA: 99.9% availability, 10s response)
- **Regulatory Reporting Service**: Maturity reporting submission (SLA: 99.9% availability, 5s response)

**Data Dependencies**:
- Trade master data with complete settlement terms
- Settlement system configuration and credentials
- Position calculation parameters
- Regulatory reporting rules and templates

## 9. Configuration Parameters
**Required Configuration**:
- `settlementEnabled`: boolean - Enable settlement processing - Default: true
- `regulatoryReportingEnabled`: boolean - Enable regulatory reporting - Default: true
- `positionUpdateEnabled`: boolean - Enable position updates - Default: true

**Optional Configuration**:
- `settlementTimeoutSeconds`: integer - Settlement processing timeout - Default: 30
- `autoMaturityEnabled`: boolean - Enable automatic maturity processing - Default: true
- `auditTrailEnabled`: boolean - Enable detailed audit trail - Default: true

**Environment-Specific Configuration**:
- Development: Mock settlement processing, relaxed validation
- Production: Full settlement processing, strict validation

## 10. Integration Points
**API Contracts**:
- Input: Trade entity with maturity eligibility
- Output: Maturity results with settlement and position details

**Data Exchange Formats**:
- **JSON**: Maturity processing results
- **XML**: Regulatory maturity reporting format
- **SWIFT**: Settlement instruction format

**Event Publishing**:
- **TradeMatured**: Published on successful maturity with details
- **MaturityFailed**: Published on maturity failure with error details
- **SettlementCompleted**: Published with settlement processing results

**Event Consumption**:
- **MaturityDue**: Triggers maturity processing
- **SettlementUpdated**: Updates settlement configuration
- **RegulatoryRulesUpdated**: Updates regulatory compliance rules
