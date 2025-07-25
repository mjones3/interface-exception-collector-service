package com.arcone.biopro.distribution.irradiation.application.mapper;

import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IrradiationInventoryMapper {
    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "location", source = "location.value")
    @Mapping(target = "alreadyIrradiated", ignore = true)
    @Mapping(target = "notConfigurableForIrradiation", ignore = true)
    @Mapping(target = "isBeingIrradiated", ignore = true)
    IrradiationInventoryOutput toDomain(Inventory inventory);
}

