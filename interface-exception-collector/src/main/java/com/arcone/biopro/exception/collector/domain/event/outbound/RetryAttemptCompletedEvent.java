package com.arcone.biopro.exception.collector.domain.event.outbound;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
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
 * Domain event fired when a retry attempt is completed.
 * Used to trigger cache invalidation for retry-related validations.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RetryAttemptCompletedEvent extends BaseEvent {

    @NotBlank(message = "Transaction ID is required")
    @JsonProperty("transactionId")
    private String transactionId;

    @NotNull(message = "Attempt number is required")
    @JsonProperty("attemptNumber")
    private Integer attemptNumber;

    @NotNull(message = "Final status is required")
    @JsonProperty("finalStatus")
    private RetryStatus finalStatus;

    @NotNull(message = "Success flag is required")
    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("completedAt")
    private OffsetDateTime completedAt;

    @JsonProperty("durationMs")
    private Long durationMs;

    /**
     * Creates a new retry attempt completed event.
     */
    public static RetryAttemptCompletedEvent create(
            String transactionId,
            Integer attemptNumber,
            RetryStatus finalStatus,
            Boolean success,
            String errorMessage,
            Long durationMs) {
        
        return RetryAttemptCompletedEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType("RetryAttemptCompleted")
            .eventVersion("1.0")
            .occurredOn(OffsetDateTime.now())
            .source("interface-exception-collector")
            .transactionId(transactionId)
            .attemptNumber(attemptNumber)
            .finalStatus(finalStatus)
            .success(success)
            .errorMessage(errorMessage)
            .completedAt(OffsetDateTime.now())
            .durationMs(durationMs)
            .build();
    }
}