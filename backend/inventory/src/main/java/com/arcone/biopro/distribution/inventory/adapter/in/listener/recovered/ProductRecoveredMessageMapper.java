package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.application.dto.ProductRecoveredInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductRecoveredMessageMapper {

    ProductRecoveredInput toInput(ProductRecoveredMessage message);
}
