package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(
    name = "OrderCreatedPayload",
    title = "OrderCreatedPayload",
    description = "Order Created Event Payload"
)
@Builder
public record OrderCreatedPayload(
    @Schema(name = "orderNumber", title = "Order number", description = "The order number", example = "2", requiredMode = REQUIRED)
    Integer orderNumber,
    @Schema(name = "externalId", title = "External ID", description = "The external order ID", example = "114117922233598", requiredMode = REQUIRED)
    String externalId,
    @Schema(name = "orderStatus", title = "Order status", description = "The order status", example = "OPEN", requiredMode = REQUIRED)
    String orderStatus,
    @Schema(name = "locationCode", title = "Location code", description = "The location code", example = "123456789", requiredMode = REQUIRED)
    String locationCode,
    @Schema(name = "createDate", title = "Create date", description = "The order creation date", example = "2025-06-18T14:25:58Z", requiredMode = REQUIRED)
    Instant createDate,
    @Schema(name = "createEmployeeCode", title = "Create employee code", description = "The employee code who created the order", example = "A1235", requiredMode = REQUIRED)
    String createEmployeeCode,
    @Schema(name = "shipmentType", title = "Shipment type", description = "The shipment type", example = "CUSTOMER", requiredMode = REQUIRED)
    String shipmentType,
    @Schema(name = "priority", title = "Priority", description = "The order priority", example = "SCHEDULED", requiredMode = REQUIRED)
    String priority,
    @Schema(name = "shippingMethod", title = "Shipping method", description = "The shipping method", example = "FEDEX", requiredMode = REQUIRED)
    String shippingMethod,
    @Schema(name = "productCategory", title = "Product category", description = "The product category", example = "REFRIGERATED", requiredMode = REQUIRED)
    String productCategory,
    @Schema(name = "desiredShippingDate", title = "Desired shipping date", description = "The desired shipping date", example = "2025-07-25", requiredMode = NOT_REQUIRED)
    LocalDate desiredShippingDate,
    @Schema(name = "shippingCustomerCode", title = "Shipping customer code", description = "The shipping customer code", example = "B2346", requiredMode = REQUIRED)
    String shippingCustomerCode,
    @Schema(name = "billingCustomerCode", title = "Billing customer code", description = "The billing customer code", example = "B2346", requiredMode = REQUIRED)
    String billingCustomerCode,
    @Schema(name = "comments", title = "Comments", description = "Order comments", requiredMode = NOT_REQUIRED)
    String comments,
    @Schema(name = "willPickUp", title = "Will pick up", description = "Whether the order will be picked up", example = "true", requiredMode = REQUIRED)
    Boolean willPickUp,
    @Schema(name = "willPickUpPhoneNumber", title = "Will pick up phone number", description = "Phone number for pickup", example = "12333333", requiredMode = NOT_REQUIRED)
    String willPickUpPhoneNumber,
    @Schema(name = "transactionId", title = "Transaction ID", description = "The transaction ID", example = "bb5df8a6-e9ec-42ea-90e8-ac9b7326c8ae", requiredMode = NOT_REQUIRED)
    UUID transactionId,
    @Schema(name = "orderItems", title = "Order items", description = "List of order items", requiredMode = REQUIRED)
    List<OrderItem> orderItems
) implements Serializable {

    @Schema(
        name = "OrderItem",
        title = "OrderItem",
        description = "Order item details"
    )
    @Builder
    public record OrderItem(
        @Schema(name = "productFamily", title = "Product family", description = "The product family", example = "RED_BLOOD_CELLS_LEUKOREDUCED", requiredMode = REQUIRED)
        String productFamily,
        @Schema(name = "bloodType", title = "Blood type", description = "The blood type", example = "ABN", requiredMode = REQUIRED)
        String bloodType,
        @Schema(name = "quantity", title = "Quantity", description = "The quantity", example = "1", requiredMode = REQUIRED)
        Integer quantity,
        @Schema(name = "comments", title = "Comments", description = "Item comments", example = "Comments", requiredMode = NOT_REQUIRED)
        String comments
    ) implements Serializable {
    }
}