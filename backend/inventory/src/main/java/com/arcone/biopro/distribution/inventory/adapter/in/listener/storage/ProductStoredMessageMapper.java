package com.arcone.biopro.distribution.inventory.adapter.in.listener.storage;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.ProductStorageInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductStoredMessageMapper extends MessageMapper<ProductStorageInput, ProductStored> {

    ProductStorageInput toInput(ProductStored message);
}
