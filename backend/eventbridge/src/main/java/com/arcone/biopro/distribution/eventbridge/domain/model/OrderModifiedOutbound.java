package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record OrderModifiedOutbound(
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
    String modifyEmployeeId,
    Instant modifyDate,
    String modifyReason,
    String transactionId
) {
    public OrderModifiedOutbound {
        if (transactionId == null) throw new IllegalArgumentException("transactionId cannot be null");
        if (externalId == null) throw new IllegalArgumentException("externalId cannot be null");
        if (orderNumber == null) throw new IllegalArgumentException("orderNumber cannot be null");
        if (modifyReason == null || modifyReason.isEmpty()) throw new IllegalArgumentException("modifyReason cannot be null or empty");
        if (modifyDate == null) throw new IllegalArgumentException("modifyDate cannot be null");
        if (orderItems == null || orderItems.isEmpty()) throw new IllegalArgumentException("orderItems cannot be null or empty");
        if (desiredShippingDate == null) throw new IllegalArgumentException("desiredShippingDate cannot be null");
    }
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
