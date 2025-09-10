package com.arcone.biopro.exception.collector.domain.entity;

import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * JPA Entity representing a status change audit record for interface
 * exceptions.
 * Tracks all status transitions for compliance and debugging purposes.
 */
@Entity
@Table(name = "exception_status_changes", indexes = {
        @Index(name = "idx_status_changes_exception_id", columnList = "exception_id"),
        @Index(name = "idx_status_changes_changed_at", columnList = "changed_at"),
        @Index(name = "idx_status_changes_changed_by", columnList = "changed_by"),
        @Index(name = "idx_status_changes_from_to_status", columnList = "from_status, to_status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "interfaceException")
public class StatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = true, length = 50)
    private ExceptionStatus fromStatus;

    @NotNull(message = "To status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    private ExceptionStatus toStatus;

    @NotBlank(message = "Changed by is required")
    @Size(max = 255, message = "Changed by must not exceed 255 characters")
    @Column(name = "changed_by", nullable = false, length = 255)
    private String changedBy;

    @NotNull(message = "Changed at timestamp is required")
    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @Column(name = "reason", length = 500)
    private String reason;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exception_id", nullable = false)
    @JsonBackReference("exception-statusChanges")
    private InterfaceException interfaceException;

    /**
     * Convenience method to check if this is a status progression (not regression).
     */
    public boolean isProgression() {
        if (fromStatus == null || toStatus == null) {
            return false;
        }

        // Define status progression order
        int fromOrder = getStatusOrder(fromStatus);
        int toOrder = getStatusOrder(toStatus);

        return toOrder > fromOrder;
    }

    /**
     * Gets the order value for status progression analysis.
     */
    private int getStatusOrder(ExceptionStatus status) {
        switch (status) {
            case NEW:
                return 1;
            case ACKNOWLEDGED:
                return 2;
            case ESCALATED:
                return 3;
            case RESOLVED:
                return 4;
            case CLOSED:
                return 5;
            case RETRIED_SUCCESS:
                return 6;
            case RETRIED_FAILED:
                return 0; // Failed can happen at any point
            default:
                return 0;
        }
    }

    /**
     * Gets a human-readable description of the status change.
     */
    public String getDescription() {
        return String.format("Status changed from %s to %s by %s",
                fromStatus, toStatus, changedBy);
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use fromStatus instead
     */
    @Deprecated
    public void setOldStatus(String oldStatus) {
        this.fromStatus = ExceptionStatus.valueOf(oldStatus);
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use toStatus instead
     */
    @Deprecated
    public void setNewStatus(String newStatus) {
        this.toStatus = ExceptionStatus.valueOf(newStatus);
    }
}