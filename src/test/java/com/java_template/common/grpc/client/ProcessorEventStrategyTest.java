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
 * ABOUTME: Tests for ProcessorEventStrategy, focusing on the recoverRequestIdFromCloudEvent method
 * which handles string-based parsing of potentially corrupted JSON to extract request IDs.
 */
@ExtendWith(MockitoExtension.class)
class ProcessorEventStrategyTest {

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
    void testRecoverRequestIdFromCloudEvent_ValidJsonWithQuotes() throws Exception {
        // Given
        String jsonData = "{\"requestId\": \"12345678-1234-1234-1234-123456789abc\", \"entityId\": \"entity123\"}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("12345678-1234-1234-1234-123456789abc", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_ValidJsonNoSpaces() throws Exception {
        // Given
        String jsonData = "{\"requestId\":\"test-request-id-456\",\"entityId\":\"entity123\"}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("test-request-id-456", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_SingleQuotes() throws Exception {
        // Given
        String jsonData = "{'requestId': 'single-quote-id-789', 'entityId': 'entity123'}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("single-quote-id-789", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_MixedQuotes() throws Exception {
        // Given
        String jsonData = "{\"requestId\": 'mixed-quote-id-101', \"entityId\": \"entity123\"}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("mixed-quote-id-101", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_MultilineJson() throws Exception {
        // Given
        String jsonData = "{\n  \"requestId\": \"multiline-id-202\",\n  \"entityId\": \"entity123\"\n}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("multiline-id-202", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_UuidFallbackPattern() throws Exception {
        // Given - corrupted JSON where quotes are missing but UUID is intact
        String jsonData = "{requestId: 12345678-1234-1234-1234-123456789abc, entityId: entity123}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("12345678-1234-1234-1234-123456789abc", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_GeneralPatternFallback() throws Exception {
        // Given - corrupted JSON with non-UUID requestId
        String jsonData = "{requestId: simple-request-id-303, entityId: entity123}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("simple-request-id-303", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_CaseInsensitive() throws Exception {
        // Given
        String jsonData = "{\"REQUESTID\": \"case-insensitive-id-404\", \"entityId\": \"entity123\"}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("case-insensitive-id-404", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_NullCloudEvent() throws Exception {
        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, (CloudEvent) null);

        // Then
        assertNull(result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_NullTextData() throws Exception {
        // Given
        when(cloudEvent.getTextData()).thenReturn(null);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertNull(result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_EmptyTextData() throws Exception {
        // Given
        when(cloudEvent.getTextData()).thenReturn("");

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertNull(result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_NoRequestIdField() throws Exception {
        // Given
        String jsonData = "{\"entityId\": \"entity123\", \"processorName\": \"test-processor\"}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertNull(result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_CorruptedJsonWithPartialRequestId() throws Exception {
        // Given - heavily corrupted JSON but requestId is still extractable
        String jsonData = "{\"req corrupted but requestId: \"recoverable-id-505\" still here}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("recoverable-id-505", result);
    }

    @Test
    void testRecoverRequestIdFromCloudEvent_RequestIdWithSpecialCharacters() throws Exception {
        // Given
        String jsonData = "{\"requestId\": \"req-id_with.special@chars#606\", \"entityId\": \"entity123\"}";
        when(cloudEvent.getTextData()).thenReturn(jsonData);

        // When
        String result = (String) recoverRequestIdMethod.invoke(processorEventStrategy, cloudEvent);

        // Then
        assertEquals("req-id_with.special@chars#606", result);
    }
}
