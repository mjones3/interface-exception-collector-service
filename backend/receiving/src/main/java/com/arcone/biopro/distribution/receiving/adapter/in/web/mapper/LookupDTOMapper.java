package com.arcone.biopro.distribution.receiving.adapter.in.web.mapper;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.receiving.application.dto.LookupOutput;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LookupDTOMapper {

    LookupDTO mapToDTO(LookupOutput lookupOutput);

}
