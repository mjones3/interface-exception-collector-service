package com.arcone.biopro.distribution.eventbridge.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record InventoryUpdatedPayload(
    ZonedDateTime occurredOn,
    String updateType,
    String unitNumber,
    String productCode,
    String productDescription,
    String productFamily,
    String bloodType,
    LocalDate expirationDate,
    String locationCode,
    String storageLocation,
    List<String> inventoryStatus,
    Map<String, Object> properties

) implements Serializable {

}
