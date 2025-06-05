package com.arcone.biopro.distribution.eventbridge.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record RecoveredPlasmaShipmentCartonClosedDTO(
    String cartonNumber,
    String locationCode,
    Integer cartonSequence,
    String closeEmployeeId,
    ZonedDateTime closeDate,
    String productType,
    String status,
    Integer totalProducts,
    BigDecimal totalWeight,
    BigDecimal totalVolume,
    List<RecoveredPlasmaShipmentCartonItemClosedDTO> packedProducts
) implements Serializable {
}
