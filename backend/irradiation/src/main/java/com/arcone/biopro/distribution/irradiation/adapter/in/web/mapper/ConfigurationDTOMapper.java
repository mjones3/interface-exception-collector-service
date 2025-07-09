package com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.domain.model.Configuration;
import com.arcone.biopro.manufacturing.labeling.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.manufacturing.labeling.domain.model.vo.Configuration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigurationDTOMapper {

    ConfigurationResponseDTO toResponseDTO(Configuration configuration);
}
