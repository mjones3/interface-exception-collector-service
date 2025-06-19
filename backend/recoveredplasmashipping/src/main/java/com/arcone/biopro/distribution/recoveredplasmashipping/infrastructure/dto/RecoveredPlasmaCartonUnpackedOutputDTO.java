package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaCartonUnpackedPayload",
    title = "RecoveredPlasmaCartonUnpackedPayload",
    description = "Recovered Plasma Carton Unpacked Event Payload"
)
public record RecoveredPlasmaCartonUnpackedOutputDTO(
    String cartonNumber,
    String locationCode,
    Integer cartonSequence,
    String unpackEmployeeId,
    ZonedDateTime unpackDate,
    String productType,
    String status,
    int totalProducts,
    List<RecoveredPlasmaCartonItemUnpackedOutputDTO> unpackedProducts
) implements Serializable {
}
