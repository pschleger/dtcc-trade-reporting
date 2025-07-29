# validateFpMLMessage Processor Specification

## 1. Component Overview
**Component Name**: validateFpMLMessage
**Component Type**: CyodaProcessor
**Business Domain**: Trade Confirmation Processing
**Purpose**: Validates incoming FpML trade confirmation messages for structural integrity, schema compliance, and business rule adherence
**Workflow Context**: TradeConfirmationWorkflow (validating state)

## 2. Input Specifications
**Entity Type**: TradeConfirmation
**Required Fields**:
- `fpmlMessage`: string - Raw FpML XML message content
- `messageId`: string - Unique message identifier
- `messageType`: string - FpML message type (e.g., "TradeConfirmed", "TradeAmended")
- `sendingParty`: string - LEI of the message sender
- `receivingParty`: string - LEI of the message receiver
- `creationTimestamp`: ISO-8601 timestamp - Message creation time

**Optional Fields**:
- `correlationId`: string - Correlation identifier for message tracking
- `messageVersion`: string - FpML schema version
- `businessDate`: ISO-8601 date - Business date for the trade

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "validation" - Tags for validation processing nodes
- `responseTimeoutMs`: 20000 - Maximum processing time (20 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-123456789abc" - Process parameter reference

**Context Data**:
- FpML schema definitions for validation
- Business rule configuration
- Counterparty LEI registry access

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeConfirmation",
    "processingTimestamp": "2024-01-15T10:30:00Z",
    "validationResults": {
      "schemaValid": true,
      "businessRulesValid": true,
      "structuralValid": true
    },
    "extractedTradeData": {
      "tradeId": "TRD-12345",
      "productType": "InterestRateSwap",
      "notionalAmount": 10000000,
      "currency": "USD",
      "effectiveDate": "2024-01-20",
      "maturityDate": "2029-01-20"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "VALIDATION_ERROR",
  "errorMessage": "FpML message validation failed",
  "details": {
    "schemaErrors": ["Invalid element 'tradeHeader'"],
    "businessRuleViolations": ["Counterparty LEI not found"],
    "structuralIssues": ["Missing required trade economics"]
  }
}
```

**Side Effects**:
- Updates TradeConfirmation entity validation status
- Logs validation results for audit trail
- Publishes validation events to monitoring system

## 4. Business Logic
**Processing Steps**:
1. Parse incoming FpML XML message
2. Validate against FpML schema definition
3. Extract trade identification and party information
4. Validate counterparty LEI codes against registry
5. Validate trade economics and product specifications
6. Check business rule compliance (trading limits, authorized products)
7. Extract and structure trade data for downstream processing

**Business Rules**:
- **LEI Validation**: All party LEI codes must be valid and active in LEI registry
- **Product Authorization**: Product type must be in authorized trading list
- **Notional Limits**: Trade notional must be within counterparty trading limits
- **Date Validation**: Trade dates must be valid business dates
- **Currency Validation**: Currency codes must be ISO 4217 compliant

**Algorithms**:
- XML schema validation using FpML 5.12 specification
- LEI checksum validation algorithm
- Business date calculation considering holidays and weekends

## 5. Validation Rules
**Pre-processing Validations**:
- **Message Format**: FpML message must be well-formed XML
- **Message Size**: Message size must not exceed 10MB limit
- **Required Fields**: All mandatory FpML elements must be present

**Post-processing Validations**:
- **Data Extraction**: All required trade data successfully extracted
- **Consistency Check**: Extracted data internally consistent

**Data Quality Checks**:
- **Duplicate Detection**: Check for duplicate message IDs
- **Temporal Validation**: Message timestamps within acceptable range
- **Referential Integrity**: Referenced entities exist in master data

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Schema or business rule validation failures
- **PROCESSING_ERROR**: XML parsing or data extraction failures
- **SYSTEM_ERROR**: LEI registry or master data access failures
- **TIMEOUT_ERROR**: Validation processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient system errors (max 3 retries)
- Fallback to manual review queue for complex validation failures
- Graceful degradation when external services unavailable

**Error Propagation**:
- Validation errors trigger transition to validation-failed state
- Error details stored in entity for manual review
- Notification sent to operations team for critical failures

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 15 seconds (95th percentile)
- **Throughput**: 100 messages per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity for XML parsing and validation
- **Memory**: 512MB per concurrent validation
- **I/O**: High for LEI registry and master data access

**Scalability**:
- Horizontal scaling through calculation node distribution
- Performance degrades linearly with message complexity
- Caching of LEI registry data improves performance

## 8. Dependencies
**Internal Dependencies**:
- **LEI Registry Service**: Counterparty validation
- **Master Data Service**: Product and currency reference data
- **Audit Service**: Validation result logging

**External Dependencies**:
- **GLEIF LEI Registry**: LEI validation (SLA: 99.5% availability, 2s response)
- **FpML Schema Repository**: Schema definitions (cached locally)

**Data Dependencies**:
- FpML schema definitions (version 5.12)
- Authorized product list
- Counterparty trading limits
- Holiday calendar data

## 9. Configuration Parameters
**Required Configuration**:
- `fpmlSchemaVersion`: string - FpML schema version - Default: "5.12"
- `maxMessageSizeMB`: integer - Maximum message size - Default: 10
- `leiValidationEnabled`: boolean - Enable LEI validation - Default: true

**Optional Configuration**:
- `strictValidation`: boolean - Enable strict validation mode - Default: false
- `cacheExpiryMinutes`: integer - LEI cache expiry - Default: 60

**Environment-Specific Configuration**:
- Development: Relaxed validation rules, mock LEI registry
- Production: Full validation, live LEI registry access

## 10. Integration Points
**API Contracts**:
- Input: FpML XML message via entity attachment
- Output: Validation results and extracted trade data

**Data Exchange Formats**:
- **FpML XML**: Input message format
- **JSON**: Validation results and extracted data

**Event Publishing**:
- **ValidationCompleted**: Published on successful validation with trade data
- **ValidationFailed**: Published on validation failure with error details

**Event Consumption**:
- **MessageReceived**: Triggers validation processing
- **SchemaUpdated**: Updates cached schema definitions
