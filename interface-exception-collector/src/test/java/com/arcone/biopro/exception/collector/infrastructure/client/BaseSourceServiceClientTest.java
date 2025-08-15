package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base test class for source service clients using WireMock for external
 * service mocking.
 */
public abstract class BaseSourceServiceClientTest {

    protected WireMockServer wireMockServer;
    protected SourceServiceClient client;
    protected RestTemplate restTemplate;
    protected ObjectMapper objectMapper;

    protected abstract SourceServiceClient createClient(String baseUrl);

    protected abstract InterfaceType getInterfaceType();

    protected abstract String getExpectedPayloadEndpoint(String transactionId);

    protected abstract String getExpectedRetryEndpoint(String transactionId);

    @BeforeEach
    void setUp() {
        // Start WireMock server on random port
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        // Create RestTemplate and client
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        client = createClient("http://localhost:" + wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testGetOriginalPayload_Success() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-transaction-123";
        Object expectedPayload = createTestPayload();

        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(getInterfaceType())
                .build();

        // Mock successful response
        stubFor(get(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-Correlation-ID", equalTo(transactionId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asJsonString(expectedPayload))));

        // When
        CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertTrue(response.isRetrieved());
        assertEquals(transactionId, response.getTransactionId());
        assertEquals(getInterfaceType().name(), response.getInterfaceType());
        assertNotNull(response.getPayload());
        assertNull(response.getErrorMessage());

        // Verify the request was made correctly
        verify(getRequestedFor(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-Correlation-ID", equalTo(transactionId))
                .withHeader("User-Agent", equalTo("interface-exception-collector-service/1.0")));
    }

    @Test
    void testGetOriginalPayload_ServiceUnavailable() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-transaction-456";

        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(getInterfaceType())
                .build();

        // Mock service unavailable response
        stubFor(get(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        // When
        CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertFalse(response.isRetrieved());
        assertEquals(transactionId, response.getTransactionId());
        assertEquals(getInterfaceType().name(), response.getInterfaceType());
        assertNull(response.getPayload());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    void testGetOriginalPayload_Timeout() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-transaction-timeout";

        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(getInterfaceType())
                .build();

        // Mock timeout response
        stubFor(get(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .willReturn(aResponse()
                        .withFixedDelay(10000) // 10 second delay to trigger timeout
                        .withStatus(200)));

        // When
        CompletableFuture<PayloadResponse> future = client.getOriginalPayload(exception);
        PayloadResponse response = future.get();

        // Then
        assertNotNull(response);
        assertFalse(response.isRetrieved());
        assertEquals(transactionId, response.getTransactionId());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    void testSubmitRetry_Success() throws ExecutionException, InterruptedException {
        // Given
        String transactionId = "test-retry-123";
        Object payload = createTestPayload();

        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(getInterfaceType())
                .operation("CREATE_" + getInterfaceType().name())
                .retryCount(1)
                .build();

        Object expectedResponse = createTestRetryResponse();

        // Mock successful retry response
        stubFor(post(urlEqualTo(getExpectedRetryEndpoint(transactionId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-Correlation-ID", equalTo(transactionId))
                .withHeader("X-Retry-Attempt", equalTo("true"))
                .withHeader("X-Retry-Count", equalTo("2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asJsonString(expectedResponse))));

        // When
        CompletableFuture<ResponseEntity<Object>> future = client.submitRetry(exception, payload);
        ResponseEntity<Object> response = future.get();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify the request was made correctly
        verify(postRequestedFor(urlEqualTo(getExpectedRetryEndpoint(transactionId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-Correlation-ID", equalTo(transactionId))
                .withHeader("X-Retry-Attempt", equalTo("true"))
                .withHeader("X-Retry-Count", equalTo("2")));
    }

    @Test
    void testSubmitRetry_Failure() {
        // Given
        String transactionId = "test-retry-fail";
        Object payload = createTestPayload();

        InterfaceException exception = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(getInterfaceType())
                .operation("CREATE_" + getInterfaceType().name())
                .build();

        // Mock failure response
        stubFor(post(urlEqualTo(getExpectedRetryEndpoint(transactionId)))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // When & Then
        CompletableFuture<ResponseEntity<Object>> future = client.submitRetry(exception, payload);

        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    void testSupports() {
        // When & Then
        assertTrue(client.supports(getInterfaceType().name()));
        assertFalse(client.supports("UNKNOWN_TYPE"));
    }

    @Test
    void testGetServiceName() {
        // When & Then
        assertNotNull(client.getServiceName());
        assertFalse(client.getServiceName().isEmpty());
    }

    protected Object createTestPayload() {
        return new TestPayload("test-data", 123, true);
    }

    protected Object createTestRetryResponse() {
        return new TestRetryResponse("success", "Retry completed successfully");
    }

    protected String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Test data classes
    public static class TestPayload {
        public String data;
        public int number;
        public boolean flag;

        public TestPayload() {
        }

        public TestPayload(String data, int number, boolean flag) {
            this.data = data;
            this.number = number;
            this.flag = flag;
        }
    }

    public static class TestRetryResponse {
        public String status;
        public String message;

        public TestRetryResponse() {
        }

        public TestRetryResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}