package com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.storage.ProductStoredMessage;
import com.arcone.biopro.distribution.inventory.application.dto.ProductDiscardedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductStorageInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductDiscardedMessageMapper {

    @Mapping(target = "reason", source = "reasonDescriptionKey")
    ProductDiscardedInput toInput(ProductDiscardedMessage message);
}
