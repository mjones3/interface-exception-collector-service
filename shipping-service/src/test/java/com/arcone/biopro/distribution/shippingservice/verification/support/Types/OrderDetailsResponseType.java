package com.arcone.biopro.distribution.shippingservice.verification.support.Types;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class OrderDetailsResponseType {
    Long id;
    Long orderNumber;
    OrderPriority priority;
    OrderStatus status;
    ZonedDateTime createDate;
    Long shippingCustomerCode;
    Long billingCustomerCode;
    Long locationCode;
    String deliveryType;
    String shippingMethod;
    String productCategory;
    LocalDate shippingDate;
    String shippingCustomerName;
    String billingCustomerName;
    String customerPhoneNumber;
    String customerAddressState;
    String customerAddressPostalCode;
    String customerAddressCountry;
    String customerAddressCountryCode;
    String customerAddressCity;
    String customerAddressDistrict;
    String customerAddressAddressLine1;
    String customerAddressAddressLine2;

    List<OrderItemResponseType> items;
}
