package com.arcone.biopro.distribution.eventbridge.application.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record RecoveredPlasmaShipmentCartonItemClosedDTO(
    String unitNumber,
    String productCode,
    String packedByEmployeeId,
    String productType,
    Integer volume,
    Integer weight,
    String aboRh,
    ZonedDateTime donationDate,
    String drawBeginTime,
    String collectionFacility,
    String collectionTimeZone
) implements Serializable {
}
