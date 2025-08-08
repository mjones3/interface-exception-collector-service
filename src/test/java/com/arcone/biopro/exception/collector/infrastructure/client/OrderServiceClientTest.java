package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for OrderServiceClient using WireMock.
 */
class OrderServiceClientTest extends BaseSourceServiceClientTest {

    @Override
    protected SourceServiceClient createClient(String baseUrl) {
        OrderServiceClient client = new OrderServiceClient(restTemplate, baseUrl);
        // Set API key for testing authentication
        ReflectionTestUtils.setField(client, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(client, "authHeader", "X-API-Key");
        return client;
    }

    @Override
    protected InterfaceType getInterfaceType() {
        return InterfaceType.ORDER;
    }

    @Override
    protected String getExpectedPayloadEndpoint(String transactionId) {
        return "/api/v1/orders/" + transactionId + "/payload";
    }

    @Override
    protected String getExpectedRetryEndpoint(String transactionId) {
        return "/api/v1/orders/" + transactionId + "/retry";
    }

    @Test
    void testAuthenticationHeadersIncluded() throws Exception {
        // Given
        String transactionId = "test-auth-123";

        stubFor(get(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .withHeader("X-API-Key", equalTo("test-api-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\": \"test\"}")));

        // When
        var exception = com.arcone.biopro.exception.collector.domain.entity.InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .build();

        client.getOriginalPayload(exception).get();

        // Then
        verify(getRequestedFor(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .withHeader("X-API-Key", equalTo("test-api-key")));
    }

    @Test
    void testServiceName() {
        assertEquals("order-service", client.getServiceName());
    }
}