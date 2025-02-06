package com.arcone.biopro.distribution.inventory.adapter.output.producer;

import com.arcone.biopro.distribution.inventory.adapter.output.producer.event.InventoryUpdatedEvent;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface InventoryUpdatedMapper {
    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "productCode", source = "productCode.value")
    @Mapping(target = "productDescription", source = "shortDescription")
    @Mapping(target = "productFamily", source = "productFamily")
    @Mapping(target = "bloodType", source = "aboRh")
    @Mapping(target = "expirationDate", source = "expirationDate")
    @Mapping(target = "locationCode", source = "location")
    @Mapping(
        target = "storageLocation",
        expression = "java(getStorageLocation(inventory))"
    )
    @Mapping(
        target = "inventoryStatus",
        expression = "java(getInventoryStatus(inventory))"
    )
    InventoryUpdatedEvent toEvent(Inventory inventory);

    default String getStorageLocation(Inventory inventory) {
        return Optional.ofNullable(inventory)
            .map(i -> i.getDeviceStored().toUpperCase() + "|" + i.getStorageLocation().toUpperCase())
            .orElse("");
    }

    default List<String> getInventoryStatus(Inventory inventory) {
        return List.of(inventory.getInventoryStatus().name());
    }

}
