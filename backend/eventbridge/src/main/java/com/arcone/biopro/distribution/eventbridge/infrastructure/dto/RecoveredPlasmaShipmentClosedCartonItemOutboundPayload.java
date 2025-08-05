package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(
    name = "RecoveredPlasmaShipmentClosedCartonItemOutboundPayload",
    title = "RecoveredPlasmaShipmentClosedCartonItemOutboundPayload"
)
@Builder
public record RecoveredPlasmaShipmentClosedCartonItemOutboundPayload(
        String unitNumber,
        String productCode,
        BigDecimal productVolume,
        String bloodType,
        String isbt128Flag,
        String collectionDate,
        String drawBeginTime,
        String collectionTimeZone,
        String collectionFacility
) implements Serializable {

}
