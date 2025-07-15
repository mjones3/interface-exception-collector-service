package com.arcone.biopro.distribution.irradiation.application.irradiation.mapper;

import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Batch domain entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface BatchMapper {

    @Mapping(source = "id.value", target = "batchId")
    @Mapping(target = "message", constant = "Batch submitted successfully")
    @Mapping(target = "success", constant = "true")
    BatchSubmissionResultDTO toSubmissionResult(Batch batch);
}
