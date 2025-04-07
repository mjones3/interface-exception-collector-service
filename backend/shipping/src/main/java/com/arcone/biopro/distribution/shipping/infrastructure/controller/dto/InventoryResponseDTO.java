package com.arcone.biopro.distribution.shipping.infrastructure.controller.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record InventoryResponseDTO(
    UUID id,
    String locationCode,
    String unitNumber,
    String productCode,
    String temperatureCategory,
    String productDescription,
    LocalDateTime expirationDate,
    String aboRh,
    String productFamily,
    ZonedDateTime collectionDate,
    String storageLocation,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate

) implements Serializable {
}
