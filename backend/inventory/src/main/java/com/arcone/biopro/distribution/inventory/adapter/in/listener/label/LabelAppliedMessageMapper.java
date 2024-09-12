package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LabelAppliedMessageMapper {

    @Mapping(target = "shortDescription", source = "productDescription")
    InventoryInput toInput(LabelAppliedMessage message);
}
