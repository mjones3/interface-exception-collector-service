package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.ExceptionSummaryResponse;
import com.arcone.biopro.exception.collector.config.CacheConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for ExceptionQueryService caching behavior.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ExceptionQueryServiceCacheTest {

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
    void shouldCacheExceptionDetailsByTransactionId() {
        // Given
        String transactionId = "test-transaction-123";
        InterfaceException exception = InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId)
                .interfaceType(InterfaceType.ORDER)
                .status(ExceptionStatus.NEW)
                .build();

        when(exceptionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(exception));

        // When - First call
        Optional<InterfaceException> result1 = exceptionQueryService.findExceptionByTransactionId(transactionId);

        // When - Second call (should be cached)
        Optional<InterfaceException> result2 = exceptionQueryService.findExceptionByTransactionId(transactionId);

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get().getTransactionId()).isEqualTo(transactionId);
        assertThat(result2.get().getTransactionId()).isEqualTo(transactionId);

        // Verify repository was only called once
        verify(exceptionRepository, times(1)).findByTransactionId(transactionId);
    }

    @Test
    void shouldCacheRelatedExceptionsByCustomer() {
        // Given
        String customerId = "CUST001";
        String excludeTransactionId = "exclude-123";
        Pageable pageable = PageRequest.of(0, 10);

        InterfaceException exception = InterfaceException.builder()
                .id(1L)
                .transactionId("related-123")
                .customerId(customerId)
                .interfaceType(InterfaceType.ORDER)
                .build();

        Page<InterfaceException> expectedPage = new PageImpl<>(List.of(exception));

        when(exceptionRepository.findRelatedExceptionsByCustomer(customerId, excludeTransactionId, pageable))
                .thenReturn(expectedPage);

        // When - First call
        Page<InterfaceException> result1 = exceptionQueryService.findRelatedExceptionsByCustomer(
                customerId, excludeTransactionId, pageable);

        // When - Second call (should be cached)
        Page<InterfaceException> result2 = exceptionQueryService.findRelatedExceptionsByCustomer(
                customerId, excludeTransactionId, pageable);

        // Then
        assertThat(result1.getContent()).hasSize(1);
        assertThat(result2.getContent()).hasSize(1);
        assertThat(result1.getContent().get(0).getCustomerId()).isEqualTo(customerId);
        assertThat(result2.getContent().get(0).getCustomerId()).isEqualTo(customerId);

        // Verify repository was only called once
        verify(exceptionRepository, times(1)).findRelatedExceptionsByCustomer(customerId, excludeTransactionId,
                pageable);
    }

    @Test
    void shouldCacheSearchResults() {
        // Given
        String searchQuery = "test error";
        List<String> searchFields = List.of("exceptionReason");
        Pageable pageable = PageRequest.of(0, 10);

        InterfaceException exception = InterfaceException.builder()
                .id(1L)
                .transactionId("search-123")
                .exceptionReason("test error message")
                .interfaceType(InterfaceType.ORDER)
                .build();

        Page<InterfaceException> expectedPage = new PageImpl<>(List.of(exception));

        when(exceptionRepository.searchInFields(searchQuery, searchFields, pageable))
                .thenReturn(expectedPage);

        // When - First call
        Page<InterfaceException> result1 = exceptionQueryService.searchExceptions(searchQuery, searchFields, pageable);

        // When - Second call (should be cached)
        Page<InterfaceException> result2 = exceptionQueryService.searchExceptions(searchQuery, searchFields, pageable);

        // Then
        assertThat(result1.getContent()).hasSize(1);
        assertThat(result2.getContent()).hasSize(1);
        assertThat(result1.getContent().get(0).getExceptionReason()).contains("test error");
        assertThat(result2.getContent().get(0).getExceptionReason()).contains("test error");

        // Verify repository was only called once
        verify(exceptionRepository, times(1)).searchInFields(searchQuery, searchFields, pageable);
    }

    @Test
    void shouldCacheExceptionSummary() {
        // Given
        String timeRange = "week";
        String groupBy = "interfaceType";

        when(exceptionRepository.countByTimestampBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(10L);
        when(exceptionRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(exceptionRepository.getDailyCounts(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When - First call
        ExceptionSummaryResponse result1 = exceptionQueryService.getExceptionSummary(timeRange, groupBy);

        // When - Second call (should be cached)
        ExceptionSummaryResponse result2 = exceptionQueryService.getExceptionSummary(timeRange, groupBy);

        // Then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getTotalExceptions()).isEqualTo(10L);
        assertThat(result2.getTotalExceptions()).isEqualTo(10L);

        // Verify repository methods were only called once
        verify(exceptionRepository, times(1)).countByTimestampBetween(any(OffsetDateTime.class),
                any(OffsetDateTime.class));
    }

    @Test
    void shouldNotCacheWhenTransactionIdIsNull() {
        // Given
        String transactionId = null;

        // When - Multiple calls with null transaction ID
        Optional<InterfaceException> result1 = exceptionQueryService.findExceptionByTransactionId(transactionId);
        Optional<InterfaceException> result2 = exceptionQueryService.findExceptionByTransactionId(transactionId);

        // Then - Both calls should execute (not cached due to condition)
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();

        // Verify repository was called twice (not cached)
        verify(exceptionRepository, times(2)).findByTransactionId(transactionId);
    }

    @Test
    void shouldNotCacheSearchWhenQueryIsEmpty() {
        // Given
        String searchQuery = "";
        List<String> searchFields = List.of("exceptionReason");
        Pageable pageable = PageRequest.of(0, 10);

        // When - Multiple calls with empty search query
        Page<InterfaceException> result1 = exceptionQueryService.searchExceptions(searchQuery, searchFields, pageable);
        Page<InterfaceException> result2 = exceptionQueryService.searchExceptions(searchQuery, searchFields, pageable);

        // Then - Both calls should execute (not cached due to condition)
        verify(exceptionRepository, times(2)).searchInFields(eq(searchQuery), eq(List.of("exceptionReason")),
                eq(pageable));
    }

    @Test
    void shouldUseDifferentCacheKeysForDifferentParameters() {
        // Given
        String transactionId1 = "transaction-1";
        String transactionId2 = "transaction-2";

        InterfaceException exception1 = InterfaceException.builder()
                .id(1L)
                .transactionId(transactionId1)
                .interfaceType(InterfaceType.ORDER)
                .build();

        InterfaceException exception2 = InterfaceException.builder()
                .id(2L)
                .transactionId(transactionId2)
                .interfaceType(InterfaceType.COLLECTION)
                .build();

        when(exceptionRepository.findByTransactionId(transactionId1))
                .thenReturn(Optional.of(exception1));
        when(exceptionRepository.findByTransactionId(transactionId2))
                .thenReturn(Optional.of(exception2));

        // When
        Optional<InterfaceException> result1 = exceptionQueryService.findExceptionByTransactionId(transactionId1);
        Optional<InterfaceException> result2 = exceptionQueryService.findExceptionByTransactionId(transactionId2);

        // Then
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result1.get().getTransactionId()).isEqualTo(transactionId1);
        assertThat(result2.get().getTransactionId()).isEqualTo(transactionId2);

        // Both should be called since they have different cache keys
        verify(exceptionRepository, times(1)).findByTransactionId(transactionId1);
        verify(exceptionRepository, times(1)).findByTransactionId(transactionId2);
    }
}