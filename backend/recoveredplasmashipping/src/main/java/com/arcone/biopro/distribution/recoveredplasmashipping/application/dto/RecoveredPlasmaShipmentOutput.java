package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record RecoveredPlasmaShipmentOutput(

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
    String customerAddressDepartmentName,
    int totalCartons,
    int totalProducts,
    boolean canAddCartons,
    List<CartonOutput> cartonList

) implements Serializable {


}
