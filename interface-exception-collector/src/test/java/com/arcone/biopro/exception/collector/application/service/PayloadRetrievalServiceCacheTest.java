package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.config.CacheConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClientRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for PayloadRetrievalService caching behavior.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PayloadRetrievalServiceCacheTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private PayloadRetrievalService payloadRetrievalService;

    @MockBean
    private SourceServiceClientRegistry clientRegistry;

    @MockBean
    private SourceServiceClient sourceServiceClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        cacheManager.getCache(CacheConfig.PAYLOAD_CACHE).clear();

        when(clientRegistry.getClient(any(InterfaceType.class))).thenReturn(sourceServiceClient);
    }

    @Test
    void shouldCachePayloadRetrievalResults() {
        // Given
        InterfaceException exception = InterfaceException.builder()
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .build();

        PayloadResponse expectedResponse = PayloadResponse.builder()
                .transactionId("test-transaction-123")
                .interfaceType("ORDER")
                .retrieved(true)
                .payload("test-payload")
                .build();

        when(sourceServiceClient.getOriginalPayload(exception))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When - First call
        CompletableFuture<PayloadResponse> result1 = payloadRetrievalService.getOriginalPayload(exception);
        PayloadResponse response1 = result1.join();

        // When - Second call (should be cached)
        CompletableFuture<PayloadResponse> result2 = payloadRetrievalService.getOriginalPayload(exception);
        PayloadResponse response2 = result2.join();

        // Then
        assertThat(response1).isEqualTo(expectedResponse);
        assertThat(response2).isEqualTo(expectedResponse);

        // Verify the source service was only called once (second call was cached)
        verify(sourceServiceClient, times(1)).getOriginalPayload(exception);
    }

    @Test
    void shouldCacheSynchronousPayloadRetrieval() {
        // Given
        String transactionId = "test-transaction-456";
        String interfaceType = "ORDER";

        InterfaceException tempException = InterfaceException.builder()
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .build();

        PayloadResponse expectedResponse = PayloadResponse.builder()
                .transactionId(transactionId)
                .interfaceType(interfaceType)
                .retrieved(true)
                .payload("test-payload-sync")
                .build();

        when(sourceServiceClient.getOriginalPayload(any(InterfaceException.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        // When - First call
        Object result1 = payloadRetrievalService.getOriginalPayload(transactionId, interfaceType);

        // When - Second call (should be cached)
        Object result2 = payloadRetrievalService.getOriginalPayload(transactionId, interfaceType);

        // Then
        assertThat(result1).isEqualTo("test-payload-sync");
        assertThat(result2).isEqualTo("test-payload-sync");

        // Verify the source service was only called once
        verify(sourceServiceClient, times(1)).getOriginalPayload(any(InterfaceException.class));
    }

    @Test
    void shouldNotCacheWhenTransactionIdIsNull() {
        // Given
        InterfaceException exception = InterfaceException.builder()
                .transactionId(null)
                .interfaceType(InterfaceType.ORDER)
                .build();

        PayloadResponse expectedResponse = PayloadResponse.builder()
                .transactionId(null)
                .interfaceType("ORDER")
                .retrieved(false)
                .errorMessage("No client available for interface type: ORDER")
                .build();

        // When - Multiple calls with null transaction ID
        CompletableFuture<PayloadResponse> result1 = payloadRetrievalService.getOriginalPayload(exception);
        CompletableFuture<PayloadResponse> result2 = payloadRetrievalService.getOriginalPayload(exception);

        // Then - Both calls should execute (not cached due to condition)
        assertThat(result1.join().getTransactionId()).isNull();
        assertThat(result2.join().getTransactionId()).isNull();
    }

    @Test
    void shouldUseDifferentCacheKeysForDifferentTransactions() {
        // Given
        InterfaceException exception1 = InterfaceException.builder()
                .transactionId("transaction-1")
                .interfaceType(InterfaceType.ORDER)
                .build();

        InterfaceException exception2 = InterfaceException.builder()
                .transactionId("transaction-2")
                .interfaceType(InterfaceType.ORDER)
                .build();

        PayloadResponse response1 = PayloadResponse.builder()
                .transactionId("transaction-1")
                .interfaceType("ORDER")
                .retrieved(true)
                .payload("payload-1")
                .build();

        PayloadResponse response2 = PayloadResponse.builder()
                .transactionId("transaction-2")
                .interfaceType("ORDER")
                .retrieved(true)
                .payload("payload-2")
                .build();

        when(sourceServiceClient.getOriginalPayload(exception1))
                .thenReturn(CompletableFuture.completedFuture(response1));
        when(sourceServiceClient.getOriginalPayload(exception2))
                .thenReturn(CompletableFuture.completedFuture(response2));

        // When
        PayloadResponse result1 = payloadRetrievalService.getOriginalPayload(exception1).join();
        PayloadResponse result2 = payloadRetrievalService.getOriginalPayload(exception2).join();

        // Then
        assertThat(result1.getPayload()).isEqualTo("payload-1");
        assertThat(result2.getPayload()).isEqualTo("payload-2");

        // Both should be called since they have different cache keys
        verify(sourceServiceClient, times(1)).getOriginalPayload(exception1);
        verify(sourceServiceClient, times(1)).getOriginalPayload(exception2);
    }
}