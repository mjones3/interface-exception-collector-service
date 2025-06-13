package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.Lookup;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.LookupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LookupEntityMapper {

    default Lookup mapToDomain(final LookupEntity entity) {
        return Lookup.fromRepository(entity.getId(), entity.getType(), entity.getOptionValue(), entity.getDescriptionKey(), entity.getOrderNumber(), entity.isActive());
    }

}
