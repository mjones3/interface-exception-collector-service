package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.application.service.PayloadRetrievalService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration test for the entire source service integration
 * using WireMock to simulate external services.
 */
class SourceServiceIntegrationTest {

    private WireMockServer orderServiceMock;
    private WireMockServer collectionServiceMock;
    private WireMockServer distributionServiceMock;

    private PayloadRetrievalService payloadRetrievalService;
    private SourceServiceClientRegistry clientRegistry;

    @BeforeEach
    void setUp() {
        // Start WireMock servers for each service
        orderServiceMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        collectionServiceMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        distributionServiceMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());

        orderServiceMock.start();
        collectionServiceMock.start();
        distributionServiceMock.start();

        // Create RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Create service clients
        OrderServiceClient orderClient = new OrderServiceClient(
                restTemplate, "http://localhost:" + orderServiceMock.port());
        CollectionServiceClient collectionClient = new CollectionServiceClient(
                restTemplate, "http://localhost:" + collectionServiceMock.port());
        DistributionServiceClient distributionClient = new DistributionServiceClient(
                restTemplate, "http://localhost:" + distributionServiceMock.port());

        // Create registry and service
        List<SourceServiceClient> clients = List.of(orderClient, collectionClient, distributionClient);
        clientRegistry = new SourceServiceClientRegistry(clients);
        payloadRetrievalService = new PayloadRetrievalService(clientRegistry);
    }

    @AfterEach
    void tearDown() {
        if (orderServiceMock != null)
            orderServiceMock.stop();
        if (collectionServiceMock != null)
            collectionServiceMock.stop();
        if (distributionServiceMock != null)
            distributionServiceMock.stop();
    }

    @Test
    void testEndToEndPayloadRetrieval_AllServices() throws Exception {
        // Given - Setup mocks for all services
        setupOrderServiceMock();
        setupCollectionServiceMock();
        setupDistributionServiceMock();

        // Test Order Service
        InterfaceException orderException = InterfaceException.builder()
                .transactionId("order-123")
                .interfaceType(InterfaceType.ORDER)
                .build();

        CompletableFuture<PayloadResponse> orderFuture = payloadRetrievalService.getOriginalPayload(orderException);
        PayloadResponse orderResponse = orderFuture.get();

        assertNotNull(orderResponse);
        assertTrue(orderResponse.isRetrieved());
        assertEquals("order-123", orderResponse.getTransactionId());
        assertEquals("ORDER", orderResponse.getInterfaceType());

        // Test Collection Service
        InterfaceException collectionException = InterfaceException.builder()
                .transactionId("collection-456")
                .interfaceType(InterfaceType.COLLECTION)
                .build();

        CompletableFuture<PayloadResponse> collectionFuture = payloadRetrievalService
                .getOriginalPayload(collectionException);
        PayloadResponse collectionResponse = collectionFuture.get();

        assertNotNull(collectionResponse);
        assertTrue(collectionResponse.isRetrieved());
        assertEquals("collection-456", collectionResponse.getTransactionId());
        assertEquals("COLLECTION", collectionResponse.getInterfaceType());

        // Test Distribution Service
        InterfaceException distributionException = InterfaceException.builder()
                .transactionId("distribution-789")
                .interfaceType(InterfaceType.DISTRIBUTION)
                .build();

        CompletableFuture<PayloadResponse> distributionFuture = payloadRetrievalService
                .getOriginalPayload(distributionException);
        PayloadResponse distributionResponse = distributionFuture.get();

        assertNotNull(distributionResponse);
        assertTrue(distributionResponse.isRetrieved());
        assertEquals("distribution-789", distributionResponse.getTransactionId());
        assertEquals("DISTRIBUTION", distributionResponse.getInterfaceType());
    }

    @Test
    void testEndToEndRetrySubmission() throws Exception {
        // Given - Setup retry mocks
        setupRetryMocks();

        InterfaceException exception = InterfaceException.builder()
                .transactionId("retry-test")
                .interfaceType(InterfaceType.ORDER)
                .operation("CREATE_ORDER")
                .build();

        Object payload = new TestPayload("retry-data");

        // When
        CompletableFuture<ResponseEntity<Object>> future = payloadRetrievalService.submitRetry(exception, payload);
        ResponseEntity<Object> response = future.get();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testCircuitBreakerFallback() throws Exception {
        // Given - Setup service to return 500 errors
        WireMock.configureFor("localhost", orderServiceMock.port());
        stubFor(get(urlMatching("/api/v1/orders/.*/payload"))
                .willReturn(aResponse().withStatus(500)));

        InterfaceException exception = InterfaceException.builder()
                .transactionId("circuit-breaker-test")
                .interfaceType(InterfaceType.ORDER)
                .build();

        // When - Make multiple calls to trigger circuit breaker
        for (int i = 0; i < 6; i++) {
            try {
                payloadRetrievalService.getOriginalPayload(exception).get();
            } catch (Exception e) {
                // Expected failures
            }
        }

        // Then - Circuit breaker should be open and return fallback
        CompletableFuture<PayloadResponse> future = payloadRetrievalService.getOriginalPayload(exception);
        PayloadResponse response = future.get();

        assertNotNull(response);
        assertFalse(response.isRetrieved());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    void testServiceUnavailableHandling() throws Exception {
        // Given - Service returns 503
        WireMock.configureFor("localhost", collectionServiceMock.port());
        stubFor(get(urlEqualTo("/api/v1/collections/unavailable-test/payload"))
                .willReturn(aResponse().withStatus(503).withBody("Service Unavailable")));

        InterfaceException exception = InterfaceException.builder()
                .transactionId("unavailable-test")
                .interfaceType(InterfaceType.COLLECTION)
                .build();

        // When
        CompletableFuture<PayloadResponse> future = payloadRetrievalService.getOriginalPayload(exception);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertFalse(response.isRetrieved());
        assertEquals("unavailable-test", response.getTransactionId());
        assertNotNull(response.getErrorMessage());
    }

    private void setupOrderServiceMock() {
        WireMock.configureFor("localhost", orderServiceMock.port());
        stubFor(get(urlEqualTo("/api/v1/orders/order-123/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"orderId\": \"order-123\", \"data\": \"order-payload\"}")));
    }

    private void setupCollectionServiceMock() {
        WireMock.configureFor("localhost", collectionServiceMock.port());
        stubFor(get(urlEqualTo("/api/v1/collections/collection-456/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"collectionId\": \"collection-456\", \"data\": \"collection-payload\"}")));
    }

    private void setupDistributionServiceMock() {
        WireMock.configureFor("localhost", distributionServiceMock.port());
        stubFor(get(urlEqualTo("/api/v1/distributions/distribution-789/payload"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"distributionId\": \"distribution-789\", \"data\": \"distribution-payload\"}")));
    }

    private void setupRetryMocks() {
        WireMock.configureFor("localhost", orderServiceMock.port());
        stubFor(post(urlEqualTo("/api/v1/orders/retry-test/retry"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"success\", \"message\": \"Retry completed\"}")));
    }

    // Test data class
    public static class TestPayload {
        public String data;

        public TestPayload() {
        }

        public TestPayload(String data) {
            this.data = data;
        }
    }
}