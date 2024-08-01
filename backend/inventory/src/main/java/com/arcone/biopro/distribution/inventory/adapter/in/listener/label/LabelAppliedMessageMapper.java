package com.arcone.biopro.distribution.inventory.adapter.in.listener.label;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelAppliedMessageMapper {

    InventoryInput toInput(LabelAppliedMessage message);
}
