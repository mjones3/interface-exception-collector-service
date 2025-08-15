package com.arcone.biopro.exception.collector.domain.event.outbound;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

/**
 * Kafka event published when an exception reaches resolved status.
 * Notifies downstream systems about exception resolution for tracking and
 * workflow completion.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExceptionResolvedEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private ExceptionResolvedPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionResolvedPayload {

        @NotNull(message = "Exception ID is required")
        @JsonProperty("exceptionId")
        private Long exceptionId;

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @NotNull(message = "Resolution method is required")
        @JsonProperty("resolutionMethod")
        private String resolutionMethod;

        @JsonProperty("resolvedBy")
        private String resolvedBy;

        @NotNull(message = "Resolved at timestamp is required")
        @JsonProperty("resolvedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private OffsetDateTime resolvedAt;

        @NotNull(message = "Total retry attempts is required")
        @JsonProperty("totalRetryAttempts")
        private Integer totalRetryAttempts;

        @JsonProperty("resolutionNotes")
        private String resolutionNotes;
    }
}