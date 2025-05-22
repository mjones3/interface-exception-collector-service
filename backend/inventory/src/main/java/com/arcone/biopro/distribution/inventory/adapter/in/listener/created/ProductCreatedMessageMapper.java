package com.arcone.biopro.distribution.inventory.adapter.in.listener.created;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductCreatedMessageMapper extends MessageMapper<ProductCreatedInput, ProductCreatedMessage> {

    @Mapping(target = "weight", source = "weight.value")
    @Mapping(target = "collectionDate", source = "drawTime")
    @Mapping(target = "inventoryLocation", source = "manufacturingLocation")
    @Mapping(target = "collectionLocation", source = "collectionLocation")
    @Mapping(target = "collectionTimeZone", source = "collectionTimeZone")
    ProductCreatedInput toInput(ProductCreatedMessage message);
}
