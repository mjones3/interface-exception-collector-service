package com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConfigurationDTOMapper {

    @Mapping(target = "key", source = "key.value")
    ConfigurationResponseDTO toResponseDTO(Configuration configuration);
}
