package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for CollectionServiceClient using WireMock.
 */
class CollectionServiceClientTest extends BaseSourceServiceClientTest {

    @Override
    protected SourceServiceClient createClient(String baseUrl) {
        CollectionServiceClient client = new CollectionServiceClient(restTemplate, baseUrl);
        // Set API key for testing authentication
        ReflectionTestUtils.setField(client, "apiKey", "test-collection-key");
        ReflectionTestUtils.setField(client, "authHeader", "X-API-Key");
        return client;
    }

    @Override
    protected InterfaceType getInterfaceType() {
        return InterfaceType.COLLECTION;
    }

    @Override
    protected String getExpectedPayloadEndpoint(String transactionId) {
        return "/api/v1/collections/" + transactionId + "/payload";
    }

    @Override
    protected String getExpectedRetryEndpoint(String transactionId) {
        return "/api/v1/collections/" + transactionId + "/retry";
    }

    @Test
    void testAuthenticationHeadersIncluded() throws Exception {
        // Given
        String transactionId = "test-collection-auth-123";

        stubFor(get(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .withHeader("X-API-Key", equalTo("test-collection-key"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\": \"collection-test\"}")));

        // When
        var exception = com.arcone.biopro.exception.collector.domain.entity.InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(InterfaceType.COLLECTION)
                .build();

        client.getOriginalPayload(exception).get();

        // Then
        verify(getRequestedFor(urlEqualTo(getExpectedPayloadEndpoint(transactionId)))
                .withHeader("X-API-Key", equalTo("test-collection-key")));
    }

    @Test
    void testServiceName() {
        assertEquals("collection-service", client.getServiceName());
    }
}