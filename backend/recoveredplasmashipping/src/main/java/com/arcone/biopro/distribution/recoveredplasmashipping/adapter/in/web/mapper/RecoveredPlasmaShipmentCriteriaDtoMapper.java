package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.ProductTypeDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ProductTypeOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentCriteriaDtoMapper {
    ProductTypeDTO toDto(ProductTypeOutput output);
}
