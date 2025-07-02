package com.arcone.biopro.distribution.irradiation.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductRecoveredInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductRecoveredMessageMapper extends MessageMapper<ProductRecoveredInput, ProductRecovered> {

    ProductRecoveredInput toInput(ProductRecovered message);
}
