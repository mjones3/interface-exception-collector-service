package com.arcone.biopro.distribution.order.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Schema(
    name = "OrderModifiedPayload",
    title = "OrderModifiedPayload",
    description = "Order Modified Event Payload"
)
@Builder
public record OrderModifiedDTO(
    @Schema(
        title = "Order ID",
        description = "The order ID",
        example = "153",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long orderNumber,
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
        example = "123456789",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String locationCode,
    @Schema(
        title = "Create date",
        description = "The date order was created",
        example = "2024-10-03T15:44:42.328889299Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    ZonedDateTime createDate,
    @Schema(
        title = "Create Employee Code",
        description = "The employee ID that created the order",
        example = "4c973896-5761-41fc-8217-07c5d13a004b",
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
        title = "Priority",
        description = "The order priority",
        example = "ASAP",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String priority,
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
        example = "2024-10-03",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    LocalDate desiredShippingDate,
    @Schema(
        title = "Shipping Customer Code",
        description = "The shipping customer code",
        example = "ABC123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String shippingCustomerCode,
    @Schema(
        title = "Billing Customer Code",
        description = "The billing customer code",
        example = "ABC123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String billingCustomerCode,
    @Schema(
        title = "Comments",
        description = "The comments",
        example = "Please delivery ASAP",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String comments,
    @Schema(
        title = "Will Pick UP",
        description = "The will pick up",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean willPickUp,
    @Schema(
        title = "Will Pick UP Phone Number",
        description = "The will pick up phone number",
        example = "12333333",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String willPickUpPhoneNumber,
    @Schema(
        title = "Total Shipped",
        description = "The total of shipped products",
        example = "10",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Integer totalShipped,
    @Schema(
        title = "Total Remaining",
        description = "The total of remaining products",
        example = "10",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Integer totalRemaining,
    @Schema(
        title = "Total Products",
        description = "The total of requested products",
        example = "10",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Integer totalProducts,
    @Schema(
        title = "Order Items",
        description = "The list of product criteria for the order",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<OrderItemCancelledDTO> orderItems,
    @Schema(
        title = "Modify Employee ID",
        description = "The employee ID of the employee that modified the order",
        example = "4c973896-5761-41fc-8217-07c5d13a004b",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String modifyEmployeeId,
    @Schema(
        title = "Modify Date",
        description = "The date order was modified",
        example = "2024-10-03T15:44:42.328889299Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    ZonedDateTime modifyDate,
    @Schema(
        title = "Modify Reason",
        description = "The reason of modifying the order",
        example = "Customer Request",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String modifyReason,
    @Schema(
        title = "Transaction ID",
        description = "The unique transaction identifier",
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    UUID transactionId,
    @Schema(
        title = "Quarantined Products",
        description = "The order can be filled with quarantined products",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    Boolean quarantinedProducts,
    @Schema(
        title = "Label Status",
        description = "The label status of the order",
        example = "LABELED",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String labelStatus
) implements Serializable {
}
