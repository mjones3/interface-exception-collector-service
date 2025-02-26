package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto;


import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ModifyOrderDTO(
    String externalId,
    String locationCode,
    String modifyDate,
    String modifyEmployeeCode,
    String shipmentType,
    String deliveryType,
    String shippingMethod,
    String productCategory,
    String desiredShippingDate,
    boolean willPickUp,
    String willPickUpPhoneNumber,
    String comments,
    List<OrderItemDTO> orderItems
) implements Serializable {


}
