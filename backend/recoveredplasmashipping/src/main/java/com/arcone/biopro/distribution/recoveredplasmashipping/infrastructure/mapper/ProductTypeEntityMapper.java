package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaProductTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductTypeEntityMapper {
    ProductType toModel(RecoveredPlasmaProductTypeEntity entity);
}



