package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Schema(
    name = "RecoveredPlasmaShipmentCreatedPayload",
    title = "RecoveredPlasmaShipmentCreatedPayload",
    description = "Recovered Plasma Shipment Created Event Payload"
)
public record RecoveredPlasmaShipmentCreatedOutputDTO (
    String locationCode,
    String productType,
    String shipmentNumber,
    String status,
    String createEmployeeId,
    String transportationReferenceNumber,
    LocalDate scheduleDate,
    BigDecimal cartonTareWeight,
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
