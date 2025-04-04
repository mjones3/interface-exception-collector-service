package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Builder
public record RecoveredPlasmaShipmentResponseDTO(

    Long id,
    String locationCode,
    String productType,
    String shipmentNumber,
    String status,
    String createEmployeeId,
    String closeEmployeeId,
    ZonedDateTime closeDate,
    String transportationReferenceNumber,
    LocalDate shipmentDate,
    BigDecimal cartonTareWeight,
    String unsuitableUnitReportDocumentStatus,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    String customerCode,
    String customerName,
    String customerState,
    String customerPostalCode,
    String customerCountry,
    String customerCountryCode,
    String customerCity,
    String customerDistrict,
    String customerAddressLine1,
    String customerAddressLine2,
    String customerAddressContactName,
    String customerAddressPhoneNumber,
    String customerAddressDepartmentName

) implements Serializable {


}
