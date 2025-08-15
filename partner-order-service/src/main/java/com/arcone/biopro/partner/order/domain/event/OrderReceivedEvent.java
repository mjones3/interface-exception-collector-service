package com.arcone.biopro.partner.order.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a partner order is successfully received and validated.
 * This event is consumed by downstream systems for order processing.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderReceivedEvent extends BaseEvent {

    /**
     * The event payload containing order details.
     */
    @JsonProperty("payload")
    private OrderReceivedPayload payload;

    /**
     * Payload data for OrderReceived events.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderReceivedPayload {

        /**
         * Partner's unique order identifier.
         */
        @JsonProperty("externalId")
        private String externalId;

        /**
         * Location code where the order was submitted.
         */
        @JsonProperty("locationCode")
        private String locationCode;

        /**
         * Product category for the order.
         */
        @JsonProperty("productCategory")
        private String productCategory;

        /**
         * Complete order data as received from the partner.
         */
        @JsonProperty("orderData")
        private Object orderData;
    }
}