package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
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
    String productDescription,
    LocalDateTime expirationDate,
    AboRhType aboRh,
    ProductFamily productFamily,
    String collectionDate,
    String storageLocation,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate

) implements Serializable {
}
