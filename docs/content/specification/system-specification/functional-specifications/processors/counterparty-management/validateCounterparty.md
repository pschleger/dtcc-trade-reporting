# validateCounterparty Processor Specification

## 1. Component Overview
**Component Name**: validateCounterparty
**Component Type**: CyodaProcessor
**Business Domain**: Counterparty Data Management
**Purpose**: Validates counterparty data and LEI information for regulatory compliance and data quality
**Workflow Context**: CounterpartyWorkflow (validating state)

## 2. Input Specifications
**Entity Type**: Counterparty
**Required Fields**:
- `counterpartyId`: string - Unique counterparty identifier
- `leiCode`: string - Legal Entity Identifier (LEI)
- `entityName`: string - Legal entity name
- `entityType`: string - Type of legal entity
- `jurisdictionCode`: string - Jurisdiction of incorporation

**Optional Fields**:
- `parentLei`: string - Parent entity LEI if applicable
- `registrationNumber`: string - Business registration number
- `addressInformation`: object - Registered address details
- `contactInformation`: object - Contact details

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "validation" - Tags for validation processing nodes
- `responseTimeoutMs`: 20000 - Maximum processing time (20 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-523456789abc" - Process parameter reference

**Context Data**:
- LEI registry access credentials
- Validation rule configuration
- Jurisdiction-specific requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Counterparty",
    "processingTimestamp": "2024-01-15T12:00:00Z",
    "validationResults": {
      "leiValid": true,
      "leiStatus": "ISSUED",
      "entityDataValid": true,
      "jurisdictionValid": true,
      "dataQualityScore": 0.97
    },
    "leiRegistryData": {
      "legalName": "Example Corporation Ltd",
      "registrationStatus": "ACTIVE",
      "lastUpdateDate": "2024-01-10",
      "expirationDate": "2025-01-10"
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "VALIDATION_ERROR",
  "errorMessage": "Counterparty validation failed",
  "details": {
    "leiErrors": ["LEI not found in registry"],
    "dataErrors": ["Invalid jurisdiction code"],
    "formatErrors": ["Entity name contains invalid characters"],
    "systemErrors": ["LEI registry service unavailable"]
  }
}
```

**Side Effects**:
- Updates Counterparty entity validation status
- Caches LEI registry data for performance
- Creates validation audit trail
- Publishes CounterpartyValidated event

## 4. Business Logic
**Processing Steps**:
1. Validate LEI format and checksum
2. Query LEI registry for entity verification
3. Validate entity data against registry information
4. Check jurisdiction and regulatory compliance
5. Assess data quality and completeness
6. Generate validation summary and recommendations
7. Update entity with validation results

**Business Rules**:
- **LEI Compliance**: LEI must be valid and active in GLEIF registry
- **Entity Matching**: Entity name must match or be consistent with LEI registry
- **Jurisdiction Validation**: Jurisdiction code must be valid ISO country code
- **Data Quality**: All required fields must be present and properly formatted
- **Status Validation**: LEI status must be "ISSUED" for active counterparties

**Algorithms**:
- LEI checksum validation using ISO 17442 standard
- Entity name matching using fuzzy string matching
- Jurisdiction validation using ISO 3166 country codes
- Data quality scoring using weighted field validation

## 5. Validation Rules
**Pre-processing Validations**:
- **LEI Format**: LEI must be 20-character alphanumeric string
- **Required Fields**: All mandatory counterparty fields present
- **Data Format**: All fields in correct format and within length limits

**Post-processing Validations**:
- **Registry Consistency**: Entity data consistent with LEI registry
- **Validation Completeness**: All validation checks completed
- **Data Quality**: Overall data quality score calculated

**Data Quality Checks**:
- **LEI Registry Match**: Entity data matches LEI registry information
- **Address Validation**: Address information complete and valid
- **Contact Validation**: Contact information valid and reachable

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: LEI or entity data validation failures
- **REGISTRY_ERROR**: LEI registry access or data issues
- **FORMAT_ERROR**: Data format or structure violations
- **SYSTEM_ERROR**: External service or infrastructure failures
- **TIMEOUT_ERROR**: Validation processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient registry errors (max 3 retries)
- Fallback to cached LEI data for minor outages
- Partial validation for non-critical data elements

**Error Propagation**:
- Validation errors trigger transition to validation-failed state
- Error details stored for manual review and correction
- Critical errors escalated to counterparty management team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 15 seconds (95th percentile)
- **Throughput**: 50 validations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: Medium intensity for validation logic and registry calls
- **Memory**: 256MB per concurrent validation
- **I/O**: High for LEI registry access

**Scalability**:
- Horizontal scaling through validation node distribution
- Performance limited by LEI registry response times
- Caching improves performance for repeated validations

## 8. Dependencies
**Internal Dependencies**:
- **Configuration Service**: Validation rule configuration
- **Cache Service**: LEI registry data caching
- **Audit Service**: Validation result logging

**External Dependencies**:
- **GLEIF LEI Registry**: LEI validation and entity data (SLA: 99.5% availability, 3s response)
- **ISO Standards Service**: Country and jurisdiction code validation (cached locally)

**Data Dependencies**:
- LEI registry access credentials and endpoints
- Validation rule configuration
- Jurisdiction and country code mappings
- Data quality scoring criteria

## 9. Configuration Parameters
**Required Configuration**:
- `leiRegistryUrl`: string - LEI registry API endpoint
- `leiRegistryApiKey`: string - API key for registry access
- `dataQualityThreshold`: decimal - Minimum data quality score - Default: 0.90

**Optional Configuration**:
- `enableRegistryValidation`: boolean - Enable LEI registry validation - Default: true
- `cacheExpiryHours`: integer - LEI data cache expiry - Default: 24
- `strictMatching`: boolean - Enable strict entity name matching - Default: false

**Environment-Specific Configuration**:
- Development: Mock LEI registry, relaxed validation
- Production: Live LEI registry, strict validation

## 10. Integration Points
**API Contracts**:
- Input: Counterparty entity with LEI and entity data
- Output: Validation results with LEI registry information

**Data Exchange Formats**:
- **JSON**: Counterparty data and validation results
- **XML**: LEI registry response format

**Event Publishing**:
- **CounterpartyValidated**: Published on successful validation with results
- **ValidationFailed**: Published on validation failure with error details
- **LeiDataUpdated**: Published when LEI registry data updated

**Event Consumption**:
- **CounterpartyCreated**: Triggers counterparty validation
- **LeiRegistryUpdated**: Updates cached LEI registry data
- **ValidationRulesUpdated**: Updates validation configuration
