package com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.ProductDiscardedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductDiscardedMessageMapper extends MessageMapper<ProductDiscardedInput, ProductDiscarded> {

    @Mapping(target = "reason", source = "reasonDescriptionKey")
    ProductDiscardedInput toInput(ProductDiscarded message);
}
