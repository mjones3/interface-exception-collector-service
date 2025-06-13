package com.arcone.biopro.distribution.receiving.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.CreateImportCommandInput;
import com.arcone.biopro.distribution.receiving.domain.model.CreateImportCommand;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InputCommandMapper {

    CreateImportCommand toCommand(CreateImportCommandInput commandInput , ProductConsequenceRepository productConsequenceRepository , DeviceRepository deviceRepository);
}
