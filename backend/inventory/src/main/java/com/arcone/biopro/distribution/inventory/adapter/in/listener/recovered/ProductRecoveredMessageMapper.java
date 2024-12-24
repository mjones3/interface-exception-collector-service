package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.ProductRecoveredInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductRecoveredMessageMapper extends MessageMapper<ProductRecoveredInput, ProductRecoveredMessage> {

    ProductRecoveredInput toInput(ProductRecoveredMessage message);
}
