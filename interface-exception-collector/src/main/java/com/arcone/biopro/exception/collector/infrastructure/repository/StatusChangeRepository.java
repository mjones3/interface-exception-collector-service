package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * Repository interface for StatusChange entity operations.
 * Provides methods for querying status change audit trail data.
 */
@Repository
public interface StatusChangeRepository extends JpaRepository<StatusChange, Long> {

    /**
     * Finds all status changes for exceptions with the given transaction IDs,
     * ordered by change timestamp in descending order (most recent first).
     *
     * @param transactionIds set of transaction IDs to find status changes for
     * @return list of status changes ordered by changed_at DESC
     */
    @Query("SELECT sc FROM StatusChange sc " +
            "JOIN sc.interfaceException ie " +
            "WHERE ie.transactionId IN :transactionIds " +
            "ORDER BY sc.changedAt DESC")
    List<StatusChange> findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(
            @Param("transactionIds") Set<String> transactionIds);

    /**
     * Finds all status changes for a specific exception by transaction ID,
     * ordered by change timestamp in descending order.
     *
     * @param transactionId the transaction ID to find status changes for
     * @return list of status changes ordered by changed_at DESC
     */
    @Query("SELECT sc FROM StatusChange sc " +
            "JOIN sc.interfaceException ie " +
            "WHERE ie.transactionId = :transactionId " +
            "ORDER BY sc.changedAt DESC")
    List<StatusChange> findByInterfaceExceptionTransactionIdOrderByChangedAtDesc(
            @Param("transactionId") String transactionId);

    /**
     * Finds status changes by status transition (from -> to).
     *
     * @param fromStatus the original status
     * @param toStatus   the new status
     * @return list of status changes matching the transition
     */
    List<StatusChange> findByFromStatusAndToStatusOrderByChangedAtDesc(
            ExceptionStatus fromStatus, ExceptionStatus toStatus);

    /**
     * Finds status changes made by a specific user.
     *
     * @param changedBy the user who made the changes
     * @return list of status changes made by the user
     */
    List<StatusChange> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * Finds status changes within a date range.
     *
     * @param startDate the start of the date range
     * @param endDate   the end of the date range
     * @return list of status changes within the date range
     */
    List<StatusChange> findByChangedAtBetweenOrderByChangedAtDesc(
            OffsetDateTime startDate, OffsetDateTime endDate);

    /**
     * Counts status changes for a specific exception.
     *
     * @param transactionId the transaction ID to count status changes for
     * @return number of status changes for the exception
     */
    @Query("SELECT COUNT(sc) FROM StatusChange sc " +
            "JOIN sc.interfaceException ie " +
            "WHERE ie.transactionId = :transactionId")
    long countByInterfaceExceptionTransactionId(@Param("transactionId") String transactionId);

    /**
     * Finds the most recent status change for a specific exception.
     *
     * @param transactionId the transaction ID to find the latest status change for
     * @return the most recent status change, or null if none exists
     */
    @Query("SELECT sc FROM StatusChange sc " +
            "JOIN sc.interfaceException ie " +
            "WHERE ie.transactionId = :transactionId " +
            "ORDER BY sc.changedAt DESC " +
            "LIMIT 1")
    StatusChange findLatestByInterfaceExceptionTransactionId(@Param("transactionId") String transactionId);
}