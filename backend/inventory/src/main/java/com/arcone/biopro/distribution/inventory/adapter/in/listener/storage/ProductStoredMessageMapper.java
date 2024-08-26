package com.arcone.biopro.distribution.inventory.adapter.in.listener.storage;

import com.arcone.biopro.distribution.inventory.application.dto.ProductStorageInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductStoredMessageMapper {

    ProductStorageInput toInput(ProductStoredMessage message);
}
