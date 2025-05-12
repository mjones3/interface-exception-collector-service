package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;


@Schema(
    name = "RecoveredPlasmaCartonItemClosedPayload",
    title = "RecoveredPlasmaCartonItemClosedPayload",
    description = "Recovered Plasma Carton Item Closed Event Payload"
)
public record RecoveredPlasmaCartonItemClosedOutputDTO(

    String unitNumber,
    String productCode,
    String packedByEmployeeId,
    String productType,
    Integer volume,
    Integer weight,
    String aboRh,
    ZonedDateTime donationDate,
    LocalDateTime drawBeginTime,
    String collectionFacility

) implements Serializable {
}
