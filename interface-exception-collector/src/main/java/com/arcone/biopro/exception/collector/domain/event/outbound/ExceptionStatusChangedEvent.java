package com.arcone.biopro.exception.collector.domain.event.outbound;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

/**
 * Domain event fired when an exception's status changes.
 * Used to trigger cache invalidation and other side effects.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExceptionStatusChangedEvent extends BaseEvent {

    @NotBlank(message = "Transaction ID is required")
    @JsonProperty("transactionId")
    private String transactionId;

    @NotNull(message = "Old status is required")
    @JsonProperty("oldStatus")
    private ExceptionStatus oldStatus;

    @NotNull(message = "New status is required")
    @JsonProperty("newStatus")
    private ExceptionStatus newStatus;

    @NotBlank(message = "Changed by user is required")
    @JsonProperty("changedBy")
    private String changedBy;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("notes")
    private String notes;

    /**
     * Creates a new exception status changed event.
     */
    public static ExceptionStatusChangedEvent create(
            String transactionId,
            ExceptionStatus oldStatus,
            ExceptionStatus newStatus,
            String changedBy,
            String reason,
            String notes) {
        
        return ExceptionStatusChangedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType("ExceptionStatusChanged")
            .eventVersion("1.0")
            .occurredOn(OffsetDateTime.now())
            .source("interface-exception-collector")
            .transactionId(transactionId)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .changedBy(changedBy)
            .reason(reason)
            .notes(notes)
            .build();
    }
}