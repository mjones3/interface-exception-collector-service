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

/*
{
    "eventId": "a575b4e3-bc3e-4054-b8e7-e5a1cff25aef",
    "occurredOn": "2025-04-04T20:51:41.536368898Z",
    "eventType": "RecoveredPlasmaShipmentClosed",
    "eventVersion": "1.0",
    "payload": {
    "locationCode": "123456789",
        "productType": "RP_FROZEN_WITHIN_72_HOURS",
        "shipmentNumber": "BPM27652",
        "closedEmployeeId": "4c973896-5761-41fc-8217-07c5d13a004b",
        "closeDate": "2025-04-04T20:51:41.467383564Z",
        "transportationReferenceNumber": "565656",
        "shipmentDate": "2025-04-04",
        "customerCode": "409",
        "customerName": "Southern Biologics",
        "customerState": "FL",
        "customerPostalCode": "32303",
        "customerCountry": "USA",
        "customerCountryCode": "USA",
        "customerCity": "Tallahassee",
        "customerDistrict": "",
        "customerAddressLine1": "4801 Woodlane Circle",
        "customerAddressLine2": "Unit 105",
        "customerAddressContactName": null,
        "customerAddressPhoneNumber": null,
        "totalCartons":150,
        "cartonList": [
    {
        "cartonNumber": "BPM4565",
        "cartonSequence": 1,
        "totalProducts": 20,
        "totalWeight": 50.56,
        "totalVolume": 58.989,
        "packedProducts": [
        {
            "unitNumber": "W036898786800",
            "productCode": "E6022V00",
            "productType":"RP_FROZEN_WITHIN_120_HOURS",
            "volume":165,
            "weight":150,
            "aboRh":"AP",
            "donationDate":"2025-12-03T10:15:30+01:00",
            "drawBeginTime":"2025-09-03T10:15:30",
            "collectionFacility":"13562"
        }
        ]
    }
    ]
}
}
*/
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
    List<RecoveredPlasmaCartonClosedOutputDTO> cartonList
) implements Serializable {
}
