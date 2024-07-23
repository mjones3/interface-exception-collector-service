package com.arcone.biopro.distribution.orderservice.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderReceivedEventDTO(
    UUID id,
    String externalId,
    String orderStatus,
    String locationCode,
    String createDate,
    String createEmployeeCode,
    String shipmentType,
    String deliveryType,
    String shippingMethod,
    String productCategory,
    String desiredShippingDate,
    Integer shippingCustomerCode,
    Integer billingCustomerCode,
    String comments,
    boolean willPickUp,
    String willPickUpPhoneNumber,
    List<OrderItemDTO> orderItems
) implements Serializable {
}
