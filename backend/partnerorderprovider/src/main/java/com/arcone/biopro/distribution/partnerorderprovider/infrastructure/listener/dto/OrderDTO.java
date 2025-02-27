package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto;


import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderDTO(
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
    String shippingCustomerCode,
    String billingCustomerCode,

    boolean willPickUp,
    String willPickUpPhoneNumber,

    String comments,
    List<OrderItemDTO> orderItems
) implements Serializable {


}
