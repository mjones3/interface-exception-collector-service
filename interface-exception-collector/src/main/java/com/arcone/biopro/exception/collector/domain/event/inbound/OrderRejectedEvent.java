package com.arcone.biopro.exception.collector.domain.event.inbound;

import com.arcone.biopro.exception.collector.domain.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Kafka event representing an order rejection from the Order Service.
 * Conforms to the OrderRejected-Inbound.json schema specification.
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

        @NotBlank(message = "Transaction ID is required")
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "Transaction ID must be a valid UUID")
        @JsonProperty("transactionId")
        private String transactionId;

        @NotBlank(message = "External ID is required")
        @JsonProperty("externalId")
        private String externalId;

        @JsonProperty("operation")
        private OrderOperation operation;

        @NotBlank(message = "Rejected reason is required")
        @JsonProperty("rejectedReason")
        private String rejectedReason;

        @JsonProperty("customerId")
        private String customerId;

        @JsonProperty("locationCode")
        private String locationCode;

        @JsonProperty("orderItems")
        private List<OrderItem> orderItems;

        @JsonProperty("originalPayload")
        private Object originalPayload;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {

        @NotBlank(message = "Blood type is required")
        @JsonProperty("bloodType")
        private String bloodType;

        @NotBlank(message = "Product family is required")
        @JsonProperty("productFamily")
        private String productFamily;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("comments")
        private String comments;
    }

    /**
     * Enumeration of valid order operations as defined in the schema.
     */
    public enum OrderOperation {
        CREATE_ORDER,
        MODIFY_ORDER,
        CANCEL_ORDER;

        /**
         * Custom deserialization to handle string values from Partner Order Service
         */
        @com.fasterxml.jackson.annotation.JsonCreator
        public static OrderOperation fromString(String value) {
            if (value == null)
                return null;
            try {
                return OrderOperation.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Default to CREATE_ORDER if unknown operation
                return CREATE_ORDER;
            }
        }
    }
}