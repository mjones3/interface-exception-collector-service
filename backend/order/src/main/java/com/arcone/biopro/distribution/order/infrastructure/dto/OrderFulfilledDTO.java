package com.arcone.biopro.distribution.order.infrastructure.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode
@ToString
public class OrderFulfilledDTO implements Serializable {
    private Long id;
    private Long orderNumber;
    private String externalId;
    private String shippingCustomerCode;
    private String billingCustomerCode;
    private String deliveryType;
    private String locationCode;
    private String shippingMethod;
    private String productCategory;
    private LocalDate shippingDate;
    private String priority;
    private String status;
    private String shippingCustomerName;
    private String billingCustomerName;
    private String customerPhoneNumber;
    private String customerAddressState;
    private String customerAddressPostalCode;
    private String customerAddressCountry;
    private String customerAddressCountryCode;
    private String customerAddressCity;
    private String customerAddressDistrict;
    private String customerAddressAddressLine1;
    private String customerAddressAddressLine2;
    private String comments;
    private String departmentName;
    private String departmentCode;
    private UUID transactionId;
    private List<OrderFulfilledItemDTO> items;
}
