package com.arcone.biopro.distribution.orderservice.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record OrderCreatedDTO(
    String eventId,
    Instant occurredOn,
    Long orderNumber,
    String externalId,
    String orderStatus,
    String locationCode,
    ZonedDateTime createDate,
    String createEmployeeCode,
    String shipmentType,
    String priority,
    String shippingMethod,
    String productCategory,
    LocalDate desiredShippingDate,
    String shippingCustomerCode,
    String billingCustomerCode,
    String comments,
    boolean willPickUp,
    String willPickUpPhoneNumber,
    List<OrderItemCreatedDTO> orderItems
) implements Serializable {
}
