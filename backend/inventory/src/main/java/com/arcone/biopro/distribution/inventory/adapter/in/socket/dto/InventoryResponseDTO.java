package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record InventoryResponseDTO(
    UUID id,
    String locationCode,
    String unitNumber,
    String productCode,
    String productDescription,
    String temperatureCategory,
    LocalDateTime expirationDate,
    AboRhType aboRh,
    Integer weight,
    Boolean isLicensed,
    String productFamily,
    ZonedDateTime collectionDate,
    String storageLocation,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    List<Volume> volumes
) implements Serializable {
}
