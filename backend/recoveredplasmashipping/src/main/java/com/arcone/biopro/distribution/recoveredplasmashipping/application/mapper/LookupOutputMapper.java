package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LookupOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Lookup;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LookupOutputMapper {

    LookupOutput mapToOutput(Lookup lookup);

}
