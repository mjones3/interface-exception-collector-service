package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ProductTypeOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentCriteriaOutputMapper {
   ProductTypeOutput toOutput(ProductType model);
}
