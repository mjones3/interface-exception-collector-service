package com.arcone.biopro.distribution.irradiation.infrastructure.mapper;

import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.distribution.irradiation.infrastructure.persistence.entity.ConfigurationEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConfigurationEntityMapper {

    @Mapping(target = "key.value", source = "key")
    Configuration toDomain(ConfigurationEntity configurationEntity);

}


