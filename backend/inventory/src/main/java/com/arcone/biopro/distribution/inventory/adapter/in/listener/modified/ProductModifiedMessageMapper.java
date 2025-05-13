package com.arcone.biopro.distribution.inventory.adapter.in.listener.modified;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.ProductModifiedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductModifiedMessageMapper extends MessageMapper<ProductModifiedInput, ProductModifiedMessage> {

    @Mapping(target = "volume", source = "volume.value")
    @Mapping(target = "weight", source = "weight.value")
    ProductModifiedInput toInput(ProductModifiedMessage message);
}
