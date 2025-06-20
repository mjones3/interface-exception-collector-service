package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Builder
public record ModifyOrderReceivedPayloadDTO(

    String externalId,
    String locationCode,
    String modifyReason,
    String modifyDate,
    String modifyEmployeeCode,
    String deliveryType,
    String shippingMethod,
    String productCategory,
    String desiredShippingDate,
    boolean willPickUp,
    String willPickUpPhoneNumber,
    String comments,
    List<OrderItemEventDTO> orderItems,
    UUID transactionId

) implements Serializable {
}
