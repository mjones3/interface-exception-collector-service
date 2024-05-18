package com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record OrderFulfilledDetailResponseDTO(
    Long id,
    Long orderNumber,
    OrderPriority priority,
    OrderStatus status,
    ZonedDateTime createDate,
    Long shippingCustomerCode,
    Long billingCustomerCode,
    Long locationCode,
    String deliveryType,
    String shippingMethod,
    String productCategory,
    LocalDate shippingDate,
    String shippingCustomerName,
    String billingCustomerName,
    String customerPhoneNumber,
    String customerAddressState,
    String customerAddressPostalCode,
    String customerAddressCountry,
    String customerAddressCountryCode,
    String customerAddressCity,
    String customerAddressDistrict,
    String customerAddressAddressLine1,
    String customerAddressAddressLine2,

    List<OrderFulFilledItemResponseDTO> items

) implements Serializable {
}
