package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record CartonOutput(
    Long id,
    String cartonNumber,
    Long shipmentId,
    Integer cartonSequence,
    String createEmployeeId,
    String closeEmployeeId,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    ZonedDateTime closeDate,
    String status,
    int totalProducts,
    int totalWeight,
    int totalVolume
) implements Serializable {
}
