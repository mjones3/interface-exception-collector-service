package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(
    name = "OrderOutboundPayload",
    title = "OrderOutboundPayload",
    description = "Order Outbound Event Payload"
)
@Builder
public record OrderOutboundPayload(
    @Schema(name = "orderNumber", title = "Order number", description = "The order number", example = "1", requiredMode = REQUIRED)
    Integer orderNumber,
    @Schema(name = "externalId", title = "External ID", description = "The external order ID", example = "EXTDIS3150001", requiredMode = REQUIRED)
    String externalId,
    @Schema(name = "orderStatus", title = "Order status", description = "The order status", example = "CANCELLED", requiredMode = REQUIRED)
    String orderStatus,
    @Schema(name = "locationCode", title = "Location code", description = "The location code", example = "123456789", requiredMode = REQUIRED)
    String locationCode,
    @Schema(name = "createDate", title = "Create date", description = "The order creation date", example = "2025-06-23T03:00:00Z", requiredMode = REQUIRED)
    Instant createDate,
    @Schema(name = "createEmployeeCode", title = "Create employee code", description = "The employee code who created the order", example = "ee1bf88e-2137-4a17-835a-d43e7b738374", requiredMode = REQUIRED)
    String createEmployeeCode,
    @Schema(name = "shipmentType", title = "Shipment type", description = "The shipment type", example = "CUSTOMER", requiredMode = REQUIRED)
    String shipmentType,
    @Schema(name = "priority", title = "Priority", description = "The order priority", example = "SCHEDULED", requiredMode = REQUIRED)
    String priority,
    @Schema(name = "shippingMethod", title = "Shipping method", description = "The shipping method", example = "FEDEX", requiredMode = REQUIRED)
    String shippingMethod,
    @Schema(name = "productCategory", title = "Product category", description = "The product category", example = "FROZEN", requiredMode = REQUIRED)
    String productCategory,
    @Schema(name = "desiredShippingDate", title = "Desired shipping date", description = "The desired shipping date", example = "2026-12-25", requiredMode = NOT_REQUIRED)
    LocalDate desiredShippingDate,
    @Schema(name = "shippingCustomerCode", title = "Shipping customer code", description = "The shipping customer code", example = "A1235", requiredMode = REQUIRED)
    String shippingCustomerCode,
    @Schema(name = "billingCustomerCode", title = "Billing customer code", description = "The billing customer code", example = "A1235", requiredMode = REQUIRED)
    String billingCustomerCode,
    @Schema(name = "comments", title = "Comments", description = "Order comments", requiredMode = NOT_REQUIRED)
    String comments,
    @Schema(name = "willPickUp", title = "Will pick up", description = "Whether the order will be picked up", example = "false", requiredMode = REQUIRED)
    Boolean willPickUp,
    @Schema(name = "willPickUpPhoneNumber", title = "Will pick up phone number", description = "Phone number for pickup", requiredMode = NOT_REQUIRED)
    String willPickUpPhoneNumber,
    @Schema(name = "totalShipped", title = "Total shipped", description = "Total quantity shipped", example = "0", requiredMode = NOT_REQUIRED)
    Integer totalShipped,
    @Schema(name = "totalRemaining", title = "Total remaining", description = "Total quantity remaining", example = "10", requiredMode = NOT_REQUIRED)
    Integer totalRemaining,
    @Schema(name = "totalProducts", title = "Total products", description = "Total quantity of products", example = "10", requiredMode = NOT_REQUIRED)
    Integer totalProducts,
    @Schema(name = "orderItems", title = "Order items", description = "List of order items", requiredMode = REQUIRED)
    List<OrderPayload.OrderItem> orderItems,
    @Schema(name = "cancelEmployeeId", title = "Cancel employee ID", description = "The employee ID who cancelled the order", requiredMode = NOT_REQUIRED)
    String cancelEmployeeId,
    @Schema(name = "cancelDate", title = "Cancel date", description = "The order cancellation date", requiredMode = NOT_REQUIRED)
    Instant cancelDate,
    @Schema(name = "cancelReason", title = "Cancel reason", description = "The reason for cancellation", requiredMode = NOT_REQUIRED)
    String cancelReason,
    @Schema(name = "modifyEmployeeId", title = "Modify employee ID", description = "The employee ID who modified the order", requiredMode = NOT_REQUIRED)
    String modifyEmployeeId,
    @Schema(name = "modifyDate", title = "Modify date", description = "The order modification date", requiredMode = NOT_REQUIRED)
    Instant modifyDate,
    @Schema(name = "modifyReason", title = "Modify reason", description = "The reason for modification", requiredMode = NOT_REQUIRED)
    String modifyReason,
    @Schema(name = "transactionId", title = "Transaction ID", description = "The transaction ID", requiredMode = NOT_REQUIRED)
    String transactionId
) implements Serializable {
}