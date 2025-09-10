package com.arcone.biopro.exception.collector.infrastructure.repository;

import com.arcone.biopro.exception.collector.domain.entity.MutationAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for mutation audit log operations.
 * Provides methods for querying audit history and statistics.
 * 
 * Requirements: 5.3, 5.5, 6.4
 */
@Repository
public interface MutationAuditLogRepository extends JpaRepository<MutationAuditLog, Long> {

    /**
     * Find all audit logs for a specific transaction ID.
     */
    List<MutationAuditLog> findByTransactionIdOrderByPerformedAtDesc(String transactionId);

    /**
     * Find all audit logs performed by a specific user.
     */
    Page<MutationAuditLog> findByPerformedByOrderByPerformedAtDesc(String performedBy, Pageable pageable);

    /**
     * Find all audit logs for a specific operation type.
     */
    Page<MutationAuditLog> findByOperationTypeOrderByPerformedAtDesc(
            MutationAuditLog.OperationType operationType, Pageable pageable);

    /**
     * Find audit logs within a date range.
     */
    @Query("SELECT mal FROM MutationAuditLog mal WHERE mal.performedAt BETWEEN :startDate AND :endDate ORDER BY mal.performedAt DESC")
    Page<MutationAuditLog> findByPerformedAtBetween(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    /**
     * Find failed operations for monitoring purposes.
     */
    @Query("SELECT mal FROM MutationAuditLog mal WHERE mal.resultStatus = 'FAILURE' AND mal.performedAt >= :since ORDER BY mal.performedAt DESC")
    List<MutationAuditLog> findRecentFailures(@Param("since") Instant since);

    /**
     * Count operations by type and status for metrics.
     */
    @Query("SELECT mal.operationType, mal.resultStatus, COUNT(mal) FROM MutationAuditLog mal " +
           "WHERE mal.performedAt >= :since GROUP BY mal.operationType, mal.resultStatus")
    List<Object[]> countOperationsByTypeAndStatus(@Param("since") Instant since);

    /**
     * Get average execution time by operation type.
     */
    @Query("SELECT mal.operationType, AVG(mal.executionTimeMs) FROM MutationAuditLog mal " +
           "WHERE mal.executionTimeMs IS NOT NULL AND mal.performedAt >= :since " +
           "GROUP BY mal.operationType")
    List<Object[]> getAverageExecutionTimeByType(@Param("since") Instant since);

    /**
     * Find audit logs by correlation ID for tracing.
     */
    List<MutationAuditLog> findByCorrelationIdOrderByPerformedAtAsc(String correlationId);

    /**
     * Find audit logs by operation ID.
     */
    List<MutationAuditLog> findByOperationIdOrderByPerformedAtAsc(String operationId);

    /**
     * Delete old audit logs for cleanup (older than specified date).
     */
    void deleteByPerformedAtBefore(Instant cutoffDate);
}