package com.arcone.biopro.distribution.inventory.application.mapper;

import com.arcone.biopro.distribution.inventory.application.dto.GetAllAvailableInventoriesOutput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryFamily;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.Product;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
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
}
