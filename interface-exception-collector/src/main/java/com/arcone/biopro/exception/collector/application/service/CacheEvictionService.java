package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.infrastructure.config.CacheConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service responsible for managing cache eviction strategies to maintain data
 * consistency.
 * Implements cache invalidation when exception data is modified.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheEvictionService {

    private final CacheManager cacheManager;

    /**
     * Evicts cache entries when an exception is updated.
     * This ensures data consistency when exception status, retry count, or other
     * fields change.
     *
     * @param exception the updated exception
     */
    @CacheEvict(value = {
            CacheConfig.EXCEPTION_DETAILS_CACHE,
            CacheConfig.EXCEPTION_SUMMARY_CACHE,
            CacheConfig.SEARCH_RESULTS_CACHE,
            CacheConfig.RELATED_EXCEPTIONS_CACHE
    }, allEntries = true)
    public void evictCachesOnExceptionUpdate(InterfaceException exception) {
        log.debug("Evicting caches due to exception update: {}", exception.getTransactionId());
    }

    /**
     * Evicts specific exception details cache entry.
     *
     * @param transactionId the transaction ID of the updated exception
     */
    @CacheEvict(value = CacheConfig.EXCEPTION_DETAILS_CACHE, key = "#transactionId")
    public void evictExceptionDetails(String transactionId) {
        log.debug("Evicting exception details cache for transaction: {}", transactionId);
    }

    /**
     * Evicts payload cache for a specific transaction.
     * Called when retry operations might change the payload or when payload is
     * updated.
     *
     * @param transactionId the transaction ID
     * @param interfaceType the interface type
     */
    @CacheEvict(value = CacheConfig.PAYLOAD_CACHE, key = "#transactionId + ':' + #interfaceType")
    public void evictPayloadCache(String transactionId, String interfaceType) {
        log.debug("Evicting payload cache for transaction: {}, interface: {}", transactionId, interfaceType);
    }

    /**
     * Evicts all summary caches when new exceptions are added or status changes
     * occur.
     * This ensures summary statistics remain accurate.
     */
    @CacheEvict(value = CacheConfig.EXCEPTION_SUMMARY_CACHE, allEntries = true)
    public void evictSummaryCaches() {
        log.debug("Evicting all exception summary caches");
    }

    /**
     * Evicts search result caches when exceptions are modified.
     * This ensures search results reflect the latest data.
     */
    @CacheEvict(value = CacheConfig.SEARCH_RESULTS_CACHE, allEntries = true)
    public void evictSearchCaches() {
        log.debug("Evicting all search result caches");
    }

    /**
     * Evicts related exceptions cache for a specific customer.
     *
     * @param customerId the customer ID
     */
    public void evictRelatedExceptionsCache(String customerId) {
        log.debug("Evicting related exceptions cache for customer: {}", customerId);

        // Since the cache key includes pagination parameters, we need to clear all
        // entries
        // for this customer by clearing the entire cache
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.RELATED_EXCEPTIONS_CACHE)).clear();
    }

    /**
     * Clears all caches. Used for administrative purposes or when major data
     * changes occur.
     */
    public void clearAllCaches() {
        log.info("Clearing all application caches");

        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            log.debug("Cleared cache: {}", cacheName);
        });
    }

    /**
     * Evicts caches when a new exception is created.
     * This ensures summary and search caches reflect the new data.
     *
     * @param exception the newly created exception
     */
    public void evictCachesOnExceptionCreation(InterfaceException exception) {
        log.debug("Evicting caches due to new exception creation: {}", exception.getTransactionId());

        // Evict summary caches to reflect new exception counts
        evictSummaryCaches();

        // Evict search caches to include new exception in search results
        evictSearchCaches();

        // Evict related exceptions cache for the customer
        if (exception.getCustomerId() != null) {
            evictRelatedExceptionsCache(exception.getCustomerId());
        }
    }

    /**
     * Evicts caches when an exception status changes.
     * This is particularly important for summary statistics and search results.
     *
     * @param exception the exception with updated status
     */
    public void evictCachesOnStatusChange(InterfaceException exception) {
        log.debug("Evicting caches due to status change for transaction: {}", exception.getTransactionId());

        // Evict the specific exception details
        evictExceptionDetails(exception.getTransactionId());

        // Evict summary caches as status counts have changed
        evictSummaryCaches();

        // Evict search caches as status-based searches need to reflect changes
        evictSearchCaches();

        // Evict related exceptions cache for the customer
        if (exception.getCustomerId() != null) {
            evictRelatedExceptionsCache(exception.getCustomerId());
        }
    }

    /**
     * Evicts caches when retry operations are performed.
     * This ensures retry history and status changes are reflected.
     *
     * @param exception the exception that was retried
     */
    public void evictCachesOnRetry(InterfaceException exception) {
        log.debug("Evicting caches due to retry operation for transaction: {}", exception.getTransactionId());

        // Evict the specific exception details to reflect retry history
        evictExceptionDetails(exception.getTransactionId());

        // Evict payload cache as retry might affect payload retrieval
        evictPayloadCache(exception.getTransactionId(), exception.getInterfaceType().name());

        // Evict summary caches if status changed due to retry
        evictSummaryCaches();

        // Evict related exceptions cache for the customer
        if (exception.getCustomerId() != null) {
            evictRelatedExceptionsCache(exception.getCustomerId());
        }
    }
}