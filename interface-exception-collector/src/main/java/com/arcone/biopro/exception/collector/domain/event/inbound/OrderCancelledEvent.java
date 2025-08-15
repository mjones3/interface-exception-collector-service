package com.arcone.biopro.exception.collector.domain.event.inbound;

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
 * Kafka event representing an order cancellation from the Order Service.
 * Contains all necessary information to create an interface exception.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private OrderCancelledPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderCancelledPayload {

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("externalId")
        private String externalId;

        @NotNull(message = "Cancel reason is required")
        @JsonProperty("cancelReason")
        private String cancelReason;

        @JsonProperty("cancelledBy")
        private String cancelledBy;

        @JsonProperty("customerId")
        private String customerId;
    }
}