package com.arcone.biopro.distribution.irradiation.adapter.in.listener.created;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductCreatedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductCreatedMessageMapper extends MessageMapper<ProductCreatedInput, ProductCreatedMessage> {

    @Mapping(target = "weight", source = "weight.value")
    @Mapping(target = "collectionDate", source = "drawTime")
    @Mapping(target = "inventoryLocation", source = "manufacturingLocation")
    @Mapping(target = "collectionLocation", source = "collectionLocation")
    @Mapping(target = "collectionTimeZone", source = "collectionTimeZone")
    @Mapping(target = "licensed", ignore = true)
    @Mapping(target = "quarantines", ignore = true)
    @Mapping(target = "temperatureCategory", ignore = true)
    ProductCreatedInput toInput(ProductCreatedMessage message);
}
