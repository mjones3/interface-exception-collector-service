package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaCartonPackedPayload",
    title = "RecoveredPlasmaCartonPackedPayload",
    description = "Recovered Plasma Carton Packed Event Payload"
)
public record RecoveredPlasmaCartonPackedOutputDTO(
    String cartonNumber,
    String locationCode,
    Integer cartonSequence,
    String closeEmployeeId,
    ZonedDateTime closeDate,
    String productType,
    String status,
    int totalProducts,
    BigDecimal totalWeight,
    BigDecimal totalVolume,
    List<RecoveredPlasmaCartonItemPackedOutputDTO> packedProducts
) implements Serializable {
}
