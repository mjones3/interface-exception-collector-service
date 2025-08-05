package com.arcone.biopro.distribution.receiving.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.LookupOutput;
import com.arcone.biopro.distribution.receiving.domain.model.Lookup;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LookupOutputMapper {

    LookupOutput mapToOutput(Lookup lookup);

}
