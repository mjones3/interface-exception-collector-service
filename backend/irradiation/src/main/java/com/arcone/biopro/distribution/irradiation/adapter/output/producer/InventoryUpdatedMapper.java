package com.arcone.biopro.distribution.irradiation.adapter.output.producer;

import com.arcone.biopro.distribution.irradiation.adapter.output.producer.event.InventoryUpdated;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryUpdateType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.*;

import static com.arcone.biopro.distribution.irradiation.BioProConstants.*;


@Mapper(componentModel = "spring")
public interface InventoryUpdatedMapper {
    @Mapping(target = "unitNumber", source = "irradiation.unitNumber.value")
    @Mapping(target = "productCode", source = "irradiation.productCode.value")
    @Mapping(target = "productDescription", source = "irradiation.shortDescription")
    @Mapping(target = "productFamily", source = "irradiation.productFamily")
    @Mapping(target = "bloodType", source = "irradiation.aboRh")
    @Mapping(target = "expirationDate", source = "irradiation.expirationDate")
    @Mapping(target = "locationCode", source = "irradiation.inventoryLocation")
    @Mapping(
        target = "storageLocation",
        expression = "java(getStorageLocation(irradiation))"
    )
    @Mapping(
        target = "inventoryStatus",
        expression = "java(getInventoryStatus(irradiation))"
    )
    @Mapping(target = "updateType", source = "updateType")
    @Mapping(target = "properties", expression = "java(getInventoryProperties(irradiation))")
    @Mapping(target = "inputProducts", expression = "java(getInputProducts())")
    InventoryUpdated toEvent(Inventory inventory, InventoryUpdateType updateType);

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
        var statusList = new ArrayList<String>();
        statusList.add(inventory.getInventoryStatus().name());
        if (inventory.getIsLabeled()) {
            statusList.add(LABELED);
        }
        if (inventory.isQuarantined()) {
            statusList.add(QUARANTINED);
        }
        return statusList;
    }

    default Map<String, Object> getInventoryProperties(Inventory inventory) {
        var properties = new LinkedHashMap<String, Object>();
        properties.put(LICENSURE, (inventory != null && Boolean.TRUE.equals(inventory.getIsLicensed())) ? "LICENSED" : "UNLICENSED");
        return properties;
    }

    default List<Object> getInputProducts() {return List.of();}

}
