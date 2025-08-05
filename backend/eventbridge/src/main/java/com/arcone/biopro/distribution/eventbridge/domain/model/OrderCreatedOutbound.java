package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderCreatedOutbound(
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
    UUID transactionId,
    List<OrderItem> orderItems
)  {
    public OrderCreatedOutbound {
        if (orderNumber == null) throw new IllegalArgumentException("orderNumber cannot be null");
        if (externalId == null) throw new IllegalArgumentException("externalId cannot be null");
        if (shipmentType == null) throw new IllegalArgumentException("shipmentType cannot be null");
        if (desiredShippingDate == null) throw new IllegalArgumentException("desiredShippingDate cannot be null");
        if (shippingCustomerCode == null) throw new IllegalArgumentException("shippingCustomerCode cannot be null");
        if (billingCustomerCode == null) throw new IllegalArgumentException("billingCustomerCode cannot be null");
        if (transactionId == null) throw new IllegalArgumentException("transactionId cannot be null");
    }

    @Builder
    public record OrderItem(
        String productFamily,
        String bloodType,
        Integer quantity,
        String comments
    ) {
    }
}
