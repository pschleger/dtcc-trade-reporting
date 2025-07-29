# validatePosition Processor Specification

## 1. Component Overview
**Component Name**: validatePosition
**Component Type**: CyodaProcessor
**Business Domain**: Position Management
**Purpose**: Validates calculated position data for accuracy, completeness, and regulatory compliance
**Workflow Context**: PositionWorkflow (validating state)

## 2. Input Specifications
**Entity Type**: Position
**Required Fields**:
- `positionId`: string - Unique position identifier
- `calculatedValues`: object - Calculated position values
- `calculationTimestamp`: ISO-8601 timestamp - When calculation completed
- `sourceTradeCount`: integer - Number of trades in calculation
- `dataQualityScore`: decimal - Calculation data quality score

**Optional Fields**:
- `previousPosition`: object - Previous position for comparison
- `validationOverrides`: array - Pre-approved validation overrides
- `riskMetrics`: object - Associated risk calculations

**Configuration Parameters**:
- `attachEntity`: true - Attach entity to processing context
- `calculationNodesTags`: "validation" - Tags for validation processing nodes
- `responseTimeoutMs`: 30000 - Maximum processing time (30 seconds)
- `processParamId`: "01932b4e-7890-7123-8456-323456789abd" - Process parameter reference

**Context Data**:
- Position validation rules
- Tolerance thresholds
- Regulatory compliance requirements

## 3. Output Specifications
**Success Response**:
```json
{
  "status": "SUCCESS",
  "result": {
    "processedEntity": "Position",
    "processingTimestamp": "2024-01-15T16:15:00Z",
    "validationResults": {
      "accuracyValid": true,
      "completenessValid": true,
      "consistencyValid": true,
      "regulatoryCompliant": true,
      "validationScore": 0.98
    },
    "validationMetadata": {
      "checksPerformed": 25,
      "validationDuration": 15000,
      "warningsCount": 1,
      "errorsCount": 0
    }
  }
}
```

**Error Response**:
```json
{
  "status": "ERROR",
  "errorCode": "VALIDATION_ERROR",
  "errorMessage": "Position validation failed",
  "details": {
    "accuracyErrors": ["Notional amount variance exceeds threshold"],
    "completenessErrors": ["Missing risk metrics"],
    "consistencyErrors": ["Position dates inconsistent"],
    "regulatoryViolations": ["Position reporting threshold exceeded"]
  }
}
```

**Side Effects**:
- Updates Position entity validation status
- Creates validation audit trail
- Stores validation results for compliance
- Publishes PositionValidated event

## 4. Business Logic
**Processing Steps**:
1. Validate position data accuracy against source trades
2. Check completeness of all required position fields
3. Verify internal consistency of position data
4. Validate regulatory compliance requirements
5. Assess overall position data quality
6. Generate validation summary and recommendations
7. Store validation results for audit and reporting

**Business Rules**:
- **Accuracy Tolerance**: Position values must be within tolerance of trade aggregation
- **Data Completeness**: All required position fields must be present and valid
- **Internal Consistency**: Position data must be internally consistent
- **Regulatory Compliance**: Position must meet regulatory reporting requirements
- **Quality Threshold**: Overall validation score must exceed minimum threshold

**Algorithms**:
- Variance analysis comparing calculated vs expected values
- Completeness checking using field requirement matrix
- Consistency validation using cross-field validation rules
- Regulatory compliance checking using current rule sets

## 5. Validation Rules
**Pre-processing Validations**:
- **Calculation Completion**: Position calculation completed successfully
- **Data Availability**: All required validation data accessible
- **Timestamp Validity**: Calculation timestamp recent and valid

**Post-processing Validations**:
- **Validation Completeness**: All validation categories completed
- **Results Consistency**: Validation results internally consistent
- **Score Calculation**: Validation score calculated correctly

**Data Quality Checks**:
- **Source Data Integrity**: Source trade data not corrupted
- **Calculation Accuracy**: Position calculations mathematically correct
- **Regulatory Alignment**: Position data meets regulatory standards

## 6. Error Handling
**Error Categories**:
- **VALIDATION_ERROR**: Position data validation failures
- **ACCURACY_ERROR**: Position accuracy threshold violations
- **COMPLETENESS_ERROR**: Missing required position data
- **SYSTEM_ERROR**: Validation service or data access failures
- **TIMEOUT_ERROR**: Validation processing timeout exceeded

**Error Recovery**:
- Retry mechanism for transient data access errors (max 3 retries)
- Partial validation for non-critical components
- Manual review queue for complex validation failures

**Error Propagation**:
- Validation errors trigger transition to validation-failed state
- Error details stored for manual review and correction
- Critical errors escalated to position management team

## 7. Performance Requirements
**SLA Requirements**:
- **Response Time**: 25 seconds (95th percentile)
- **Throughput**: 20 validations per second
- **Availability**: 99.9% uptime during business hours

**Resource Requirements**:
- **CPU**: High intensity for validation calculations and comparisons
- **Memory**: 1GB per concurrent validation
- **I/O**: High for source data access and validation

**Scalability**:
- Horizontal scaling through validation node distribution
- Performance varies with position complexity and trade count
- Parallel processing for independent validation components

## 8. Dependencies
**Internal Dependencies**:
- **Trade Service**: Source trade data for validation
- **Calculation Service**: Position calculation verification
- **Configuration Service**: Validation rule configuration
- **Audit Service**: Validation result logging

**External Dependencies**:
- **Market Data Service**: Market data for validation calculations (SLA: 99.5% availability, 2s response)
- **Regulatory Service**: Current validation requirements (daily updates)

**Data Dependencies**:
- Source trade data with complete economics
- Position validation rules and thresholds
- Market data for validation calculations
- Regulatory compliance requirements

## 9. Configuration Parameters
**Required Configuration**:
- `accuracyTolerancePercent`: decimal - Accuracy tolerance threshold - Default: 0.01
- `dataQualityThreshold`: decimal - Minimum data quality score - Default: 0.95
- `validationLevel`: string - Validation strictness level - Default: "strict"

**Optional Configuration**:
- `enableAccuracyValidation`: boolean - Enable accuracy validation - Default: true
- `enableCompletenessValidation`: boolean - Enable completeness validation - Default: true
- `enableConsistencyValidation`: boolean - Enable consistency validation - Default: true

**Environment-Specific Configuration**:
- Development: Relaxed tolerances, extended timeouts
- Production: Strict tolerances, standard timeouts

## 10. Integration Points
**API Contracts**:
- Input: Position entity with calculated values
- Output: Validation results with detailed findings

**Data Exchange Formats**:
- **JSON**: Position data and validation results
- **CSV**: Detailed validation report for audit

**Event Publishing**:
- **PositionValidated**: Published on successful validation with results
- **ValidationFailed**: Published on validation failure with error details
- **ValidationWarning**: Published when warnings detected

**Event Consumption**:
- **PositionCalculated**: Triggers position validation process
- **ValidationRulesUpdated**: Updates validation configuration
- **RegulatoryRulesUpdated**: Updates regulatory compliance rules
