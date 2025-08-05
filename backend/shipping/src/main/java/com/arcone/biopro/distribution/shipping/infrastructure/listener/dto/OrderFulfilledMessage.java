package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Schema(
    name = "OrderFulfilledPayload",
    title = "OrderFulfilledPayload",
    description = "Order Fulfilled Event Payload"
)
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
    String departmentCode,
    List<OrderItemFulfilledMessage> items,
    String shipmentType,
    String labelStatus,
    Boolean quarantinedProducts

) implements Serializable {
}
