package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.FinNumber;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.FinNumberEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FinNumberEntityMapper {

    FinNumber toDomain(FinNumberEntity entity);

    FinNumberEntity toEntity(FinNumber finNumber);
}
