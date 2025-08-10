package com.arcone.biopro.exception.collector.domain.entity;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.enums.ResolutionMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing an interface exception captured from BioPro services.
 * Contains all metadata required for exception tracking, retry management, and
 * resolution.
 */
@Entity
@Table(name = "interface_exceptions", indexes = {
        @Index(name = "idx_interface_exceptions_transaction_id", columnList = "transactionId", unique = true),
        @Index(name = "idx_interface_exceptions_interface_type", columnList = "interfaceType"),
        @Index(name = "idx_interface_exceptions_status", columnList = "status"),
        @Index(name = "idx_interface_exceptions_severity", columnList = "severity"),
        @Index(name = "idx_interface_exceptions_customer_id", columnList = "customerId"),
        @Index(name = "idx_interface_exceptions_timestamp", columnList = "timestamp"),
        @Index(name = "idx_interface_exceptions_processed_at", columnList = "processedAt"),
        @Index(name = "idx_interface_exceptions_type_status", columnList = "interfaceType, status"),
        @Index(name = "idx_interface_exceptions_severity_timestamp", columnList = "severity, timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "retryAttempts", "orderItems" })
public class InterfaceException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 255, message = "Transaction ID must not exceed 255 characters")
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @NotNull(message = "Interface type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "interface_type", nullable = false, length = 50)
    private InterfaceType interfaceType;

    @NotBlank(message = "Exception reason is required")
    @Column(name = "exception_reason", nullable = false, columnDefinition = "TEXT")
    private String exceptionReason;

    @NotBlank(message = "Operation is required")
    @Size(max = 100, message = "Operation must not exceed 100 characters")
    @Column(name = "operation", nullable = false, length = 100)
    private String operation;

    @Size(max = 255, message = "External ID must not exceed 255 characters")
    @Column(name = "external_id", length = 255)
    private String externalId;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private ExceptionStatus status = ExceptionStatus.NEW;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "severity", nullable = false, length = 50)
    private ExceptionSeverity severity = ExceptionSeverity.MEDIUM;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ExceptionCategory category;

    @Builder.Default
    @Column(name = "retryable", nullable = false)
    private Boolean retryable = true;

    @Size(max = 255, message = "Customer ID must not exceed 255 characters")
    @Column(name = "customer_id", length = 255)
    private String customerId;

    @Size(max = 100, message = "Location code must not exceed 100 characters")
    @Column(name = "location_code", length = 100)
    private String locationCode;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @NotNull(message = "Processed at timestamp is required")
    @Builder.Default
    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt = OffsetDateTime.now();

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;

    @Size(max = 255, message = "Acknowledged by must not exceed 255 characters")
    @Column(name = "acknowledged_by", length = 255)
    private String acknowledgedBy;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Size(max = 255, message = "Resolved by must not exceed 255 characters")
    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_method", length = 50)
    private ResolutionMethod resolutionMethod;

    @Size(max = 1000, message = "Resolution notes must not exceed 1000 characters")
    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Size(max = 1000, message = "Acknowledgment notes must not exceed 1000 characters")
    @Column(name = "acknowledgment_notes", length = 1000)
    private String acknowledgmentNotes;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private OffsetDateTime lastRetryAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "interfaceException", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RetryAttempt> retryAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "interfaceException", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Convenience method to add a retry attempt to this exception.
     * Maintains bidirectional relationship consistency.
     */
    public void addRetryAttempt(RetryAttempt retryAttempt) {
        retryAttempts.add(retryAttempt);
        retryAttempt.setInterfaceException(this);
    }

    /**
     * Convenience method to remove a retry attempt from this exception.
     * Maintains bidirectional relationship consistency.
     */
    public void removeRetryAttempt(RetryAttempt retryAttempt) {
        retryAttempts.remove(retryAttempt);
        retryAttempt.setInterfaceException(null);
    }

    /**
     * Convenience method to add an order item to this exception.
     * Maintains bidirectional relationship consistency.
     */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setInterfaceException(this);
    }

    /**
     * Convenience method to remove an order item from this exception.
     * Maintains bidirectional relationship consistency.
     */
    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setInterfaceException(null);
    }
}