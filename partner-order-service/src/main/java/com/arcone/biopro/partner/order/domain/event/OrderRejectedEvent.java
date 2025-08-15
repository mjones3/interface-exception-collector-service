package com.arcone.biopro.partner.order.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Event published when a partner order is rejected for testing purposes.
 * This event is consumed by the Interface Exception Collector Service
 * to demonstrate the exception handling and retry workflow.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderRejectedEvent extends BaseEvent {

    /**
     * The event payload containing rejection details.
     */
    @JsonProperty("payload")
    private OrderRejectedPayload payload;

    /**
     * Payload data for OrderRejected events.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRejectedPayload {

        /**
         * Transaction ID that uniquely identifies the business transaction.
         */
        @JsonProperty("transactionId")
        private String transactionId;

        /**
         * Partner's unique order identifier.
         */
        @JsonProperty("externalId")
        private String externalId;

        /**
         * Reason why the order was rejected.
         */
        @JsonProperty("rejectedReason")
        private String rejectedReason;

        /**
         * Operation that was being performed when rejection occurred.
         */
        @JsonProperty("operation")
        private String operation;

        /**
         * Customer identifier associated with the order.
         */
        @JsonProperty("customerId")
        private String customerId;

        /**
         * Location code where the order was submitted.
         */
        @JsonProperty("locationCode")
        private String locationCode;

        /**
         * Order items that were part of the rejected order.
         */
        @JsonProperty("orderItems")
        private List<OrderItem> orderItems;

        /**
         * Complete original payload for retry operations.
         */
        @JsonProperty("originalPayload")
        private Object originalPayload;
    }

    /**
     * Order item details for rejected orders.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {

        /**
         * Blood type for the order item.
         */
        @JsonProperty("bloodType")
        private String bloodType;

        /**
         * Product family for the order item.
         */
        @JsonProperty("productFamily")
        private String productFamily;

        /**
         * Quantity requested.
         */
        @JsonProperty("quantity")
        private Integer quantity;

        /**
         * Optional comments for the order item.
         */
        @JsonProperty("comments")
        private String comments;
    }
}