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
 * Kafka event published when a retry operation is completed.
 * Notifies downstream systems about retry outcomes for tracking and workflow
 * triggers.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExceptionRetryCompletedEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private ExceptionRetryCompletedPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionRetryCompletedPayload {

        @NotNull(message = "Exception ID is required")
        @JsonProperty("exceptionId")
        private Long exceptionId;

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @NotNull(message = "Attempt number is required")
        @JsonProperty("attemptNumber")
        private Integer attemptNumber;

        @NotNull(message = "Retry status is required")
        @JsonProperty("retryStatus")
        private String retryStatus;

        @JsonProperty("retryResult")
        private RetryResult retryResult;

        @NotNull(message = "Initiated by is required")
        @JsonProperty("initiatedBy")
        private String initiatedBy;

        @NotNull(message = "Completed at timestamp is required")
        @JsonProperty("completedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private OffsetDateTime completedAt;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryResult {

        @NotNull(message = "Success flag is required")
        @JsonProperty("success")
        private Boolean success;

        @JsonProperty("message")
        private String message;

        @JsonProperty("responseCode")
        private Integer responseCode;

        @JsonProperty("errorDetails")
        private String errorDetails;
    }
}