package com.arcone.biopro.exception.collector.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing a mutation audit log entry.
 * Stores comprehensive audit information for all GraphQL mutation operations.
 * 
 * Requirements: 5.3, 5.5, 6.4
 */
@Entity
@Table(name = "mutation_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MutationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Column(name = "transaction_id", nullable = false, length = 255)
    private String transactionId;

    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @Column(name = "input_data", columnDefinition = "text")
    private String inputData;

    @Column(name = "result_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ResultStatus resultStatus;

    @Column(name = "error_details", columnDefinition = "text")
    private String errorDetails;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "operation_id", length = 100)
    private String operationId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent")
    private String userAgent;

    /**
     * Enumeration of supported mutation operation types.
     */
    public enum OperationType {
        RETRY,
        ACKNOWLEDGE,
        RESOLVE,
        CANCEL_RETRY,
        BULK_RETRY,
        BULK_ACKNOWLEDGE
    }

    /**
     * Enumeration of operation result statuses.
     */
    public enum ResultStatus {
        SUCCESS,
        FAILURE,
        PARTIAL_SUCCESS
    }

    @PrePersist
    protected void onCreate() {
        if (performedAt == null) {
            performedAt = Instant.now();
        }
    }
}