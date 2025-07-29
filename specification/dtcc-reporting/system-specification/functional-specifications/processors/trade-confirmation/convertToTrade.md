# convertToTrade Processor Specification

## 1. Component Overview
**Component Name**: convertToTrade
**Component Type**: CyodaProcessor
**Business Domain**: Trade Confirmation Processing
**Purpose**: Converts validated FpML trade confirmation messages to standardized Trade entities for downstream processing
**Workflow Context**: TradeConfirmationWorkflow (processing state)

## 2. Input Specifications
**Entity Type**: TradeConfirmation
**Required Fields**:
- `fpmlMessage`: string - Validated FpML XML message content
- `validationResults`: object - Results from validateFpMLMessage processor
- `extractedTradeData`: object - Pre-extracted trade data from validation
- `messageId`: string - Unique message identifier
- `processingTimestamp`: ISO-8601 timestamp - Validation completion time

**Optional Fields**:
- `correlationId`: string - Correlation identifier for tracking
- `businessDate`: ISO-8601 date - Business date override
- `processingFlags`: object - Special processing instructions

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "conversion" - Tags for conversion processing nodes
- `responseTimeoutMs`: 30000 - Maximum processing time (30 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-123456789abd" - Process parameter reference

**Context Data**:
- Product master data for trade classification
- Counterparty master data for party resolution
- Currency and market data for valuation context

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "TradeConfirmation",
    "processingTimestamp": "2024-01-15T10:35:00Z",
    "createdTrade": {
      "tradeId": "TRD-12345",
      "entityType": "Trade",
      "status": "new",
      "creationTimestamp": "2024-01-15T10:35:00Z"
    },
    "conversionMetadata": {
      "sourceMessageId": "MSG-67890",
      "conversionRules": ["FpML-5.12-to-Trade-v1.0"],
      "dataQualityScore": 0.98
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "CONVERSION_ERROR",
  "errorMessage": "Trade entity conversion failed",
  "details": {
    "conversionErrors": ["Unable to map product type"],
    "missingData": ["Counterparty master data"],
    "dataQualityIssues": ["Inconsistent notional amounts"]
  }
}
```

**Side Effects**:
- Creates new Trade entity in the system
- Updates TradeConfirmation entity with conversion status
- Establishes linkage between TradeConfirmation and Trade entities
- Publishes TradeCreated event for downstream workflows

## 4. Business Logic
**Processing Steps**:
1. Extract trade identification and generate internal trade ID
2. Map FpML party information to internal counterparty entities
3. Convert FpML product specification to internal product classification
4. Transform trade economics (notional, rates, dates) to internal format
5. Apply business logic for trade classification and risk categorization
6. Create Trade entity with standardized data structure
7. Establish audit trail linking confirmation to trade
8. Validate converted trade data for completeness and consistency

**Business Rules**:
- **Trade ID Generation**: Internal trade IDs follow format "TRD-{YYYYMMDD}-{sequence}"
- **Party Mapping**: FpML party roles mapped to internal counterparty relationships
- **Product Classification**: FpML product types mapped to internal taxonomy
- **Date Standardization**: All dates converted to UTC and business date aligned
- **Currency Normalization**: All amounts converted to base currency equivalents

**Algorithms**:
- Trade ID generation using date-based sequence numbering
- Product taxonomy mapping using configurable rule engine
- Currency conversion using market data rates
- Risk classification using notional and product type matrices

## 5. Validation Rules
**Pre-processing Validations**:
- **Validation Status**: TradeConfirmation must have successful validation status
- **Required Data**: All mandatory trade data elements present in extracted data
- **Data Integrity**: Extracted data passes consistency checks

**Post-processing Validations**:
- **Trade Completeness**: Created Trade entity contains all required fields
- **Data Consistency**: Converted data maintains referential integrity
- **Business Logic**: Trade classification and risk categorization applied correctly

**Data Quality Checks**:
- **Duplicate Prevention**: Check for existing trades with same external reference
- **Data Accuracy**: Verify converted amounts and dates within expected ranges
- **Referential Integrity**: Ensure all referenced entities exist in master data

## 6. Error Handling
**Error Categories**:
- **CONVERSION_ERROR**: Data mapping or transformation failures
- **VALIDATION_ERROR**: Post-conversion validation failures
- **SYSTEM_ERROR**: Master data access or entity creation failures
- **TIMEOUT_ERROR**: Conversion processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient system errors (max 3 retries)
- Partial conversion recovery for non-critical data elements
- Manual review queue for complex conversion failures

**Error Propagation**:
- Conversion errors trigger transition to processing-failed state
- Error details stored for manual review and correction
- Operations team notified for critical conversion failures

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 25 seconds (95th percentile)
- **Throughput**: 50 conversions per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for data transformation and validation
- **Memory**: 1GB per concurrent conversion for complex products
- **I/O**: High for master data access and entity creation

**Scalability**:
- Horizontal scaling through calculation node distribution
- Performance varies with product complexity and data volume
- Caching of master data improves conversion performance

## 8. Dependencies
**Internal Dependencies**:
- **Master Data Service**: Product and counterparty reference data
- **Entity Service**: Trade entity creation and management
- **ID Generation Service**: Trade ID sequence management
- **Currency Service**: Exchange rate data for conversions

**External Dependencies**:
- **Market Data Service**: Real-time currency rates (SLA: 99.5% availability, 1s response)
- **Product Taxonomy Service**: Product classification rules (cached locally)

**Data Dependencies**:
- Product master data with FpML mapping rules
- Counterparty master data with LEI mappings
- Currency exchange rates (daily updates)
- Business calendar for date calculations

## 9. Configuration Parameters
**Required Configuration**:
- `tradeIdPrefix`: string - Trade ID prefix format - Default: "TRD"
- `baseCurrency`: string - Base currency for conversions - Default: "USD"
- `dataQualityThreshold`: decimal - Minimum data quality score - Default: 0.95

**Optional Configuration**:
- `enableCurrencyConversion`: boolean - Enable currency conversion - Default: true
- `strictMappingMode`: boolean - Require exact product mapping - Default: false
- `auditTrailEnabled`: boolean - Enable detailed audit trail - Default: true

**Environment-Specific Configuration**:
- Development: Relaxed data quality thresholds, mock master data
- Production: Full data quality validation, live master data access

## 10. Integration Points
**API Contracts**:
- Input: Validated TradeConfirmation entity with extracted data
- Output: Created Trade entity with conversion metadata

**Data Exchange Formats**:
- **FpML XML**: Source data format
- **JSON**: Internal entity format
- **Avro**: Event publishing format

**Event Publishing**:
- **TradeCreated**: Published on successful trade creation with trade details
- **ConversionCompleted**: Published with conversion metadata and quality metrics
- **ConversionFailed**: Published on conversion failure with error details

**Event Consumption**:
- **ValidationCompleted**: Triggers conversion processing
- **MasterDataUpdated**: Updates cached reference data for mapping rules
