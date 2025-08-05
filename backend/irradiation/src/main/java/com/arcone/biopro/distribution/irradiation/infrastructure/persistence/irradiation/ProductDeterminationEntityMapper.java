package com.arcone.biopro.distribution.irradiation.infrastructure.persistence.irradiation;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.ProductDetermination;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = ProductCode.class)
interface ProductDeterminationEntityMapper {

    @Mapping(target = "sourceProductCode", expression = "java(ProductCode.of(entity.getSourceProductCode()))")
    @Mapping(target = "targetProductCode", expression = "java(ProductCode.of(entity.getTargetProductCode()))")
    ProductDetermination toDomain(ProductDeterminationEntity entity);

    @Mapping(target = "sourceProductCode", expression = "java(domain.getSourceProductCode().value())")
    @Mapping(target = "targetProductCode", expression = "java(domain.getTargetProductCode().value())")
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    ProductDeterminationEntity toEntity(ProductDetermination domain);
}
