package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PageOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Page;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentReport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PageOutputMapper {
   PageOutput<RecoveredPlasmaShipmentReportOutput> toPageOutput(Page<RecoveredPlasmaShipmentReport> recoveredPlasmaShipmentReportPage);
}
