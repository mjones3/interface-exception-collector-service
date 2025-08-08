package com.arcone.biopro.exception.collector.domain.event.outbound;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Kafka event published when an exception is successfully captured and stored.
 * Notifies downstream systems about new exceptions for workflow triggers.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExceptionCapturedEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private ExceptionCapturedPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionCapturedPayload {

        @NotNull(message = "Exception ID is required")
        @JsonProperty("exceptionId")
        private Long exceptionId;

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @NotNull(message = "Interface type is required")
        @JsonProperty("interfaceType")
        private String interfaceType;

        @NotNull(message = "Severity is required")
        @JsonProperty("severity")
        private String severity;

        @NotNull(message = "Category is required")
        @JsonProperty("category")
        private String category;

        @NotNull(message = "Exception reason is required")
        @JsonProperty("exceptionReason")
        private String exceptionReason;

        @JsonProperty("customerId")
        private String customerId;

        @NotNull(message = "Retryable flag is required")
        @JsonProperty("retryable")
        private Boolean retryable;
    }
}