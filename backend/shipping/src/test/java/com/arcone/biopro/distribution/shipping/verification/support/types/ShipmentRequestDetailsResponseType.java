package com.arcone.biopro.distribution.shipping.verification.support.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipmentRequestDetailsResponseType {
    Long id;
    Long orderNumber;
    String externalId;
    String priority;
    String status;
    ZonedDateTime createDate;
    String shippingCustomerCode;
    Long billingCustomerCode;
    String locationCode;
    String deliveryType;
    String shippingMethod;
    String productCategory;
    LocalDate shippingDate;
    String department;
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
    String customerAddressAddressComplement;
    String comments;

    List<ShipmentFulfillmentRequest> items;
}
