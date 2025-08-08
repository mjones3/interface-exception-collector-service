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

import java.util.List;

/**
 * Kafka event representing an order rejection from the Order Service.
 * Contains all necessary information to create an interface exception.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderRejectedEvent extends BaseEvent {

    @NotNull(message = "Payload is required")
    @Valid
    @JsonProperty("payload")
    private OrderRejectedPayload payload;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRejectedPayload {

        @NotNull(message = "Transaction ID is required")
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("externalId")
        private String externalId;

        @NotNull(message = "Operation is required")
        @JsonProperty("operation")
        private String operation;

        @NotNull(message = "Rejected reason is required")
        @JsonProperty("rejectedReason")
        private String rejectedReason;

        @JsonProperty("customerId")
        private String customerId;

        @JsonProperty("locationCode")
        private String locationCode;

        @JsonProperty("orderItems")
        private List<OrderItem> orderItems;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {

        @JsonProperty("itemId")
        private String itemId;

        @JsonProperty("itemType")
        private String itemType;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("unitPrice")
        private Double unitPrice;
    }
}