package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.config.CacheConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CacheEvictionService.
 */
@ExtendWith(MockitoExtension.class)
class CacheEvictionServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache exceptionDetailsCache;

    @Mock
    private Cache payloadCache;

    @Mock
    private Cache summaryCache;

    @Mock
    private Cache searchCache;

    @Mock
    private Cache relatedCache;

    private CacheEvictionService cacheEvictionService;

    @BeforeEach
    void setUp() {
        cacheEvictionService = new CacheEvictionService(cacheManager);

        when(cacheManager.getCache(CacheConfig.EXCEPTION_DETAILS_CACHE)).thenReturn(exceptionDetailsCache);
        when(cacheManager.getCache(CacheConfig.PAYLOAD_CACHE)).thenReturn(payloadCache);
        when(cacheManager.getCache(CacheConfig.EXCEPTION_SUMMARY_CACHE)).thenReturn(summaryCache);
        when(cacheManager.getCache(CacheConfig.SEARCH_RESULTS_CACHE)).thenReturn(searchCache);
        when(cacheManager.getCache(CacheConfig.RELATED_EXCEPTIONS_CACHE)).thenReturn(relatedCache);
    }

    @Test
    void shouldEvictExceptionDetails() {
        String transactionId = "test-transaction-123";

        cacheEvictionService.evictExceptionDetails(transactionId);

        verify(exceptionDetailsCache).evict(transactionId);
    }

    @Test
    void shouldEvictPayloadCache() {
        String transactionId = "test-transaction-123";
        String interfaceType = "ORDER";

        cacheEvictionService.evictPayloadCache(transactionId, interfaceType);

        verify(payloadCache).evict(transactionId + ":" + interfaceType);
    }

    @Test
    void shouldEvictSummaryCaches() {
        cacheEvictionService.evictSummaryCaches();

        verify(summaryCache).clear();
    }

    @Test
    void shouldEvictSearchCaches() {
        cacheEvictionService.evictSearchCaches();

        verify(searchCache).clear();
    }

    @Test
    void shouldEvictRelatedExceptionsCache() {
        String customerId = "CUST001";

        cacheEvictionService.evictRelatedExceptionsCache(customerId);

        verify(relatedCache).clear();
    }

    @Test
    void shouldEvictCachesOnExceptionCreation() {
        InterfaceException exception = InterfaceException.builder()
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .customerId("CUST001")
                .build();

        cacheEvictionService.evictCachesOnExceptionCreation(exception);

        verify(summaryCache).clear();
        verify(searchCache).clear();
        verify(relatedCache).clear();
    }

    @Test
    void shouldEvictCachesOnStatusChange() {
        InterfaceException exception = InterfaceException.builder()
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .customerId("CUST001")
                .status(ExceptionStatus.ACKNOWLEDGED)
                .build();

        cacheEvictionService.evictCachesOnStatusChange(exception);

        verify(exceptionDetailsCache).evict("test-transaction-123");
        verify(summaryCache).clear();
        verify(searchCache).clear();
        verify(relatedCache).clear();
    }

    @Test
    void shouldEvictCachesOnRetry() {
        InterfaceException exception = InterfaceException.builder()
                .transactionId("test-transaction-123")
                .interfaceType(InterfaceType.ORDER)
                .customerId("CUST001")
                .build();

        cacheEvictionService.evictCachesOnRetry(exception);

        verify(exceptionDetailsCache).evict("test-transaction-123");
        verify(payloadCache).evict("test-transaction-123:ORDER");
        verify(summaryCache).clear();
        verify(relatedCache).clear();
    }
}