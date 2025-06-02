package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.ProductConsequenceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductConsequenceEntityMapper {
    ProductConsequence mapToDomain(ProductConsequenceEntity entity);
}
