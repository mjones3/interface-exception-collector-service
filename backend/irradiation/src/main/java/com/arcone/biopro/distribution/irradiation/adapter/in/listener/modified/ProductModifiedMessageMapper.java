package com.arcone.biopro.distribution.irradiation.adapter.in.listener.modified;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductModifiedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductModifiedMessageMapper extends MessageMapper<ProductModifiedInput, ProductModified> {

    @Mapping(target = "volume", source = "volume.value")
    @Mapping(target = "weight", source = "weight.value")
    @Mapping(target = "shortDescription", source = "productDescription")
    ProductModifiedInput toInput(ProductModified message);
}
