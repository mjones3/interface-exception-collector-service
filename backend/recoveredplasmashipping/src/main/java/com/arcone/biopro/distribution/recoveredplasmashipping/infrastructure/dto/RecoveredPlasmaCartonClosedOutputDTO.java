package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "RecoveredPlasmaCartonClosedPayload",
    title = "RecoveredPlasmaCartonClosedPayload",
    description = "Recovered Plasma Carton Closed Event Payload"
)

@Builder
public record RecoveredPlasmaCartonClosedOutputDTO(
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
    List<RecoveredPlasmaCartonItemClosedOutputDTO> packedProducts
) implements Serializable {
}
