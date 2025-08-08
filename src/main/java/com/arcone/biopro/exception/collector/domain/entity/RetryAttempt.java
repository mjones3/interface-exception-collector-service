package com.arcone.biopro.exception.collector.domain.entity;

import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * JPA Entity representing a retry attempt for an interface exception.
 * Tracks the history and outcome of retry operations.
 */
@Entity
@Table(name = "retry_attempts", uniqueConstraints = @UniqueConstraint(columnNames = { "exception_id",
        "attempt_number" }), indexes = {
                @Index(name = "idx_retry_attempts_exception_id", columnList = "exception_id"),
                @Index(name = "idx_retry_attempts_status", columnList = "status"),
                @Index(name = "idx_retry_attempts_initiated_at", columnList = "initiated_at")
        })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Interface exception is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exception_id", nullable = false, foreignKey = @ForeignKey(name = "fk_retry_attempts_exception"))
    private InterfaceException interfaceException;

    @NotNull(message = "Attempt number is required")
    @Positive(message = "Attempt number must be positive")
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private RetryStatus status = RetryStatus.PENDING;

    @NotBlank(message = "Initiated by is required")
    @Size(max = 255, message = "Initiated by must not exceed 255 characters")
    @Column(name = "initiated_by", nullable = false, length = 255)
    private String initiatedBy;

    @NotNull(message = "Initiated at timestamp is required")
    @Builder.Default
    @Column(name = "initiated_at", nullable = false)
    private OffsetDateTime initiatedAt = OffsetDateTime.now();

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "result_success")
    private Boolean resultSuccess;

    @Column(name = "result_message", columnDefinition = "TEXT")
    private String resultMessage;

    @Column(name = "result_response_code")
    private Integer resultResponseCode;

    @Column(name = "result_error_details", columnDefinition = "JSONB")
    private String resultErrorDetails;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Convenience method to mark the retry attempt as completed with success.
     */
    public void markAsSuccess(String message, Integer responseCode) {
        this.status = RetryStatus.SUCCESS;
        this.completedAt = OffsetDateTime.now();
        this.resultSuccess = true;
        this.resultMessage = message;
        this.resultResponseCode = responseCode;
    }

    /**
     * Convenience method to mark the retry attempt as failed.
     */
    public void markAsFailed(String message, Integer responseCode, String errorDetails) {
        this.status = RetryStatus.FAILED;
        this.completedAt = OffsetDateTime.now();
        this.resultSuccess = false;
        this.resultMessage = message;
        this.resultResponseCode = responseCode;
        this.resultErrorDetails = errorDetails;
    }
}