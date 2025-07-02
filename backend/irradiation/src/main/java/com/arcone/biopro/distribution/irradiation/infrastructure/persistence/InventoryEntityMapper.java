package com.arcone.biopro.distribution.irradiation.infrastructure.persistence;

import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryEntityMapper {

    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "productCode", source = "productCode.value")
    InventoryEntity toEntity(Inventory domain);

    @Mapping(target = "unitNumber.value", source = "unitNumber")
    @Mapping(target = "productCode.value", source = "productCode")
    Inventory toDomain(InventoryEntity inventoryEntity);

    @Mapping(target = "irradiation", source = "inventoryEntity")
    @Mapping(target = "notificationMessages", ignore = true)
    @Mapping(target = "properties", ignore = true)
    InventoryAggregate toAggregate(InventoryEntity inventoryEntity);

    List<InventoryAggregate> toAggregate(List<InventoryEntity> inventoryEntity);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "key", source = "property.key")
    @Mapping(target = "value", source = "property.value")
    @Mapping(target = "inventoryId", source = "inventoryEntity.id")
    PropertyEntity toEntity(Property property, InventoryEntity inventoryEntity);

    Property toDomain(PropertyEntity propertyEntity);
}
