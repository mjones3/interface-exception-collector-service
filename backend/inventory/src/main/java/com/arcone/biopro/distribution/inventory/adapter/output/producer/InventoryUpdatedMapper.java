package com.arcone.biopro.distribution.inventory.adapter.output.producer;

import com.arcone.biopro.distribution.inventory.adapter.output.producer.event.InventoryUpdatedEvent;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryUpdateType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface InventoryUpdatedMapper {
    @Mapping(target = "unitNumber", source = "inventory.unitNumber.value")
    @Mapping(target = "productCode", source = "inventory.productCode.value")
    @Mapping(target = "productDescription", source = "inventory.shortDescription")
    @Mapping(target = "productFamily", source = "inventory.productFamily")
    @Mapping(target = "bloodType", source = "inventory.aboRh")
    @Mapping(target = "expirationDate", source = "inventory.expirationDate")
    @Mapping(target = "locationCode", source = "inventory.location")
    @Mapping(
        target = "storageLocation",
        expression = "java(getStorageLocation(inventory))"
    )
    @Mapping(
        target = "inventoryStatus",
        expression = "java(getInventoryStatus(inventory))"
    )
@Mapping(target = "updateType", source = "updateType")
    InventoryUpdatedEvent toEvent(Inventory inventory, InventoryUpdateType updateType);

    default String getStorageLocation(Inventory inventory) {
        return Optional.ofNullable(inventory)
            .map(i -> Optional.ofNullable(i.getDeviceStored())
                .map(dev -> dev.toUpperCase() + "|")
                .orElse("")
                + Optional.ofNullable(i.getStorageLocation())
                .map(String::toUpperCase)
                .orElse(""))
            .orElse("");
    }


    default List<String> getInventoryStatus(Inventory inventory) {
        return List.of(inventory.getInventoryStatus().name());
    }

}
