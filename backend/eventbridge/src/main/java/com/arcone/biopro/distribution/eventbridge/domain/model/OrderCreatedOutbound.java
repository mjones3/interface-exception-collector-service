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
) implements Validatable {

    @Builder
    public record OrderItem(
        String productFamily,
        String bloodType,
        Integer quantity,
        String comments
    ) {
    }
}