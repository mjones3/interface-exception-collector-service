package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Builder
public record CartonItemDTO(
    Long id,
    Long cartonId,
    String unitNumber,
    String productCode,
    String productDescription,
    String productType,
    Integer volume,
    Integer weight,
    String packedByEmployeeId,
    String aboRh,
    String status,
    LocalDateTime expirationDate,
    ZonedDateTime collectionDate,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate
) implements Serializable {
}
