package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import com.arcone.biopro.distribution.irradiation.application.usecase.ProductStoredUseCase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductStoredMapper extends MessageMapper<ProductStoredUseCase.Input, ProductStored> {

    @Override
    ProductStoredUseCase.Input toInput(ProductStored payload);
}
