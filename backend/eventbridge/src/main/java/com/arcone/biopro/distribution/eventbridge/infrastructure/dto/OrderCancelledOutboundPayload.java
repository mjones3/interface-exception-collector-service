package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(
    name = "OrderCancelledOutbound",
    title = "OrderCancelledOutbound",
    description = "Order Cancelled Outbound Event"
)
@Builder
public record OrderCancelledOutboundPayload(
    @Schema(name = "orderNumber", title = "Order number", description = "The order number", example = "1", requiredMode = REQUIRED)
    Integer orderNumber,
    @Schema(name = "externalId", title = "External ID", description = "The external order ID", example = "114117922233598", requiredMode = REQUIRED)
    String externalId,
    @Schema(name = "orderStatus", title = "Order status", description = "The order status", example = "CANCELLED", requiredMode = REQUIRED)
    String orderStatus,
    @Schema(name = "locationCode", title = "Location code", description = "The location code", example = "123456789", requiredMode = REQUIRED)
    String locationCode,
    @Schema(name = "createDate", title = "Create date", description = "The order creation date", example = "2025-06-18T11:15:03Z", requiredMode = REQUIRED)
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
    @Schema(name = "totalShipped", title = "Total shipped", description = "Total quantity shipped", example = "0", requiredMode = REQUIRED)
    Integer totalShipped,
    @Schema(name = "totalRemaining", title = "Total remaining", description = "Total quantity remaining", example = "1", requiredMode = REQUIRED)
    Integer totalRemaining,
    @Schema(name = "totalProducts", title = "Total products", description = "Total number of products", example = "1", requiredMode = REQUIRED)
    Integer totalProducts,
    @Schema(name = "orderItems", title = "Order items", description = "List of order items", requiredMode = REQUIRED)
    List<OrderItem> orderItems,
    @Schema(name = "cancelEmployeeId", title = "Cancel employee ID", description = "The employee ID who cancelled the order", example = "A1235", requiredMode = REQUIRED)
    String cancelEmployeeId,
    @Schema(name = "cancelDate", title = "Cancel date", description = "The order cancellation date", example = "2025-06-18T18:05:09.4763007Z", requiredMode = REQUIRED)
    Instant cancelDate,
    @Schema(name = "cancelReason", title = "Cancel reason", description = "The reason for cancellation", example = "Customer no longer need", requiredMode = REQUIRED)
    String cancelReason,
    @Schema(name = "transactionId", title = "Transaction ID", description = "The transaction ID", example = "72555983-8592-44a7-b5c1-1d7b4cc6b9c9", requiredMode = NOT_REQUIRED)
    String transactionId
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
        @Schema(name = "quantityShipped", title = "Quantity shipped", description = "The quantity shipped", example = "0", requiredMode = REQUIRED)
        Integer quantityShipped,
        @Schema(name = "quantityRemaining", title = "Quantity remaining", description = "The quantity remaining", example = "1", requiredMode = REQUIRED)
        Integer quantityRemaining,
        @Schema(name = "comments", title = "Comments", description = "Item comments", example = "Comments", requiredMode = NOT_REQUIRED)
        String comments
    ) implements Serializable {
    }
}
