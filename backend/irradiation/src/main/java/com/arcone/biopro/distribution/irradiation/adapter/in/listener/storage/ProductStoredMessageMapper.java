package com.arcone.biopro.distribution.irradiation.adapter.in.listener.storage;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductStorageInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductStoredMessageMapper extends MessageMapper<ProductStorageInput, ProductStored> {

    ProductStorageInput toInput(ProductStored message);
}
