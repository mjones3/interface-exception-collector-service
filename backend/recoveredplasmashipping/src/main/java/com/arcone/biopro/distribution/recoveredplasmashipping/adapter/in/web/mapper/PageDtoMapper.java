package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PageOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentReportOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PageDtoMapper {
    PageDTO<RecoveredPlasmaShipmentReportDTO> toDto(PageOutput<RecoveredPlasmaShipmentReportOutput> pageOutput);

}
