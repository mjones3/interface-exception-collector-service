package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.SystemProcessProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.SystemProcessPropertyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SystemProcessPropertyEntityMapper {

    SystemProcessPropertyEntity mapToEntity(final SystemProcessProperty systemProcessProperty);
    SystemProcessProperty mapToModel(final SystemProcessPropertyEntity systemProcessProperty);

}
