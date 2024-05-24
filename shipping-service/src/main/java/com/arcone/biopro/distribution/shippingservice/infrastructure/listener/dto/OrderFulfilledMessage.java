package com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record OrderFulfilledMessage(


    Long id,
    Long orderNumber,

    Long shippingCustomerCode,

    Long billingCustomerCode,
    Integer locationCode,

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


    List<OrderItemFulfilledMessage> items

) implements Serializable {}
