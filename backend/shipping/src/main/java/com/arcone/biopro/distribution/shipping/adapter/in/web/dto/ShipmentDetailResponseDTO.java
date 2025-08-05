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
    String externalId,
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
    List<ShipmentItemResponseDTO> items,
    boolean checkDigitActive,
    boolean visualInspectionActive,
    boolean secondVerificationActive,
    String departmentName,
    String departmentCode,
    String labelStatus,
    String shipmentType,
    Boolean quarantinedProducts
) implements Serializable {
}
