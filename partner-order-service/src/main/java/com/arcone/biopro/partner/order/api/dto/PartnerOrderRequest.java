package com.arcone.biopro.partner.order.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for partner order submissions.
 * Maps to the JSON schema requirements with JSR-303 validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Partner order submission request")
public class PartnerOrderRequest {

    @NotBlank(message = "External ID is required")
    @Size(min = 1, max = 255, message = "External ID must be between 1 and 255 characters")
    @Schema(description = "Partner's unique order identifier", example = "ORDER-123456", required = true)
    @JsonProperty("externalId")
    private String externalId;

    @NotBlank(message = "Order status is required")
    @Pattern(regexp = "OPEN", message = "Order status must be 'OPEN'")
    @Schema(description = "Order status", example = "OPEN", required = true, allowableValues = { "OPEN" })
    @JsonProperty("orderStatus")
    private String orderStatus;

    @NotBlank(message = "Location code is required")
    @Size(min = 1, max = 255, message = "Location code must be between 1 and 255 characters")
    @Schema(description = "Location code for the order", example = "HOSP-NYC-001", required = true)
    @JsonProperty("locationCode")
    private String locationCode;

    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$", message = "Create date must be in format YYYY-MM-DD HH:MM:SS")
    @Schema(description = "Order creation date", example = "2025-08-14 10:30:00")
    @JsonProperty("createDate")
    private String createDate;

    @NotBlank(message = "Shipment type is required")
    @Pattern(regexp = "CUSTOMER", message = "Shipment type must be 'CUSTOMER'")
    @Schema(description = "Shipment type", example = "CUSTOMER", required = true, allowableValues = { "CUSTOMER" })
    @JsonProperty("shipmentType")
    private String shipmentType;

    @Pattern(regexp = "DATE_TIME|SCHEDULED|STAT|ROUTINE|ASAP", message = "Delivery type must be one of: DATE_TIME, SCHEDULED, STAT, ROUTINE, ASAP")
    @Schema(description = "Delivery type", example = "ROUTINE", allowableValues = { "DATE_TIME", "SCHEDULED", "STAT",
            "ROUTINE", "ASAP" })
    @JsonProperty("deliveryType")
    private String deliveryType;

    @NotBlank(message = "Product category is required")
    @Size(min = 1, message = "Product category cannot be empty")
    @Schema(description = "Product category", example = "BLOOD_PRODUCTS", required = true)
    @JsonProperty("productCategory")
    private String productCategory;

    @NotNull(message = "Order items are required")
    @NotEmpty(message = "At least one order item is required")
    @Valid
    @Schema(description = "List of order items", required = true)
    @JsonProperty("orderItems")
    private List<OrderItemRequest> orderItems;

    /**
     * Nested DTO for order line items.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Order line item")
    public static class OrderItemRequest {

        @NotBlank(message = "Product family is required")
        @Schema(description = "Product family", example = "RED_BLOOD_CELLS_LEUKOREDUCED", required = true)
        @JsonProperty("productFamily")
        private String productFamily;

        @NotBlank(message = "Blood type is required")
        @Size(min = 1, message = "Blood type cannot be empty")
        @Schema(description = "Blood type", example = "O-", required = true)
        @JsonProperty("bloodType")
        private String bloodType;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Quantity requested", example = "2", required = true, minimum = "1")
        @JsonProperty("quantity")
        private Integer quantity;

        @Schema(description = "Additional comments for this item", example = "Urgent request")
        @JsonProperty("comments")
        private String comments;
    }
}