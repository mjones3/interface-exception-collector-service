package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
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
 * Repository interface for RetryAttempt entity providing data access operations
 * for retry history management and tracking.
 */
@Repository
public interface RetryAttemptRepository extends JpaRepository<RetryAttempt, Long> {

        /**
         * Find all retry attempts for a specific exception ordered by attempt number.
         * 
         * @param interfaceException the exception to get retry attempts for
         * @return List of retry attempts ordered by attempt number
         */
        List<RetryAttempt> findByInterfaceExceptionOrderByAttemptNumberAsc(InterfaceException interfaceException);

        /**
         * Find all retry attempts for a specific exception with pagination.
         * 
         * @param interfaceException the exception to get retry attempts for
         * @param pageable           pagination and sorting parameters
         * @return Page of retry attempts
         */
        Page<RetryAttempt> findByInterfaceException(InterfaceException interfaceException, Pageable pageable);

        /**
         * Find retry attempts by status.
         * 
         * @param status   the retry status to filter by
         * @param pageable pagination and sorting parameters
         * @return Page of retry attempts with the specified status
         */
        Page<RetryAttempt> findByStatus(RetryStatus status, Pageable pageable);

        /**
         * Find retry attempts initiated by a specific user.
         * 
         * @param initiatedBy the user who initiated the retry
         * @param pageable    pagination and sorting parameters
         * @return Page of retry attempts initiated by the user
         */
        Page<RetryAttempt> findByInitiatedBy(String initiatedBy, Pageable pageable);

        /**
         * Find retry attempts within a date range.
         * 
         * @param fromDate start date (inclusive)
         * @param toDate   end date (inclusive)
         * @param pageable pagination and sorting parameters
         * @return Page of retry attempts within the date range
         */
        Page<RetryAttempt> findByInitiatedAtBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable);

        /**
         * Find the latest retry attempt for a specific exception.
         * 
         * @param interfaceException the exception to get the latest retry for
         * @return Optional containing the latest retry attempt if any exists
         */
        Optional<RetryAttempt> findTopByInterfaceExceptionOrderByAttemptNumberDesc(
                        InterfaceException interfaceException);

        /**
         * Find a specific retry attempt by exception and attempt number.
         * 
         * @param interfaceException the exception
         * @param attemptNumber      the attempt number
         * @return Optional containing the retry attempt if found
         */
        Optional<RetryAttempt> findByInterfaceExceptionAndAttemptNumber(InterfaceException interfaceException,
                        Integer attemptNumber);

        /**
         * Count total retry attempts for a specific exception.
         * 
         * @param interfaceException the exception to count retries for
         * @return total number of retry attempts
         */
        long countByInterfaceException(InterfaceException interfaceException);

        /**
         * Count successful retry attempts for a specific exception.
         * 
         * @param interfaceException the exception to count successful retries for
         * @return number of successful retry attempts
         */
        long countByInterfaceExceptionAndStatus(InterfaceException interfaceException, RetryStatus status);

        /**
         * Count retry attempts by status.
         * 
         * @param status the retry status
         * @return count of retry attempts with the status
         */
        long countByStatus(RetryStatus status);

        /**
         * Find pending retry attempts that may need attention.
         * 
         * @param pageable pagination parameters
         * @return Page of pending retry attempts
         */
        Page<RetryAttempt> findByStatusOrderByInitiatedAtAsc(RetryStatus status, Pageable pageable);

        /**
         * Find retry attempts that have been pending for too long.
         * 
         * @param status     the pending status
         * @param cutoffTime the time before which attempts are considered stale
         * @param pageable   pagination parameters
         * @return Page of stale retry attempts
         */
        @Query("SELECT ra FROM RetryAttempt ra WHERE " +
                        "ra.status = :status AND ra.initiatedAt < :cutoffTime " +
                        "ORDER BY ra.initiatedAt ASC")
        Page<RetryAttempt> findStaleRetryAttempts(
                        @Param("status") RetryStatus status,
                        @Param("cutoffTime") OffsetDateTime cutoffTime,
                        Pageable pageable);

        /**
         * Get retry statistics for a specific exception.
         * 
         * @param interfaceException the exception to get statistics for
         * @return Array containing [totalAttempts, successfulAttempts, failedAttempts,
         *         pendingAttempts]
         */
        @Query("SELECT " +
                        "COUNT(ra), " +
                        "SUM(CASE WHEN ra.status = 'SUCCESS' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN ra.status = 'FAILED' THEN 1 ELSE 0 END), " +
                        "SUM(CASE WHEN ra.status = 'PENDING' THEN 1 ELSE 0 END) " +
                        "FROM RetryAttempt ra WHERE ra.interfaceException = :interfaceException")
        Object[] getRetryStatistics(@Param("interfaceException") InterfaceException interfaceException);

        /**
         * Find retry attempts with specific result success status.
         * 
         * @param resultSuccess whether the retry was successful
         * @param pageable      pagination parameters
         * @return Page of retry attempts with the specified success status
         */
        Page<RetryAttempt> findByResultSuccess(Boolean resultSuccess, Pageable pageable);

        /**
         * Find retry attempts that completed within a date range.
         * 
         * @param fromDate start date (inclusive)
         * @param toDate   end date (inclusive)
         * @param pageable pagination parameters
         * @return Page of retry attempts completed within the date range
         */
        Page<RetryAttempt> findByCompletedAtBetween(OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable);

        /**
         * Get daily retry attempt counts for trend analysis.
         * 
         * @param fromDate start date for trend analysis
         * @param toDate   end date for trend analysis
         * @return List of daily counts with date and count
         */
        @Query("SELECT DATE(ra.initiatedAt) as date, COUNT(ra) as count " +
                        "FROM RetryAttempt ra " +
                        "WHERE ra.initiatedAt BETWEEN :fromDate AND :toDate " +
                        "GROUP BY DATE(ra.initiatedAt) " +
                        "ORDER BY DATE(ra.initiatedAt)")
        List<Object[]> getDailyRetryCounts(@Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate);

        /**
         * Find retry attempts with error details containing specific text.
         * Uses Spring Data JPA method naming convention to avoid query issues.
         * 
         * @param errorText text to search for in error details
         * @param pageable  pagination parameters
         * @return Page of retry attempts with matching error details
         */
        Page<RetryAttempt> findByResultErrorDetailsContaining(String errorText, Pageable pageable);

        /**
         * Find the next attempt number for a specific exception.
         * 
         * @param interfaceException the exception to get the next attempt number for
         * @return the next attempt number (max + 1, or 1 if no attempts exist)
         */
        @Query("SELECT COALESCE(MAX(ra.attemptNumber), 0) + 1 FROM RetryAttempt ra WHERE ra.interfaceException = :interfaceException")
        Integer getNextAttemptNumber(@Param("interfaceException") InterfaceException interfaceException);

        /**
         * Delete all retry attempts for a specific exception.
         * Used for cleanup operations.
         * 
         * @param interfaceException the exception to delete retry attempts for
         */
        void deleteByInterfaceException(InterfaceException interfaceException);

        /**
         * Find retry attempts by response code.
         * 
         * @param responseCode the HTTP response code
         * @param pageable     pagination parameters
         * @return Page of retry attempts with the specified response code
         */
        Page<RetryAttempt> findByResultResponseCode(Integer responseCode, Pageable pageable);

        /**
         * Check if an exception has any successful retry attempts.
         * 
         * @param interfaceException the exception to check
         * @return true if there are successful retry attempts, false otherwise
         */
        boolean existsByInterfaceExceptionAndStatus(InterfaceException interfaceException, RetryStatus status);

        /**
         * Find retry attempts for a collection of exceptions.
         * Used for batch loading in DataLoader pattern.
         * 
         * @param exceptions collection of exceptions to find retry attempts for
         * @return List of retry attempts for the given exceptions
         */
        List<RetryAttempt> findByInterfaceExceptionIn(List<InterfaceException> exceptions);

        /**
         * Count retry attempts within a date range.
         * 
         * @param fromDate start date (inclusive)
         * @param toDate   end date (inclusive)
         * @return count of retry attempts within the date range
         */
        long countByInitiatedAtBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

        /**
         * Count successful retry attempts within a date range.
         * 
         * @param fromDate      start date (inclusive)
         * @param toDate        end date (inclusive)
         * @param resultSuccess whether the retry was successful
         * @return count of retry attempts with the specified success status within the
         *         date range
         */
        long countByInitiatedAtBetweenAndResultSuccess(OffsetDateTime fromDate, OffsetDateTime toDate,
                        Boolean resultSuccess);
}