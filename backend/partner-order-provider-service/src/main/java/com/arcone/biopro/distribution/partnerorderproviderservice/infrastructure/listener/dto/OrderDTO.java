package com.arcone.biopro.distribution.partnerorderproviderservice.infrastructure.listener.dto;


import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderDTO(
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
    OrderPickUpTypeDTO orderPickUpType,
    List<OrderItemDTO> orderItems
) implements Serializable {


}
