# checkDuplicates Processor Specification

## 1. Component Overview
**Component Name**: checkDuplicates
**Component Type**: CyodaProcessor
**Business Domain**: Counterparty Data Management
**Purpose**: Checks for duplicate counterparty records in the system to maintain data integrity and compliance
**Workflow Context**: CounterpartyWorkflow (duplicate-checking state)

## 2. Input Specifications
**Entity Type**: Counterparty
**Required Fields**:
- `counterpartyId`: string - Unique counterparty identifier
- `leiCode`: string - Legal Entity Identifier (LEI)
- `entityName`: string - Legal entity name
- `registrationNumber`: string - Business registration number
- `jurisdictionCode`: string - Jurisdiction of incorporation

**Optional Fields**:
- `parentLei`: string - Parent entity LEI if applicable
- `addressInformation`: object - Registered address details
- `alternativeNames`: array - Alternative entity names
- `historicalIdentifiers`: array - Previous identifiers

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "duplicate-check" - Tags for duplicate checking processing nodes
- `responseTimeoutMs`: 30000 - Maximum processing time (30 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-523456789abd" - Process parameter reference

**Context Data**:
- Duplicate detection rules
- Matching algorithms configuration
- Data quality thresholds

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Counterparty",
    "processingTimestamp": "2024-01-15T12:30:00Z",
    "duplicateCheckResults": {
      "duplicatesFound": false,
      "potentialMatches": 0,
      "exactMatches": 0,
      "fuzzyMatches": 0,
      "confidenceScore": 0.95
    },
    "matchingDetails": {
      "leiMatches": 0,
      "nameMatches": 0,
      "registrationMatches": 0,
      "addressMatches": 0
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "DUPLICATE_CHECK_ERROR",
  "errorMessage": "Duplicate checking failed",
  "details": {
    "searchErrors": ["Database query timeout"],
    "matchingErrors": ["Fuzzy matching service unavailable"],
    "dataErrors": ["Incomplete counterparty data"],
    "systemErrors": ["Search index unavailable"]
  }
}
```

**Side Effects**:
- Updates Counterparty entity duplicate check status
- Creates duplicate check audit trail
- Flags potential duplicates for manual review
- Publishes DuplicateCheckCompleted event

## 4. Business Logic
**Processing Steps**:
1. Extract key identifying information from counterparty
2. Search existing counterparty database for exact matches
3. Perform fuzzy matching for potential duplicates
4. Analyze matching results and calculate confidence scores
5. Flag high-confidence matches for manual review
6. Generate duplicate check summary and recommendations
7. Update entity with duplicate check results

**Business Rules**:
- **LEI Uniqueness**: LEI codes must be unique across all counterparties
- **Registration Uniqueness**: Registration numbers must be unique within jurisdiction
- **Name Similarity**: Similar entity names require manual review
- **Address Matching**: Same address may indicate duplicate entities
- **Confidence Threshold**: High-confidence matches require investigation

**Algorithms**:
- Exact matching using database queries on key fields
- Fuzzy string matching using Levenshtein distance for names
- Address normalization and matching algorithms
- Confidence scoring using weighted field matching

## 5. Validation Rules
**Pre-processing Validations**:
- **Required Fields**: All key identifying fields present
- **Data Format**: Fields in correct format for matching
- **Search Index**: Counterparty search index accessible

**Post-processing Validations**:
- **Search Completeness**: All matching algorithms executed
- **Results Consistency**: Matching results internally consistent
- **Confidence Calculation**: Confidence scores calculated correctly

**Data Quality Checks**:
- **Field Completeness**: Key matching fields complete and valid
- **Data Normalization**: Data properly normalized for matching
- **Index Currency**: Search index current and complete

## 6. Error Handling
**Error Categories**:
- **DUPLICATE_CHECK_ERROR**: Duplicate detection logic failures
- **SEARCH_ERROR**: Database or index search failures
- **MATCHING_ERROR**: Fuzzy matching algorithm failures
- **DATA_ERROR**: Counterparty data quality issues
- **SYSTEM_ERROR**: Infrastructure or service failures
- **TIMEOUT_ERROR**: Duplicate checking timeout exceeded

**Error Recovery**:
- Retry mechanism for transient search errors (max 3 retries)
- Fallback to exact matching for fuzzy matching failures
- Manual review queue for system failures

**Error Propagation**:
- Duplicate check errors trigger transition to check-failed state
- Error details stored for manual review and retry
- Critical errors escalated to counterparty management team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 25 seconds (95th percentile)
- **Throughput**: 20 duplicate checks per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for fuzzy matching and search operations
- **Memory**: 512MB per concurrent duplicate check
- **I/O**: Very high for database search and index access

**Scalability**:
- Horizontal scaling through duplicate checking nodes
- Performance varies with database size and matching complexity
- Search index optimization improves performance

## 8. Dependencies
**Internal Dependencies**:
- **Counterparty Database**: Existing counterparty data for matching
- **Search Service**: Counterparty search and indexing
- **Matching Service**: Fuzzy matching algorithms
- **Configuration Service**: Duplicate detection rule configuration

**External Dependencies**:
- **LEI Registry Service**: LEI validation and uniqueness checking (SLA: 99.5% availability, 3s response)

**Data Dependencies**:
- Counterparty master data with complete identifying information
- Duplicate detection rules and thresholds
- Fuzzy matching algorithm configuration
- Search index with current counterparty data

## 9. Configuration Parameters
**Required Configuration**:
- `exactMatchThreshold`: decimal - Threshold for exact matches - Default: 1.0
- `fuzzyMatchThreshold`: decimal - Threshold for fuzzy matches - Default: 0.85
- `confidenceThreshold`: decimal - Threshold for high-confidence matches - Default: 0.90

**Optional Configuration**:
- `enableFuzzyMatching`: boolean - Enable fuzzy matching - Default: true
- `enableAddressMatching`: boolean - Enable address matching - Default: true
- `maxSearchResults`: integer - Maximum search results to analyze - Default: 100

**Environment-Specific Configuration**:
- Development: Relaxed thresholds, smaller search scope
- Production: Strict thresholds, full search scope

## 10. Integration Points
**API Contracts**:
- Input: Counterparty entity with identifying information
- Output: Duplicate check results with potential matches

**Data Exchange Formats**:
- **JSON**: Counterparty data and duplicate check results
- **CSV**: Bulk duplicate analysis reports

**Event Publishing**:
- **DuplicateCheckCompleted**: Published on successful check with results
- **DuplicatesFound**: Published when potential duplicates identified
- **CheckFailed**: Published on duplicate check failure with error details

**Event Consumption**:
- **CounterpartyCreated**: Triggers duplicate checking process
- **CounterpartyUpdated**: Triggers re-checking for duplicates
- **MatchingRulesUpdated**: Updates duplicate detection configuration
