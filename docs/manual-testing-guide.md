# Manual Testing Guide for FpML Trade Confirmation API

This guide provides examples for manually testing the FpML trade confirmation processing API using real FpML samples.

## Prerequisites

1. Start the application: `./gradlew bootRun`
2. The API will be available at: `http://localhost:8080`

## Test Samples

The following FpML samples are available in `src/test/resources/fpml-samples/`:

- `irs-trade-confirmation.xml` - Valid Interest Rate Swap
- `fx-forward-confirmation.xml` - Valid FX Forward
- `invalid-fpml.xml` - Invalid FpML structure
- `malformed-xml.xml` - Malformed XML

## API Endpoints

### 1. Submit Trade Confirmation

**Endpoint:** `POST /api/v1/trade-confirmations`

**Headers:**
- `Content-Type: application/json`
- `X-Correlation-ID: <optional-correlation-id>`

**Request Body Example (IRS Trade):**

```bash
# First, encode the FpML content to base64
FPML_CONTENT=$(base64 -i src/test/resources/fpml-samples/irs-trade-confirmation.xml)

# Submit the trade confirmation
curl -X POST http://localhost:8080/api/v1/trade-confirmations \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-correlation-001" \
  -d '{
    "messageId": "MANUAL-TEST-IRS-001",
    "messageType": "TRADE_CONFIRMATION",
    "fpmlVersion": "5.12",
    "senderLei": "1234567890ABCDEFGH12",
    "receiverLei": "ZYXWVUTSRQ9876543201",
    "fpmlContent": "'$FPML_CONTENT'",
    "correlationId": "test-correlation-001"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Trade confirmation processed successfully",
  "data": {
    "messageId": "MANUAL-TEST-IRS-001",
    "processingId": "uuid-generated-id",
    "processingStatus": "PROCESSED",
    "validationStatus": "VALID",
    "receivedTimestamp": "2025-07-30T...",
    "processedTimestamp": "2025-07-30T...",
    "validationResults": [...],
    "extractedTradeData": {...}
  },
  "correlationId": "test-correlation-001",
  "timestamp": "2025-07-30T...",
  "apiVersion": "v1"
}
```

### 2. FX Forward Trade Example

```bash
# Encode FX Forward FpML
FPML_CONTENT=$(base64 -i src/test/resources/fpml-samples/fx-forward-confirmation.xml)

curl -X POST http://localhost:8080/api/v1/trade-confirmations \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-correlation-002" \
  -d '{
    "messageId": "MANUAL-TEST-FX-001",
    "messageType": "TRADE_CONFIRMATION",
    "fpmlVersion": "5.12",
    "senderLei": "1234567890ABCDEFGH12",
    "receiverLei": "ZYXWVUTSRQ9876543201",
    "fpmlContent": "'$FPML_CONTENT'",
    "correlationId": "test-correlation-002"
  }'
```

### 3. Invalid FpML Example

```bash
# Test with invalid FpML structure
FPML_CONTENT=$(base64 -i src/test/resources/fpml-samples/invalid-fpml.xml)

curl -X POST http://localhost:8080/api/v1/trade-confirmations \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-correlation-003" \
  -d '{
    "messageId": "MANUAL-TEST-INVALID-001",
    "messageType": "TRADE_CONFIRMATION",
    "fpmlVersion": "5.12",
    "senderLei": "1234567890ABCDEFGH12",
    "receiverLei": "ZYXWVUTSRQ9876543201",
    "fpmlContent": "'$FPML_CONTENT'",
    "correlationId": "test-correlation-003"
  }'
```

**Expected Response (Validation Errors):**
```json
{
  "success": true,
  "statusCode": 200,
  "data": {
    "messageId": "MANUAL-TEST-INVALID-001",
    "processingStatus": "FAILED",
    "validationStatus": "INVALID",
    "validationResults": [
      {
        "validationType": "SCHEMA",
        "severity": "ERROR",
        "errorCode": "INVALID_ROOT",
        "errorMessage": "Document must have FpML or dataDocument root element"
      }
    ]
  }
}
```

### 4. Get Trade Confirmation Status

**Endpoint:** `GET /api/v1/trade-confirmations/{messageId}/status`

```bash
curl -X GET http://localhost:8080/api/v1/trade-confirmations/MANUAL-TEST-IRS-001/status \
  -H "X-Correlation-ID: test-correlation-004"
```

**Expected Response:**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Status retrieved successfully",
  "data": {
    "messageId": "MANUAL-TEST-IRS-001",
    "status": "PROCESSED",
    "timestamp": "2025-07-30T..."
  },
  "correlationId": "test-correlation-004"
}
```

### 5. Get Trade Confirmation Details

**Endpoint:** `GET /api/v1/trade-confirmations/{messageId}`

```bash
curl -X GET http://localhost:8080/api/v1/trade-confirmations/MANUAL-TEST-IRS-001 \
  -H "X-Correlation-ID: test-correlation-005"
```

## Validation Error Examples

### Invalid LEI Format

```bash
curl -X POST http://localhost:8080/api/v1/trade-confirmations \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "INVALID-LEI-TEST",
    "messageType": "TRADE_CONFIRMATION",
    "fpmlVersion": "5.12",
    "senderLei": "INVALID_LEI",
    "receiverLei": "ZYXWVUTSRQ9876543201",
    "fpmlContent": "dGVzdA=="
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "statusCode": 400,
  "message": "Validation failed",
  "validationErrors": [
    {
      "field": "senderLei",
      "code": "Pattern",
      "message": "Sender LEI must be a valid 20-character LEI code",
      "rejectedValue": "INVALID_LEI"
    }
  ]
}
```

### Missing Required Fields

```bash
curl -X POST http://localhost:8080/api/v1/trade-confirmations \
  -H "Content-Type: application/json" \
  -d '{
    "messageType": "TRADE_CONFIRMATION",
    "fpmlVersion": "5.12"
  }'
```

## Testing Checklist

- [ ] Valid IRS trade confirmation processes successfully
- [ ] Valid FX forward confirmation processes successfully
- [ ] Invalid FpML structure returns validation errors
- [ ] Malformed XML returns processing errors
- [ ] Invalid LEI format returns validation error (400)
- [ ] Missing required fields return validation errors (400)
- [ ] Status endpoint returns correct status
- [ ] Details endpoint returns trade information
- [ ] Correlation IDs are properly tracked
- [ ] Response times are reasonable (< 5 seconds)

## Performance Testing

For load testing, you can use the provided samples with tools like Apache Bench:

```bash
# Create a test file with the JSON payload
echo '{...}' > test-payload.json

# Run load test
ab -n 100 -c 10 -T application/json -p test-payload.json \
   http://localhost:8080/api/v1/trade-confirmations
```
