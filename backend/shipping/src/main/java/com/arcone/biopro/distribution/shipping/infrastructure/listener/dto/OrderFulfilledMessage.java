package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record OrderFulfilledMessage(


    Long id,
    Long orderNumber,
    String externalId,
    String shippingCustomerCode,
    String billingCustomerCode,
    String locationCode,
    String deliveryType,

    String shippingMethod,

    String productCategory,
    LocalDate shippingDate,

    String priority,
    String status,
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

    String comments,
    String departmentName,

    List<OrderItemFulfilledMessage> items

) implements Serializable {
}
