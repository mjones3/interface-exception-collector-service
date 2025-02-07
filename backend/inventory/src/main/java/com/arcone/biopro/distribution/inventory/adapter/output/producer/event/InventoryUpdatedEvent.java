package com.arcone.biopro.distribution.inventory.adapter.output.producer.event;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record InventoryUpdatedEvent(
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
    List<String> inventoryStatus
    // TODO: Input products and properties

) implements Serializable {
}
