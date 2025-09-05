package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerOrderServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RSocketRequester.Builder rSocketRequesterBuilder;

    @Mock
    private RSocketRequester rSocketRequester;

    @Mock
    private RSocketConnectionManager connectionManager;

    private ObjectMapper objectMapper;
    private PartnerOrderServiceClient client;
    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        client = new PartnerOrderServiceClient(restTemplate, "http://localhost:8090", 
                objectMapper, rSocketRequesterBuilder);
        
        // Set up test exception
        testException = new InterfaceException();
        testException.setId(1L);
        testException.setTransactionId("test-transaction-123");
        testException.setInterfaceType(InterfaceType.PARTNER_ORDER);
        testException.setExceptionReason("Test exception");
        testException.setOperation("CREATE_ORDER");
        testException.setExternalId("ORDER-123");
        testException.setStatus(ExceptionStatus.NEW);
        testException.setSeverity(ExceptionSeverity.HIGH);
        testException.setCategory("VALIDATION_ERROR");
        testException.setRetryable(true);
        testException.setCustomerId("CUST-001");
        testException.setLocationCode("LOC-001");
        testException.setTimestamp(Instant.now());
        testException.setProcessedAt(Instant.now());
        testException.setRetryCount(0);
        testException.setMaxRetries(3);
        testException.setCreatedAt(Instant.now());
        testException.setUpdatedAt(Instant.now());
    }

    @Test
    void shouldSupportPartnerOrderInterfaceType() {
        assertTrue(client.supports("PARTNER_ORDER"));
        assertTrue(client.supports(InterfaceType.PARTNER_ORDER.name()));
    }

    @Test
    void shouldSupportOrderInterfaceType() {
        assertTrue(client.supports("ORDER"));
        assertTrue(client.supports(InterfaceType.ORDER.name()));
    }

    @Test
    void shouldNotSupportOtherInterfaceTypes() {
        assertFalse(client.supports("COLLECTION"));
        assertFalse(client.supports("DISTRIBUTION"));
        assertFalse(client.supports("UNKNOWN"));
    }

    @Test
    void shouldReturnCorrectServiceName() {
        assertEquals("partner-order-service", client.getServiceName());
    }

    @Test
    void shouldRetrieveOriginalPayloadViaRSocketSuccessfully() throws Exception {
        // Arrange
        String expectedOrderData = "{\"externalId\":\"ORDER-123\",\"orderItems\":[{\"productFamily\":\"RED_BLOOD_CELLS\",\"bloodType\":\"O-\",\"quantity\":1}]}";
        
        when(rSocketRequesterBuilder.tcp(anyString(), anyInt())).thenReturn(rSocketRequester);
        when(rSocketRequester.route("orders.ORDER-123")).thenReturn(rSocketRequester);
        when(rSocketRequester.retrieveMono(String.class)).thenReturn(Mono.just(expectedOrderData));

        // Act
        CompletableFuture<PayloadResponse> future = client.getOriginalPayload(testException);
        PayloadResponse response = future.get();

        // Assert
        assertNotNull(response);
        assertTrue(response.isRetrieved());
        assertEquals(expectedOrderData, response.getPayload());
        assertEquals("test-transaction-123", response.getTransactionId());
        assertEquals("PARTNER_ORDER", response.getInterfaceType());
        assertEquals("partner-order-service-rsocket", response.getSourceService());
    }

    @Test
    void shouldHandleRSocketPayloadRetrievalFailure() throws Exception {
        // Arrange
        when(rSocketRequesterBuilder.tcp(anyString(), anyInt())).thenReturn(rSocketRequester);
        when(rSocketRequester.route("orders.ORDER-123")).thenReturn(rSocketRequester);
        when(rSocketRequester.retrieveMono(String.class)).thenReturn(Mono.error(new RuntimeException("RSocket connection failed")));

        // Act
        CompletableFuture<PayloadResponse> future = client.getOriginalPayload(testException);
        PayloadResponse response = future.get();

        // Assert
        assertNotNull(response);
        assertFalse(response.isRetrieved());
        assertTrue(response.getErrorMessage().contains("RSocket connection failed"));
        assertEquals("test-transaction-123", response.getTransactionId());
        assertEquals("partner-order-service-rsocket", response.getSourceService());
    }

    @Test
    void shouldSubmitRetryViaRestSuccessfully() throws Exception {
        // Arrange
        String orderData = "{\"externalId\":\"ORDER-123\",\"orderItems\":[{\"productFamily\":\"RED_BLOOD_CELLS\",\"bloodType\":\"O-\",\"quantity\":1}]}";
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Retry accepted", HttpStatus.ACCEPTED);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(mockResponse);

        // Act
        CompletableFuture<ResponseEntity<Object>> future = client.submitRetry(testException, orderData);
        ResponseEntity<Object> response = future.get();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Retry accepted", response.getBody());
    }

    @Test
    void shouldTransformOrderDataToPartnerOrderRequestFormat() throws Exception {
        // Arrange
        String orderData = "{\"externalId\":\"ORDER-123\",\"locationCode\":\"LOC-001\",\"orderItems\":[{\"productFamily\":\"RED_BLOOD_CELLS\",\"bloodType\":\"O-\",\"quantity\":2}]}";
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Retry accepted", HttpStatus.ACCEPTED);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    // Capture the request body to verify transformation
                    Object requestBody = invocation.getArgument(2, org.springframework.http.HttpEntity.class).getBody();
                    JsonNode transformedData = objectMapper.valueToTree(requestBody);
                    
                    // Verify the transformation
                    assertEquals("ORDER-123", transformedData.get("externalId").asText());
                    assertEquals("OPEN", transformedData.get("orderStatus").asText());
                    assertEquals("LOC-001", transformedData.get("locationCode").asText());
                    assertEquals("CUSTOMER", transformedData.get("shipmentType").asText());
                    assertEquals("BLOOD_PRODUCTS", transformedData.get("productCategory").asText());
                    
                    JsonNode orderItems = transformedData.get("orderItems");
                    assertTrue(orderItems.isArray());
                    assertEquals(1, orderItems.size());
                    assertEquals("RED_BLOOD_CELLS", orderItems.get(0).get("productFamily").asText());
                    assertEquals("O-", orderItems.get(0).get("bloodType").asText());
                    assertEquals(2, orderItems.get(0).get("quantity").asInt());
                    
                    return mockResponse;
                });

        // Act
        CompletableFuture<ResponseEntity<Object>> future = client.submitRetry(testException, orderData);
        ResponseEntity<Object> response = future.get();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void shouldAddRetryHeadersForRetryRequests() throws Exception {
        // Arrange
        String orderData = "{\"externalId\":\"ORDER-123\"}";
        testException.setRetryCount(2);
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Retry accepted", HttpStatus.ACCEPTED);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    // Verify retry headers are present
                    org.springframework.http.HttpEntity<?> httpEntity = invocation.getArgument(2);
                    org.springframework.http.HttpHeaders headers = httpEntity.getHeaders();
                    
                    assertEquals("3", headers.getFirst("X-Retry-Attempt"));
                    assertEquals("test-transaction-123", headers.getFirst("X-Original-Transaction-ID"));
                    
                    return mockResponse;
                });

        // Act
        CompletableFuture<ResponseEntity<Object>> future = client.submitRetry(testException, orderData);
        ResponseEntity<Object> response = future.get();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void shouldHandleRetrySubmissionFailure() throws Exception {
        // Arrange
        String orderData = "{\"externalId\":\"ORDER-123\"}";
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("REST service unavailable"));

        // Act & Assert
        CompletableFuture<ResponseEntity<Object>> future = client.submitRetry(testException, orderData);
        
        try {
            future.get();
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("REST retry submission failed"));
        }
    }
}