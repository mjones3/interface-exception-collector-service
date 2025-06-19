package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.LocationDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LocationOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDto(LocationOutput locationOutput);
}
