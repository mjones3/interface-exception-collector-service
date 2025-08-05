package com.arcone.biopro.distribution.eventbridge.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaShipmentClosedPayload",
    title = "RecoveredPlasmaShipmentClosedPayload",
    description = "Recovered Plasma Shipment Closed Event Payload"
)
@Builder
public record RecoveredPlasmaShipmentClosedPayload(
    String locationCode,
    String productType,
    String shipmentNumber,
    String status,
    String createEmployeeId,
    String transportationReferenceNumber,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate shipmentDate,
    Integer cartonTareWeight,
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
    String locationShipmentCode,
    String locationCartonCode,
    List<RecoveredPlasmaShipmentCartonClosedDTO> cartonList
) implements Serializable {

}
