package com.arcone.biopro.distribution.shippingservice.verification.support.Types;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipmentRequestDetailsResponseType {
    Long id;
    Long orderNumber;
    String priority;
    String status;
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

    List<ShipmentFulfillmentRequest> items;
}
