package com.java_template.common.grpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.common.workflow.CyodaContextFactory;
import com.java_template.common.workflow.OperationFactory;
import io.cloudevents.v1.proto.CloudEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * ABOUTME: Integration tests for ProcessorEventStrategy demonstrating the recoverRequestIdFromCloudEvent method
 * working with realistic corrupted JSON scenarios that might occur in production.
 */
@ExtendWith(MockitoExtension.class)
class ProcessorEventStrategyIntegrationTest {

    @Mock
    private OperationFactory operationFactory;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CyodaContextFactory eventContextFactory;

    @Mock
    private CloudEvent cloudEvent;

    private ProcessorEventStrategy processorEventStrategy;
    private Method recoverRequestIdMethod;

    @BeforeEach
    void setUp() throws Exception {
        processorEventStrategy = new ProcessorEventStrategy(operationFactory, objectMapper, eventContextFactory);
        
        // Get access to the private method for testing
        recoverRequestIdMethod = ProcessorEventStrategy.class.getDeclaredMethod("recoverRequestIdFromCloudEvent", CloudEvent.class);
        recoverRequestIdMethod.setAccessible(true);
    }

    @Test
    void testRecoverRequestId_RealWorldCorruptedJson_MissingQuotes() throws Exception {
        // Given - Real-world scenario where JSON parsing failed due to missing quotes
        String corruptedJson = """
            {
                requestId: 550e8400-e29b-41d4-a716-446655440000,
                entityId: entity-12345,
                processorName: calculateRisk,
                payload: {
                    data: {
                        amount: 1000.50,
                        currency: USD
                    }
                }
            }
            """;
        when(cloudEvent.getTextData()).thenReturn(corruptedJson);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("550e8400-e29b-41d4-a716-446655440000", result);
    }

    @Test
    void testRecoverRequestId_RealWorldCorruptedJson_TruncatedJson() throws Exception {
        // Given - Real-world scenario where JSON was truncated during transmission
        String truncatedJson = """
            {"requestId": "abc123-def456-ghi789", "entityId": "entity-999", "processorName": "validateData", "payload": {"data": {"field1": "value1", "field2":
            """;
        when(cloudEvent.getTextData()).thenReturn(truncatedJson);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("abc123-def456-ghi789", result);
    }

    @Test
    void testRecoverRequestId_RealWorldCorruptedJson_ExtraCharacters() throws Exception {
        // Given - Real-world scenario where extra characters were inserted
        String corruptedJson = """
            {
                "requestId": "req-id-12345-abcde",
                "entityId": "entity-567",
                "processorName": "processPayment",
                "payload": {
                    "data": {
                        "amount": 250.75,
                        "currency": "EUR"
                    }
                },
                "extraField": "shouldNotAffectParsing"
            }CORRUPTED_EXTRA_DATA_HERE
            """;
        when(cloudEvent.getTextData()).thenReturn(corruptedJson);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("req-id-12345-abcde", result);
    }

    @Test
    void testRecoverRequestId_RealWorldCorruptedJson_MixedCaseField() throws Exception {
        // Given - Real-world scenario where field name has different casing
        String corruptedJson = """
            {
                "REQUESTID": "UPPER-CASE-REQUEST-ID-789",
                "entityId": "entity-abc",
                "processorName": "validateUser"
            }
            """;
        when(cloudEvent.getTextData()).thenReturn(corruptedJson);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("UPPER-CASE-REQUEST-ID-789", result);
    }

    @Test
    void testRecoverRequestId_RealWorldCorruptedJson_EmbeddedInLargePayload() throws Exception {
        // Given - Real-world scenario where requestId is buried in a large payload
        String largeCorruptedJson = """
            {
                "timestamp": "2024-01-15T10:30:00Z",
                "source": "payment-service",
                "metadata": {
                    "version": "1.0",
                    "correlationId": "corr-123"
                },
                "requestId": "embedded-req-id-456",
                "entityId": "large-entity-789",
                "processorName": "complexProcessor",
                "payload": {
                    "data": {
                        "customer": {
                            "id": "cust-123",
                            "name": "John Doe",
                            "address": {
                                "street": "123 Main St",
                                "city": "Anytown",
                                "country": "USA"
                            }
                        },
                        "transaction": {
                            "amount": 1500.00,
                            "currency": "USD",
                            "description": "Payment for services"
                        }
                    }
                }
            }
            """;
        when(cloudEvent.getTextData()).thenReturn(largeCorruptedJson);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("embedded-req-id-456", result);
    }

    @Test
    void testRecoverRequestId_RealWorldCorruptedJson_NoRequestIdFound() throws Exception {
        // Given - Real-world scenario where requestId field is completely missing
        String jsonWithoutRequestId = """
            {
                "entityId": "entity-without-request-id",
                "processorName": "someProcessor",
                "payload": {
                    "data": {
                        "field1": "value1",
                        "field2": "value2"
                    }
                }
            }
            """;
        when(cloudEvent.getTextData()).thenReturn(jsonWithoutRequestId);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertNull(result);
    }

    @Test
    void testRecoverRequestId_EdgeCase_RequestIdAtBeginning() throws Exception {
        // Given - Edge case where requestId is the first field
        String jsonData = """
            {"requestId": "first-field-req-id", "entityId": "entity-first"}
            """;
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("first-field-req-id", result);
    }

    @Test
    void testRecoverRequestId_EdgeCase_RequestIdAtEnd() throws Exception {
        // Given - Edge case where requestId is the last field
        String jsonData = """
            {"entityId": "entity-last", "processorName": "lastProcessor", "requestId": "last-field-req-id"}
            """;
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("last-field-req-id", result);
    }
}
