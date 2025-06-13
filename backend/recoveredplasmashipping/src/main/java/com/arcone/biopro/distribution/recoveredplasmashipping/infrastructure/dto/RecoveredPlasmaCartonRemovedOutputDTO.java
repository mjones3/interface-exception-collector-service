package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaCartonRemovedPayload",
    title = "RecoveredPlasmaCartonRemovedPayload",
    description = "Recovered Plasma Carton Removed Event Payload"
)
public record RecoveredPlasmaCartonRemovedOutputDTO(
    String cartonNumber,
    String locationCode,
    Integer cartonSequence,
    String removeEmployeeId,
    ZonedDateTime removeDate,
    String productType,
    String status,
    int totalProducts,
    List<RecoveredPlasmaCartonItemUnpackedOutputDTO> unpackedProducts
) implements Serializable {
}
