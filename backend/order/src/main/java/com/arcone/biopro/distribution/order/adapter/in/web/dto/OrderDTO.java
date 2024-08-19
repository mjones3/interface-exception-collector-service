package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record OrderDTO(
    Long id,
    Long orderNumber,
    String externalId,
    String locationCode,
    String shipmentType,
    String shippingMethod,
    String shippingCustomerName,
    String shippingCustomerCode,
    String billingCustomerName,
    String billingCustomerCode,
    LocalDate desiredShippingDate,
    Boolean willCallPickup,
    String phoneNumber,
    String productCategory,
    String comments,
    String status,
    String priority,
    String createEmployeeId,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    ZonedDateTime deleteDate,
    List<OrderItemDTO> orderItems
) {}
