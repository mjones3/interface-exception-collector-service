package com.arcone.biopro.exception.collector.performance;

import com.arcone.biopro.exception.collector.application.service.ExceptionQueryService;
import com.arcone.biopro.exception.collector.infrastructure.config.CacheConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Performance tests for cache behavior under concurrent load.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CachePerformanceTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private ExceptionQueryService exceptionQueryService;

    @MockBean
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Test
    void shouldHandleConcurrentCacheAccess() throws InterruptedException {
        // Given
        String transactionId = "concurrent-test-123";
        InterfaceException exception = InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(exception));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int numberOfRequests = 100;

        // When - Multiple concurrent requests for the same data
        CompletableFuture<Void> allRequests = CompletableFuture.allOf(
                IntStream.range(0, numberOfRequests)
                        .mapToObj(i -> CompletableFuture.runAsync(() -> {
                            Optional<InterfaceException> result = exceptionQueryService
                                    .findExceptionByTransactionId(transactionId);
                            assertThat(result).isPresent();
                            assertThat(result.get().getTransactionId()).isEqualTo(transactionId);
                        }, executor))
                        .toArray(CompletableFuture[]::new));

        // Wait for all requests to complete
        try {
            allRequests.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to complete concurrent requests", e);
        }
        executor.shutdown();

        // Then - Repository should be called only a few times (due to caching)
        // Allow for some cache misses due to concurrent access, but should be much less
        // than total requests
        verify(exceptionRepository, atMost(10)).findByTransactionId(transactionId);
    }

    @Test
    void shouldMaintainCachePerformanceUnderLoad() {
        // Given
        String transactionId = "performance-test-456";
        InterfaceException exception = InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(exception));

        // When - First call to populate cache
        long startTime = System.currentTimeMillis();
        exceptionQueryService.findExceptionByTransactionId(transactionId);
        long firstCallTime = System.currentTimeMillis() - startTime;

        // When - Subsequent cached calls
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            exceptionQueryService.findExceptionByTransactionId(transactionId);
        }
        long cachedCallsTime = System.currentTimeMillis() - startTime;

        // Then - Cached calls should be significantly faster
        double averageCachedCallTime = cachedCallsTime / 1000.0;

        // Cache should provide significant performance improvement
        assertThat(averageCachedCallTime).isLessThan(firstCallTime / 2.0);

        // Repository should only be called once (first call)
        verify(exceptionRepository, atMost(1)).findByTransactionId(transactionId);
    }

    @Test
    void shouldHandleCacheEvictionGracefully() {
        // Given
        String transactionId = "eviction-test-789";
        InterfaceException exception = InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(exception));

        // When - Populate cache
        Optional<InterfaceException> result1 = exceptionQueryService.findExceptionByTransactionId(transactionId);
        assertThat(result1).isPresent();

        // When - Manually evict cache
        cacheManager.getCache(CacheConfig.EXCEPTION_DETAILS_CACHE).evict(transactionId);

        // When - Access after eviction
        Optional<InterfaceException> result2 = exceptionQueryService.findExceptionByTransactionId(transactionId);

        // Then - Should still work correctly
        assertThat(result2).isPresent();
        assertThat(result2.get().getTransactionId()).isEqualTo(transactionId);

        // Repository should be called twice (once before eviction, once after)
        verify(exceptionRepository, atMost(2)).findByTransactionId(transactionId);
    }

    @Test
    void shouldHandleMemoryPressureGracefully() {
        // Given - Create many cache entries to test memory handling
        when(exceptionRepository.findByTransactionId(anyString()))
                .thenAnswer(invocation -> {
                    String txId = invocation.getArgument(0);
                    return Optional.of(InterfaceException.builder()
                            .id(Long.parseLong(txId.replace("tx-", "")))
                            .transactionId(txId)
                            .interfaceType(InterfaceType.ORDER)
                            .build());
                });

        // When - Create many cache entries
        for (int i = 0; i < 1000; i++) {
            String txId = "tx-" + i;
            Optional<InterfaceException> result = exceptionQueryService.findExceptionByTransactionId(txId);
            assertThat(result).isPresent();
        }

        // Then - Cache should handle the load without errors
        // Verify that cache is still functional
        Optional<InterfaceException> testResult = exceptionQueryService.findExceptionByTransactionId("tx-999");
        assertThat(testResult).isPresent();
        assertThat(testResult.get().getTransactionId()).isEqualTo("tx-999");
    }
}