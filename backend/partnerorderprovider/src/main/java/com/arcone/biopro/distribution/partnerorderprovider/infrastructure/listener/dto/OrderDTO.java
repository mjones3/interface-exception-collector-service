package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "OrderReceivedPayload",
    title = "OrderReceivedPayload",
    description = "Order Received Event Payload"
)
@Builder
public record OrderDTO(
    @Schema(
        title = "Transaction ID",
        description = "The transaction ID",
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID transactionId,
    @Schema(
        title = "External Order ID",
        description = "The external order ID",
        example = "ABC56865",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String externalId,
    @Schema(
        title = "Order Status",
        description = "The order status",
        example = "OPEN",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String orderStatus,
    @Schema(
        title = "Location Code",
        description = "The location code",
        example = "565",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String locationCode,
    @Schema(
        title = "Create Date",
        description = "The create date",
        example = "2023-04-25 20:09:01",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String createDate,
    @Schema(
        title = "Create Employee Code",
        description = "The create employee code",
        example = "ee1bf88e-2137-4a17-835a-d43e7b738374",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String createEmployeeCode,
    @Schema(
        title = "Shipment Type",
        description = "The shipment type",
        example = "CUSTOMER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String shipmentType,
    @Schema(
        title = "Delivery Type",
        description = "The delivery type",
        example = "SCHEDULED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String deliveryType,
    @Schema(
        title = "Shipping Method",
        description = "The shipping method",
        example = "FEDEX",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String shippingMethod,
    @Schema(
        title = "Product Category",
        description = "The product category",
        example = "FROZEN",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String productCategory,
    @Schema(
        title = "Desire Shipping Date",
        description = "The desire shipping date",
        example = "2020-04-25",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String desiredShippingDate,
    @Schema(
        title = "Shipping Customer Code",
        description = "The shipping customer code",
        example = "A1235",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String shippingCustomerCode,
    @Schema(
        title = "Billing Customer Code",
        description = "The billing customer code",
        example = "A1235",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String billingCustomerCode,
    @Schema(
        title = "Will Pick Up",
        description = "The will pick up",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    boolean willPickUp,
    @Schema(
        title = "Will Pick Up Phone Number",
        description = "The phone number from will pick up",
        example = "12333333",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String willPickUpPhoneNumber,
    @Schema(
        title = "Comments",
        description = "The order comments",
        example = "Comments",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String comments,
    @Schema(
        title = "Order Items",
        description = "The order items",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<OrderItemDTO> orderItems,

    @Schema(
        title = "Quarantined Products",
        description = "The order contains quarantined products",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    boolean quarantinedProducts,

    @Schema(
        title = "Label Status",
        description = "The label status",
        example = "LABELED",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String labelStatus
) implements Serializable {


}
