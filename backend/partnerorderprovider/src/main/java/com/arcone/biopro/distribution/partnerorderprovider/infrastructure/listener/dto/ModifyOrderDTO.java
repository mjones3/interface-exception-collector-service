package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "ModifyOrderPayload",
    title = "ModifyOrderPayload",
    description = "Modify Order Received Event Payload"
)
@Builder
public record ModifyOrderDTO(
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
        title = "Location Code",
        description = "The location code",
        example = "565",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String locationCode,
    @Schema(
        title = "Modify Reason",
        description = "The modify reason",
        example = "Missing product details.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String modifyReason,
    @Schema(
        title = "Modify Date",
        description = "The modify date",
        example = "2023-04-25 20:09:01",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String modifyDate,
    @Schema(
        title = "Modify Employee Code",
        description = "The modify employee code",
        example = "ee1bf88e-2137-4a17-835a-d43e7b738374",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String modifyEmployeeCode,
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
        title = "Will Pick Up",
        description = "The will pick up",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
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
    String labelStatus,

    @Schema(
        title = "Order Items",
        description = "The order items",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<OrderItemDTO> orderItems

) implements Serializable {


}
