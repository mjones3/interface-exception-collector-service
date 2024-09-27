package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ShipmentDetailResponseDTO(
    Long id,
    Long orderNumber,
    ShipmentPriority priority,
    ShipmentStatus status,
    ZonedDateTime createDate,
    String shippingCustomerCode,
    String locationCode,
    String deliveryType,
    String shippingMethod,
    String productCategory,
    LocalDate shippingDate,
    String shippingCustomerName,
    String customerPhoneNumber,
    String customerAddressState,
    String customerAddressPostalCode,
    String customerAddressCountry,
    String customerAddressCountryCode,
    String customerAddressCity,
    String customerAddressDistrict,
    String customerAddressAddressLine1,
    String customerAddressAddressLine2,
    ZonedDateTime completeDate,
    String completedByEmployeeId,
    String comments,
    List<ShipmentItemResponseDTO> items

) implements Serializable {
}
