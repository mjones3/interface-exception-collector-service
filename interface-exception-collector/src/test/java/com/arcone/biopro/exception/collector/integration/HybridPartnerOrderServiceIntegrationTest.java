package com.arcone.biopro.exception.collector.integration;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.client.PartnerOrderServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Integration test for the hybrid Partner Order Service flow.
 * Tests both RSocket retrieval and REST retry functionality.
 */
@SpringBootTest
@ActiveProfiles("test")
class HybridPartnerOrderServiceIntegrationTest {

    @Autowired
    private PartnerOrderServiceClient partnerOrderServiceClient;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private RSocketRequester.Builder rSocketRequesterBuilder;

    @MockBean
    private RSocketRequester rSocketRequester;

    private InterfaceException testException;

    @BeforeEach
    void setUp() {
        // Create test exception
        testException = new InterfaceException();
        testException.setTransactionId("hybrid-test-" + System.currentTimeMillis());
        testException.setInterfaceType(InterfaceType.ORDER);
        testException.setExternalId("HYBRID-ORDER-123");
        testException.setExceptionReason("Test order rejection for hybrid flow");
        testException.setOperation("CREATE_ORDER");
        testException.setStatus(ExceptionStatus.NEW);
        testException.setRetryable(true);
        testException.setCustomerId("CUST-HYBRID-001");
        testException.setLocationCode("LOC-HYBRID-001");
        testException.setTimestamp(Instant.now());
        testException.setProcessedAt(Instant.now());
        testException.setRetryCount(0);
        testException.setMaxRetries(3);
        testException.setCreatedAt(Instant.now());
        testException.setUpdatedAt(Instant.now());

        // Save test exception
        testException = exceptionRepository.save(testException);
    }

    @Test
    void shouldRetrieveOrderDataViaRSocketSuccessfully() throws Exception {
        // Given
        String mockOrderData = createMockOrderData();
        
        when(rSocketRequesterBuilder.tcp(anyString(), anyInt())).thenReturn(rSocketRequester);
        when(rSocketRequester.route("orders.HYBRID-ORDER-123")).thenReturn(rSocketRequester);
        when(rSocketRequester.retrieveMono(String.class)).thenReturn(Mono.just(mockOrderData));

        // When
        CompletableFuture<PayloadResponse> future = partnerOrderServiceClient.getOriginalPayload(testException);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertTrue(response.isRetrieved());
        assertEquals(mockOrderData, response.getPayload());
        assertEquals(testException.getTransactionId(), response.getTransactionId());
        assertEquals("ORDER", response.getInterfaceType());
        assertEquals("partner-order-service-rsocket", response.getSourceService());
        assertNull(response.getErrorMessage());
    }

    @Test
    void shouldHandleRSocketConnectionFailure() throws Exception {
        // Given
        when(rSocketRequesterBuilder.tcp(anyString(), anyInt())).thenReturn(rSocketRequester);
        when(rSocketRequester.route("orders.HYBRID-ORDER-123")).thenReturn(rSocketRequester);
        when(rSocketRequester.retrieveMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));

        // When
        CompletableFuture<PayloadResponse> future = partnerOrderServiceClient.getOriginalPayload(testException);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertFalse(response.isRetrieved());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("Connection refused"));
        assertEquals("partner-order-service-rsocket", response.getSourceService());
    }

    @Test
    void shouldSubmitRetryViaRestWithCorrectTransformation() throws Exception {
        // Given
        String orderData = createMockOrderData();
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(
                createMockPartnerOrderResponse(), HttpStatus.ACCEPTED);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    // Verify the transformation
                    Object requestBody = invocation.getArgument(2, org.springframework.http.HttpEntity.class).getBody();
                    JsonNode transformedData = objectMapper.valueToTree(requestBody);
                    
                    // Verify required fields
                    assertEquals("HYBRID-ORDER-123", transformedData.get("externalId").asText());
                    assertEquals("OPEN", transformedData.get("orderStatus").asText());
                    assertEquals("LOC-HYBRID-001", transformedData.get("locationCode").asText());
                    assertEquals("CUSTOMER", transformedData.get("shipmentType").asText());
                    assertEquals("BLOOD_PRODUCTS", transformedData.get("productCategory").asText());
                    
                    // Verify order items transformation
                    JsonNode orderItems = transformedData.get("orderItems");
                    assertTrue(orderItems.isArray());
                    assertTrue(orderItems.size() > 0);
                    
                    JsonNode firstItem = orderItems.get(0);
                    assertNotNull(firstItem.get("productFamily"));
                    assertNotNull(firstItem.get("bloodType"));
                    assertNotNull(firstItem.get("quantity"));
                    
                    return mockResponse;
                });

        // When
        CompletableFuture<ResponseEntity<Object>> future = partnerOrderServiceClient.submitRetry(testException, orderData);
        ResponseEntity<Object> response = future.get();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldIncludeRetryHeadersInRestRequest() throws Exception {
        // Given
        testException.setRetryCount(2);
        String orderData = createMockOrderData();
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success", HttpStatus.ACCEPTED);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    // Verify retry headers
                    org.springframework.http.HttpEntity<?> httpEntity = invocation.getArgument(2);
                    org.springframework.http.HttpHeaders headers = httpEntity.getHeaders();
                    
                    assertEquals("3", headers.getFirst("X-Retry-Attempt"));
                    assertEquals(testException.getTransactionId(), headers.getFirst("X-Original-Transaction-ID"));
                    assertEquals("application/json", headers.getFirst("Content-Type"));
                    
                    return mockResponse;
                });

        // When
        CompletableFuture<ResponseEntity<Object>> future = partnerOrderServiceClient.submitRetry(testException, orderData);
        ResponseEntity<Object> response = future.get();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void shouldHandleRestRetryFailure() throws Exception {
        // Given
        String orderData = createMockOrderData();
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenThrow(new RuntimeException("Partner order service unavailable"));

        // When & Then
        CompletableFuture<ResponseEntity<Object>> future = partnerOrderServiceClient.submitRetry(testException, orderData);
        
        assertThrows(Exception.class, () -> future.get());
    }

    @Test
    void shouldTransformComplexOrderDataCorrectly() throws Exception {
        // Given
        String complexOrderData = createComplexMockOrderData();
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success", HttpStatus.ACCEPTED);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    Object requestBody = invocation.getArgument(2, org.springframework.http.HttpEntity.class).getBody();
                    JsonNode transformedData = objectMapper.valueToTree(requestBody);
                    
                    // Verify complex transformation
                    JsonNode orderItems = transformedData.get("orderItems");
                    assertEquals(2, orderItems.size());
                    
                    // First item
                    JsonNode item1 = orderItems.get(0);
                    assertEquals("RED_BLOOD_CELLS_LEUKOREDUCED", item1.get("productFamily").asText());
                    assertEquals("O-", item1.get("bloodType").asText());
                    assertEquals(2, item1.get("quantity").asInt());
                    
                    // Second item
                    JsonNode item2 = orderItems.get(1);
                    assertEquals("PLATELETS", item2.get("productFamily").asText());
                    assertEquals("A+", item2.get("bloodType").asText());
                    assertEquals(1, item2.get("quantity").asInt());
                    
                    return mockResponse;
                });

        // When
        CompletableFuture<ResponseEntity<Object>> future = partnerOrderServiceClient.submitRetry(testException, complexOrderData);
        ResponseEntity<Object> response = future.get();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void shouldHandleEmptyOrderItemsGracefully() throws Exception {
        // Given
        String orderDataWithoutItems = "{\"externalId\":\"HYBRID-ORDER-123\",\"locationCode\":\"LOC-001\"}";
        ResponseEntity<Object> mockResponse = new ResponseEntity<>("Success", HttpStatus.ACCEPTED);
        
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenAnswer(invocation -> {
                    Object requestBody = invocation.getArgument(2, org.springframework.http.HttpEntity.class).getBody();
                    JsonNode transformedData = objectMapper.valueToTree(requestBody);
                    
                    // Should create default order items
                    JsonNode orderItems = transformedData.get("orderItems");
                    assertTrue(orderItems.isArray());
                    assertEquals(1, orderItems.size());
                    
                    JsonNode defaultItem = orderItems.get(0);
                    assertEquals("RED_BLOOD_CELLS_LEUKOREDUCED", defaultItem.get("productFamily").asText());
                    assertEquals("O-", defaultItem.get("bloodType").asText());
                    assertEquals(1, defaultItem.get("quantity").asInt());
                    
                    return mockResponse;
                });

        // When
        CompletableFuture<ResponseEntity<Object>> future = partnerOrderServiceClient.submitRetry(testException, orderDataWithoutItems);
        ResponseEntity<Object> response = future.get();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void shouldSupportBothOrderAndPartnerOrderInterfaceTypes() {
        // Test ORDER interface type
        assertTrue(partnerOrderServiceClient.supports(InterfaceType.ORDER.name()));
        
        // Test PARTNER_ORDER interface type
        assertTrue(partnerOrderServiceClient.supports(InterfaceType.PARTNER_ORDER.name()));
        
        // Test unsupported types
        assertFalse(partnerOrderServiceClient.supports(InterfaceType.COLLECTION.name()));
        assertFalse(partnerOrderServiceClient.supports(InterfaceType.DISTRIBUTION.name()));
    }

    private String createMockOrderData() {
        try {
            return objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("externalId", "HYBRID-ORDER-123")
                    .put("customerId", "CUST-HYBRID-001")
                    .put("locationCode", "LOC-HYBRID-001")
                    .put("orderStatus", "PENDING")
                    .put("productCategory", "BLOOD_PRODUCTS")
                    .set("orderItems", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("productFamily", "RED_BLOOD_CELLS_LEUKOREDUCED")
                                    .put("bloodType", "O-")
                                    .put("quantity", 2)
                                    .put("comments", "Urgent request"))));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock order data", e);
        }
    }

    private String createComplexMockOrderData() {
        try {
            return objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("externalId", "HYBRID-ORDER-123")
                    .put("customerId", "CUST-HYBRID-001")
                    .put("locationCode", "LOC-HYBRID-001")
                    .put("orderStatus", "PENDING")
                    .put("productCategory", "BLOOD_PRODUCTS")
                    .put("createDate", "2025-01-15 10:30:00")
                    .put("deliveryType", "ROUTINE")
                    .set("orderItems", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("productFamily", "RED_BLOOD_CELLS_LEUKOREDUCED")
                                    .put("bloodType", "O-")
                                    .put("quantity", 2)
                                    .put("comments", "Urgent request"))
                            .add(objectMapper.createObjectNode()
                                    .put("productFamily", "PLATELETS")
                                    .put("bloodType", "A+")
                                    .put("quantity", 1)
                                    .put("comments", "Standard delivery"))));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create complex mock order data", e);
        }
    }

    private Object createMockPartnerOrderResponse() {
        try {
            return objectMapper.createObjectNode()
                    .put("status", "ACCEPTED")
                    .put("transactionId", testException.getTransactionId())
                    .put("externalId", "HYBRID-ORDER-123")
                    .put("message", "Order accepted for processing");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock partner order response", e);
        }
    }
}