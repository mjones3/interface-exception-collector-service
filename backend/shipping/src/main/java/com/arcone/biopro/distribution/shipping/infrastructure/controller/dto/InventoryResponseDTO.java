package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record InventoryResponseDTO(
    Long id,
    String locationCode,
    String unitNumber,
    String productCode,
    String productDescription,
    ZonedDateTime expirationDate,
    String aboRh,
    String productFamily,
    ZonedDateTime collectionDate,
    String storageLocation,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate

) implements Serializable {
}
