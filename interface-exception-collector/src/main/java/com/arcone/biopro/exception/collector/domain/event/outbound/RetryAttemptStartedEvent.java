package com.arcone.biopro.exception.collector.domain.event.outbound;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput.RetryPriority;
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
 * Domain event fired when a retry attempt is started.
 * Used to trigger cache invalidation for retry-related validations.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RetryAttemptStartedEvent extends BaseEvent {

    @NotBlank(message = "Transaction ID is required")
    @JsonProperty("transactionId")
    private String transactionId;

    @NotNull(message = "Attempt number is required")
    @JsonProperty("attemptNumber")
    private Integer attemptNumber;

    @NotBlank(message = "Started by user is required")
    @JsonProperty("startedBy")
    private String startedBy;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("priority")
    private RetryPriority priority;

    @JsonProperty("notes")
    private String notes;

    /**
     * Creates a new retry attempt started event.
     */
    public static RetryAttemptStartedEvent create(
            String transactionId,
            Integer attemptNumber,
            String startedBy,
            String reason,
            RetryPriority priority,
            String notes) {
        
        return RetryAttemptStartedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType("RetryAttemptStarted")
            .eventVersion("1.0")
            .occurredOn(OffsetDateTime.now())
            .source("interface-exception-collector")
            .transactionId(transactionId)
            .attemptNumber(attemptNumber)
            .startedBy(startedBy)
            .reason(reason)
            .priority(priority)
            .notes(notes)
            .build();
    }
}