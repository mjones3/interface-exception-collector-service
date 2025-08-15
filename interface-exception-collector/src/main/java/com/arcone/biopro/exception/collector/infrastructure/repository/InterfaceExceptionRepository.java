package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InterfaceException entity providing data access
 * operations.
 * Extends JpaRepository for basic CRUD operations and includes custom query
 * methods
 * for filtering, searching, and aggregation operations.
 */
@Repository
public interface InterfaceExceptionRepository
                extends JpaRepository<InterfaceException, Long>, InterfaceExceptionRepositoryCustom {

        /**
         * Find an exception by its transaction ID.
         * 
         * @param transactionId the unique transaction identifier
         * @return Optional containing the exception if found
         */
        Optional<InterfaceException> findByTransactionId(String transactionId);

        /**
         * Check if an exception exists with the given transaction ID.
         * 
         * @param transactionId the unique transaction identifier
         * @return true if exception exists, false otherwise
         */
        boolean existsByTransactionId(String transactionId);

        /**
         * Find all exceptions with pagination and sorting support.
         * 
         * @param pageable pagination and sorting parameters
         * @return Page of exceptions
         */
        Page<InterfaceException> findAll(Pageable pageable);

        /**
         * Find exceptions by interface type with pagination.
         * 
         * @param interfaceType the interface type to filter by
         * @param pageable      pagination and sorting parameters
         * @return Page of exceptions matching the interface type
         */
        Page<InterfaceException> findByInterfaceType(InterfaceType interfaceType, Pageable pageable);

        /**
         * Find exceptions by status with pagination.
         * 
         * @param status   the exception status to filter by
         * @param pageable pagination and sorting parameters
         * @return Page of exceptions matching the status
         */
        Page<InterfaceException> findByStatus(ExceptionStatus status, Pageable pageable);

        /**
         * Find exceptions by severity with pagination.
         * 
         * @param severity the exception severity to filter by
         * @param pageable pagination and sorting parameters
         * @return Page of exceptions matching the severity
         */
        Page<InterfaceException> findBySeverity(ExceptionSeverity severity, Pageable pageable);

        /**
         * Find exceptions by customer ID with pagination.
         * 
         * @param customerId the customer ID to filter by
         * @param pageable   pagination and sorting parameters
         * @return Page of exceptions for the specified customer
         */
        Page<InterfaceException> findByCustomerId(String customerId, Pageable pageable);

        /**
         * Find exceptions within a date range with pagination.
         * 
         * @param fromDate start date (inclusive)
         * @param toDate   end date (inclusive)
         * @param pageable pagination and sorting parameters
         * @return Page of exceptions within the date range
         */
        Page<InterfaceException> findByTimestampBetween(OffsetDateTime fromDate, OffsetDateTime toDate,
                        Pageable pageable);

        /**
         * Complex filtering query supporting multiple optional filters.
         * Uses simple JPQL with proper null handling to avoid parameter type issues.
         * 
         * @param interfaceType optional interface type filter
         * @param status        optional status filter
         * @param severity      optional severity filter
         * @param customerId    optional customer ID filter
         * @param fromDate      optional start date filter
         * @param toDate        optional end date filter
         * @param pageable      pagination and sorting parameters
         * @return Page of exceptions matching the specified filters
         */
        @Query("SELECT ie FROM InterfaceException ie WHERE " +
                        "(:interfaceType IS NULL OR ie.interfaceType = :interfaceType) AND " +
                        "(:status IS NULL OR ie.status = :status) AND " +
                        "(:severity IS NULL OR ie.severity = :severity) AND " +
                        "(:customerId IS NULL OR ie.customerId = :customerId) AND " +
                        "(:fromDate IS NULL OR ie.timestamp >= :fromDate) AND " +
                        "(:toDate IS NULL OR ie.timestamp <= :toDate)")
        Page<InterfaceException> findWithFilters(
                        @Param("interfaceType") InterfaceType interfaceType,
                        @Param("status") ExceptionStatus status,
                        @Param("severity") ExceptionSeverity severity,
                        @Param("customerId") String customerId,
                        @Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate,
                        Pageable pageable);

        /**
         * Full-text search across exception reason field using PostgreSQL text search.
         * 
         * @param searchQuery the search query string
         * @param pageable    pagination and sorting parameters
         * @return Page of exceptions matching the search query in exception reason
         */
        @Query(value = "SELECT * FROM interface_exceptions ie WHERE " +
                        "to_tsvector('english', ie.exception_reason) @@ plainto_tsquery('english', :searchQuery)", nativeQuery = true)
        Page<InterfaceException> searchByExceptionReason(@Param("searchQuery") String searchQuery, Pageable pageable);

        /**
         * Full-text search across external ID field using PostgreSQL text search.
         * 
         * @param searchQuery the search query string
         * @param pageable    pagination and sorting parameters
         * @return Page of exceptions matching the search query in external ID
         */
        @Query(value = "SELECT * FROM interface_exceptions ie WHERE " +
                        "ie.external_id IS NOT NULL AND " +
                        "to_tsvector('english', ie.external_id) @@ plainto_tsquery('english', :searchQuery)", nativeQuery = true)
        Page<InterfaceException> searchByExternalId(@Param("searchQuery") String searchQuery, Pageable pageable);

        /**
         * Full-text search across operation field using PostgreSQL text search.
         * 
         * @param searchQuery the search query string
         * @param pageable    pagination and sorting parameters
         * @return Page of exceptions matching the search query in operation
         */
        @Query(value = "SELECT * FROM interface_exceptions ie WHERE " +
                        "to_tsvector('english', ie.operation) @@ plainto_tsquery('english', :searchQuery)", nativeQuery = true)
        Page<InterfaceException> searchByOperation(@Param("searchQuery") String searchQuery, Pageable pageable);

        /**
         * Multi-field full-text search across specified fields.
         * 
         * @param searchQuery  the search query string
         * @param searchFields list of fields to search in (exceptionReason, externalId,
         *                     operation)
         * @param pageable     pagination and sorting parameters
         * @return Page of exceptions matching the search query in any of the specified
         *         fields
         */
        @Query("SELECT DISTINCT ie FROM InterfaceException ie WHERE " +
                        "(:#{#searchFields.contains('exceptionReason')} = true AND " +
                        " LOWER(ie.exceptionReason) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) OR " +
                        "(:#{#searchFields.contains('externalId')} = true AND ie.externalId IS NOT NULL AND " +
                        " LOWER(ie.externalId) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) OR " +
                        "(:#{#searchFields.contains('operation')} = true AND " +
                        " LOWER(ie.operation) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
        Page<InterfaceException> searchInFields(
                        @Param("searchQuery") String searchQuery,
                        @Param("searchFields") List<String> searchFields,
                        Pageable pageable);

        /**
         * Count total exceptions.
         * 
         * @return total number of exceptions
         */
        long count();

        /**
         * Count exceptions by interface type.
         * 
         * @param interfaceType the interface type
         * @return count of exceptions for the interface type
         */
        long countByInterfaceType(InterfaceType interfaceType);

        /**
         * Count exceptions by status.
         * 
         * @param status the exception status
         * @return count of exceptions with the status
         */
        long countByStatus(ExceptionStatus status);

        /**
         * Count exceptions by severity.
         * 
         * @param severity the exception severity
         * @return count of exceptions with the severity
         */
        long countBySeverity(ExceptionSeverity severity);

        /**
         * Count exceptions within a date range.
         * 
         * @param fromDate start date (inclusive)
         * @param toDate   end date (inclusive)
         * @return count of exceptions within the date range
         */
        long countByTimestampBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

        /**
         * Get daily exception counts for trend analysis.
         * 
         * @param fromDate start date for trend analysis
         * @param toDate   end date for trend analysis
         * @return List of daily counts with date and count
         */
        @Query("SELECT DATE(ie.timestamp) as date, COUNT(ie) as count " +
                        "FROM InterfaceException ie " +
                        "WHERE ie.timestamp BETWEEN :fromDate AND :toDate " +
                        "GROUP BY DATE(ie.timestamp) " +
                        "ORDER BY DATE(ie.timestamp)")
        List<Object[]> getDailyCounts(@Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate);

        /**
         * Find exceptions for the same customer to identify related issues.
         * 
         * @param customerId           the customer ID
         * @param excludeTransactionId transaction ID to exclude from results
         * @param pageable             pagination parameters
         * @return Page of related exceptions for the customer
         */
        @Query("SELECT ie FROM InterfaceException ie WHERE " +
                        "ie.customerId = :customerId AND ie.transactionId != :excludeTransactionId " +
                        "ORDER BY ie.timestamp DESC")
        Page<InterfaceException> findRelatedExceptionsByCustomer(
                        @Param("customerId") String customerId,
                        @Param("excludeTransactionId") String excludeTransactionId,
                        Pageable pageable);

        /**
         * Find exceptions that require critical alerting based on severity and retry
         * count.
         * 
         * @param criticalSeverity the critical severity level
         * @param maxRetryCount    maximum retry count threshold
         * @return List of exceptions requiring critical alerts
         */
        @Query("SELECT ie FROM InterfaceException ie WHERE " +
                        "ie.severity = :criticalSeverity OR ie.retryCount > :maxRetryCount")
        List<InterfaceException> findCriticalExceptions(
                        @Param("criticalSeverity") ExceptionSeverity criticalSeverity,
                        @Param("maxRetryCount") Integer maxRetryCount);

        /**
         * Find retryable exceptions that haven't been resolved.
         * 
         * @param pageable pagination parameters
         * @return Page of retryable exceptions
         */
        @Query("SELECT ie FROM InterfaceException ie WHERE " +
                        "ie.retryable = true AND ie.status NOT IN ('RESOLVED', 'CLOSED')")
        Page<InterfaceException> findRetryableExceptions(Pageable pageable);

        /**
         * Count exceptions by status list (for active exceptions count).
         * 
         * @param statusList list of status values to count
         * @return count of exceptions with any of the specified statuses
         */
        @Query("SELECT COUNT(ie) FROM InterfaceException ie WHERE ie.status IN :statusList")
        long countByStatusIn(@Param("statusList") List<String> statusList);

        /**
         * Find resolved exceptions after a specific date for resolution time
         * calculation.
         * 
         * @param resolvedAfter the date after which to find resolved exceptions
         * @return list of resolved exceptions
         */
        @Query("SELECT ie FROM InterfaceException ie WHERE ie.resolvedAt IS NOT NULL AND ie.resolvedAt >= :resolvedAfter")
        List<InterfaceException> findByResolvedAtAfter(@Param("resolvedAfter") java.time.Instant resolvedAfter);
}