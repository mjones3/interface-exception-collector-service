package com.arcone.biopro.distribution.receiving.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ValidationResultOutputMapper {

    ValidationResultOutput toOutput(ValidationResult validationResult);
}
