package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Optimized repository for mutation operations with enhanced queries
 * specifically designed for GraphQL mutation validation and performance.
 * 
 * This repository focuses on:
 * - Fast transaction ID lookups for mutation validation
 * - Efficient retry limit checking
 * - Status validation queries
 * - Optimized queries for mutation operations
 */
@Repository
public interface OptimizedExceptionRepository extends JpaRepository<InterfaceException, Long> {

    /**
     * Find exception by transaction ID with optimized query for mutation validation.
     * Uses query hints for performance optimization and includes only essential fields
     * needed for mutation validation.
     * 
     * @param transactionId the unique transaction identifier
     * @return Optional containing the exception if found
     */
    @Query("SELECT ie FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "10"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "mutation-validation")
    })
    Optional<InterfaceException> findByTransactionIdOptimized(@Param("transactionId") String transactionId);

    /**
     * Find retryable exception by transaction ID with status validation.
     * Optimized for retry mutation validation - checks if exception exists,
     * is retryable, and is in a valid state for retry operations.
     * 
     * @param transactionId the unique transaction identifier
     * @param retryableStatuses list of statuses that allow retry operations
     * @return Optional containing the exception if retryable
     */
    @Query("SELECT ie FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId " +
           "AND ie.retryable = true " +
           "AND ie.status IN :retryableStatuses")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "10"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Optional<InterfaceException> findRetryableExceptionByTransactionId(
        @Param("transactionId") String transactionId,
        @Param("retryableStatuses") List<ExceptionStatus> retryableStatuses);

    /**
     * Find acknowledgeable exception by transaction ID with status validation.
     * Optimized for acknowledge mutation validation - checks if exception exists
     * and is in a valid state for acknowledgment operations.
     * 
     * @param transactionId the unique transaction identifier
     * @param acknowledgeableStatuses list of statuses that allow acknowledgment
     * @return Optional containing the exception if acknowledgeable
     */
    @Query("SELECT ie FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId " +
           "AND ie.status IN :acknowledgeableStatuses")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "10"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Optional<InterfaceException> findAcknowledgeableExceptionByTransactionId(
        @Param("transactionId") String transactionId,
        @Param("acknowledgeableStatuses") List<ExceptionStatus> acknowledgeableStatuses);

    /**
     * Find resolvable exception by transaction ID with status validation.
     * Optimized for resolve mutation validation - checks if exception exists
     * and is in a valid state for resolution operations.
     * 
     * @param transactionId the unique transaction identifier
     * @param resolvableStatuses list of statuses that allow resolution
     * @return Optional containing the exception if resolvable
     */
    @Query("SELECT ie FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId " +
           "AND ie.status IN :resolvableStatuses")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "10"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Optional<InterfaceException> findResolvableExceptionByTransactionId(
        @Param("transactionId") String transactionId,
        @Param("resolvableStatuses") List<ExceptionStatus> resolvableStatuses);

    /**
     * Check if exception has exceeded retry limits.
     * Optimized query that only returns the retry count and max retries
     * for efficient limit checking without loading the full entity.
     * 
     * @param transactionId the unique transaction identifier
     * @return Array containing [retryCount, maxRetries] or null if not found
     */
    @Query("SELECT ie.retryCount, ie.maxRetries FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "5"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Object[] getRetryLimits(@Param("transactionId") String transactionId);

    /**
     * Count pending retry attempts for an exception.
     * Optimized query to check if there are any pending retries
     * that would prevent new retry operations.
     * 
     * @param transactionId the unique transaction identifier
     * @return count of pending retry attempts
     */
    @Query("SELECT COUNT(ra) FROM RetryAttempt ra " +
           "JOIN ra.interfaceException ie " +
           "WHERE ie.transactionId = :transactionId " +
           "AND ra.status = 'PENDING'")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "5"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    long countPendingRetries(@Param("transactionId") String transactionId);

    /**
     * Check if exception exists and get basic validation info.
     * Returns minimal data needed for mutation validation:
     * [id, status, retryable, retryCount, maxRetries]
     * 
     * @param transactionId the unique transaction identifier
     * @return Array with validation data or null if not found
     */
    @Query("SELECT ie.id, ie.status, ie.retryable, ie.retryCount, ie.maxRetries " +
           "FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "5"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Object[] getValidationInfo(@Param("transactionId") String transactionId);

    /**
     * Find exception with retry attempts for cancel retry validation.
     * Optimized for cancel retry mutation - loads exception with retry attempts
     * to validate if there are active retries that can be cancelled.
     * 
     * @param transactionId the unique transaction identifier
     * @return Optional containing exception with retry attempts if found
     */
    @Query("SELECT ie FROM InterfaceException ie " +
           "LEFT JOIN FETCH ie.retryAttempts ra " +
           "WHERE ie.transactionId = :transactionId " +
           "AND EXISTS (SELECT 1 FROM RetryAttempt ra2 " +
           "           WHERE ra2.interfaceException = ie " +
           "           AND ra2.status IN ('PENDING', 'IN_PROGRESS'))")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "10"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false")
    })
    Optional<InterfaceException> findExceptionWithActiveRetries(@Param("transactionId") String transactionId);

    /**
     * Batch validation for multiple transaction IDs.
     * Optimized for bulk operations - returns validation info for multiple
     * exceptions in a single query to reduce database round trips.
     * 
     * @param transactionIds list of transaction IDs to validate
     * @return List of validation data arrays [transactionId, status, retryable, retryCount, maxRetries]
     */
    @Query("SELECT ie.transactionId, ie.status, ie.retryable, ie.retryCount, ie.maxRetries " +
           "FROM InterfaceException ie " +
           "WHERE ie.transactionId IN :transactionIds")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "15"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "100"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Object[]> getBatchValidationInfo(@Param("transactionIds") List<String> transactionIds);

    /**
     * Check if exception is in a cancellable retry state.
     * Fast query to determine if cancel retry operation is valid
     * without loading the full entity or retry attempts.
     * 
     * @param transactionId the unique transaction identifier
     * @return true if exception has cancellable retries, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(ra) > 0 THEN true ELSE false END " +
           "FROM RetryAttempt ra " +
           "JOIN ra.interfaceException ie " +
           "WHERE ie.transactionId = :transactionId " +
           "AND ra.status IN ('PENDING', 'IN_PROGRESS')")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "5"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    boolean hasCancellableRetries(@Param("transactionId") String transactionId);

    /**
     * Get exception status for quick validation.
     * Minimal query that only returns the status for fast validation
     * without loading any other entity data.
     * 
     * @param transactionId the unique transaction identifier
     * @return the exception status or null if not found
     */
    @Query("SELECT ie.status FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "3"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    ExceptionStatus getStatusByTransactionId(@Param("transactionId") String transactionId);

    /**
     * Check if exception exists (fast existence check).
     * Optimized existence check that doesn't load any entity data,
     * just returns boolean result for transaction ID validation.
     * 
     * @param transactionId the unique transaction identifier
     * @return true if exception exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(ie) > 0 THEN true ELSE false END " +
           "FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "3"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    boolean existsByTransactionIdOptimized(@Param("transactionId") String transactionId);

    /**
     * Find exception for mutation with optimistic locking.
     * Loads exception with version for optimistic locking to prevent
     * concurrent modification issues during mutation operations.
     * 
     * @param transactionId the unique transaction identifier
     * @return Optional containing exception with version info
     */
    @Query("SELECT ie FROM InterfaceException ie " +
           "WHERE ie.transactionId = :transactionId")
    @QueryHints({
        @QueryHint(name = "org.hibernate.timeout", value = "10"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "1"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"),
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    Optional<InterfaceException> findByTransactionIdForUpdate(@Param("transactionId") String transactionId);
}