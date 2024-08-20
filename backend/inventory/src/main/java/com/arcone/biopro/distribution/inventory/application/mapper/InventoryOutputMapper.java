package com.arcone.biopro.distribution.inventory.application.mapper;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ErrorMessage;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryOutputMapper {

    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "productCode", source = "productCode.value")
    InventoryOutput toOutput(Inventory domain);

    @Mapping(target = "productFamily", source = "productFamily")
    @Mapping(target = "aboRh", source = "aboRh")
    @Mapping(target = "quantityAvailable", source = "quantity")
    @Mapping(target = "shortDateProducts", source = "aggregates")
    InventoryFamily toOutput(ProductFamily productFamily, AboRhCriteria aboRh, Long quantity, List<InventoryAggregate> aggregates);

    GetAllAvailableInventoriesOutput toOutput(String location, List<InventoryFamily> inventories);



    @Mapping(target = "unitNumber", source = "inventory.unitNumber.value")
    @Mapping(target = "productCode", source = "inventory.productCode.value")
    @Mapping(target = "storageLocation", source = "inventory.location")
    @Mapping(target = "aboRh", source = "inventory.aboRh")
    Product toOutput(InventoryAggregate inventoryAggregate);

    @Mapping(target = "inventoryOutput", source = "inventory")
    ValidateInventoryOutput toValidateInventoryOutput(InventoryAggregate inventoryAggregate);

    @Mapping(target = "inventoryOutput", ignore = true)
    ValidateInventoryOutput toOutput(ErrorMessage errorMessage);

    @Mapping(target = "inventory.unitNumber.value", source = "unitNumber")
    @Mapping(target = "inventory.productCode.value", source = "productCode")
    @Mapping(target = "inventory.shortDescription", source = "shortDescription")
    @Mapping(target = "inventory.expirationDate", source = "expirationDate")
    @Mapping(target = "inventory.collectionDate", source = "collectionDate")
    @Mapping(target = "inventory.location", source = "location")
    @Mapping(target = "inventory.productFamily", source = "productFamily")
    @Mapping(target = "inventory.aboRh", source = "aboRh")
    @Mapping(target = "inventory.id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "inventory.inventoryStatus", expression = "java(com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus.AVAILABLE)")
    @Mapping(target = "errorMessage", ignore = true)
    InventoryAggregate toAggregate(InventoryInput input);

}
