package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record CartonDTO(
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
    BigDecimal totalWeight,
    BigDecimal totalVolume,
    List<CartonItemDTO> packedProducts,
    Integer maxNumberOfProducts,
    Integer minNumberOfProducts
) implements Serializable {
}
