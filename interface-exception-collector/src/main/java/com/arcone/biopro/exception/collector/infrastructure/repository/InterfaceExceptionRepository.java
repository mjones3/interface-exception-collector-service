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
import java.util.Set;

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
         * Find an exception by its transaction ID with eager loading of retry attempts.
         * This prevents LazyInitializationException during GraphQL serialization.
         * Note: We fetch only retry attempts to avoid Hibernate's
         * MultipleBagFetchException.
         * 
         * @param transactionId the unique transaction identifier
         * @return Optional containing the exception with eager-loaded retry attempts if
         *         found
         */
        @Query("SELECT ie FROM InterfaceException ie " +
                        "LEFT JOIN FETCH ie.retryAttempts " +
                        "WHERE ie.transactionId = :transactionId")
        Optional<InterfaceException> findByTransactionIdWithEagerLoading(@Param("transactionId") String transactionId);

        /**
         * Check if an exception exists with the given transaction ID.
         * 
         * @param transactionId the unique transaction identifier
         * @return true if exception exists, false otherwise
         */
        boolean existsByTransactionId(String transactionId);

        /**
         * Find exceptions by a collection of transaction IDs.
         * Used for batch loading in DataLoader pattern.
         * 
         * @param transactionIds collection of transaction IDs to find
         * @return List of exceptions matching the transaction IDs
         */
        List<InterfaceException> findByTransactionIdIn(Set<String> transactionIds);

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

        // Note: The findWithFilters method has been removed to prevent PostgreSQL
        // parameter binding issues.
        // Use the type-safe findWithFiltersTypeSafe method from
        // InterfaceExceptionRepositoryCustom instead.

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

        /**
         * Get aggregated summary statistics from materialized view for optimal
         * performance.
         * 
         * @param fromDate start date for the summary
         * @param toDate   end date for the summary
         * @return List of summary statistics grouped by interface type, status, and
         *         severity
         */
        @Query(value = "SELECT " +
                        "interface_type, " +
                        "status, " +
                        "severity, " +
                        "SUM(exception_count) as total_count, " +
                        "SUM(unique_customers) as unique_customers, " +
                        "SUM(critical_count) as critical_count, " +
                        "AVG(avg_resolution_hours) as avg_resolution_hours " +
                        "FROM exception_summary_mv " +
                        "WHERE day_bucket >= :fromDate AND day_bucket <= :toDate " +
                        "GROUP BY interface_type, status, severity " +
                        "ORDER BY interface_type, status, severity", nativeQuery = true)
        List<Object[]> getSummaryStatisticsFromMaterializedView(
                        @Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate);

        /**
         * Get trend data from materialized view for time-series analysis.
         * 
         * @param fromDate start date for trends
         * @param toDate   end date for trends
         * @param interval time interval ('hour', 'day', 'week', 'month')
         * @return List of trend data points with timestamp and count
         */
        @Query(value = "SELECT " +
                        "CASE " +
                        "  WHEN :interval = 'hour' THEN hour_bucket " +
                        "  WHEN :interval = 'day' THEN day_bucket " +
                        "  WHEN :interval = 'week' THEN week_bucket " +
                        "  ELSE month_bucket " +
                        "END as time_bucket, " +
                        "SUM(exception_count) as total_count " +
                        "FROM exception_summary_mv " +
                        "WHERE day_bucket >= :fromDate AND day_bucket <= :toDate " +
                        "GROUP BY time_bucket " +
                        "ORDER BY time_bucket", nativeQuery = true)
        List<Object[]> getTrendDataFromMaterializedView(
                        @Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate,
                        @Param("interval") String interval);

        /**
         * Count unique customers impacted within a date range.
         * 
         * @param fromDate start date (inclusive)
         * @param toDate   end date (inclusive)
         * @return count of unique customers impacted
         */
        @Query("SELECT COUNT(DISTINCT ie.customerId) FROM InterfaceException ie " +
                        "WHERE ie.timestamp BETWEEN :fromDate AND :toDate " +
                        "AND ie.customerId IS NOT NULL")
        long countUniqueCustomersImpacted(@Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate);

        /**
         * Calculate average resolution time in hours for resolved exceptions.
         * 
         * @param fromDate start date (inclusive)
         * @param toDate   end date (inclusive)
         * @return Average resolution time in hours, or null if no resolved exceptions
         */
        @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - timestamp))/3600) " +
                        "FROM interface_exceptions " +
                        "WHERE timestamp BETWEEN :fromDate AND :toDate " +
                        "AND resolved_at IS NOT NULL", nativeQuery = true)
        Double getAverageResolutionTime(@Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate);

        /**
         * Count exceptions by interface type excluding a specific status.
         * 
         * @param interfaceType the interface type
         * @param status        the status to exclude
         * @return count of exceptions for the interface type excluding the status
         */
        long countByInterfaceTypeAndStatusNot(InterfaceType interfaceType, ExceptionStatus status);

        /**
         * Count exceptions created before a date excluding a specific status.
         * 
         * @param date   the date threshold
         * @param status the status to exclude
         * @return count of exceptions created before the date excluding the status
         */
        long countByCreatedAtBeforeAndStatusNot(OffsetDateTime date, ExceptionStatus status);

        /**
         * Count exceptions created after a specific date.
         * 
         * @param date the date threshold
         * @return count of exceptions created after the date
         */
        long countByCreatedAtAfter(OffsetDateTime date);
}
