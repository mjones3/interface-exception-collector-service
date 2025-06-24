package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record OrderCancelledOutbound(
    Integer orderNumber,
    String externalId,
    String orderStatus,
    String locationCode,
    Instant createDate,
    String createEmployeeCode,
    String shipmentType,
    String priority,
    String shippingMethod,
    String productCategory,
    LocalDate desiredShippingDate,
    String shippingCustomerCode,
    String billingCustomerCode,
    String comments,
    Boolean willPickUp,
    String willPickUpPhoneNumber,
    Integer totalShipped,
    Integer totalRemaining,
    Integer totalProducts,
    List<OrderItem> orderItems,
    String cancelEmployeeId,
    Instant cancelDate,
    String cancelReason,
    String transactionId
) {
    @Builder
    public record OrderItem(
        String productFamily,
        String bloodType,
        Integer quantity,
        Integer quantityShipped,
        Integer quantityRemaining,
        String comments
    ) {
    }
}
