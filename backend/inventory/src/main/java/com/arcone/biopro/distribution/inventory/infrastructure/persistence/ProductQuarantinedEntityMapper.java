package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.ProductQuarantine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductQuarantinedEntityMapper {

    @Mapping(target = "createDate.value", source = "createDate")
    ProductQuarantine toDomain(ProductQuarantinedEntity productQuarantinedEntity);

    @Mapping(target = "createDate", source = "createDate.value")
    ProductQuarantinedEntity toEntity(ProductQuarantine productQuarantine);

    List<ProductQuarantinedEntity> toEntity(List<ProductQuarantine> productQuarantines);

    List<ProductQuarantine> toDomain(List<ProductQuarantinedEntity> entities);

}


