package com.arcone.biopro.distribution.inventory.adapter.in.listener.created;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductCreatedMessageMapper extends MessageMapper<ProductCreatedInput, ProductCreatedMessage> {

}
