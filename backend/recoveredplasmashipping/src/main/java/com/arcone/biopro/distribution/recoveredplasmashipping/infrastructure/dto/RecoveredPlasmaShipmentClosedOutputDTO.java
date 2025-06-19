package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaShipmentClosedPayload",
    title = "RecoveredPlasmaShipmentClosedPayload",
    description = "Recovered Plasma Shipment Closed Event Payload"
)
@Builder
public record RecoveredPlasmaShipmentClosedOutputDTO(
    String locationCode,
    String productType,
    String shipmentNumber,
    String status,
    String createEmployeeId,
    String transportationReferenceNumber,
    LocalDate shipmentDate,
    BigDecimal cartonTareWeight,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    String closedEmployeeId,
    ZonedDateTime closeDate,
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
    Integer totalCartons,
    List<RecoveredPlasmaCartonClosedOutputDTO> cartonList,
    String locationShipmentCode,
    String locationCartonCode
) implements Serializable {
}
