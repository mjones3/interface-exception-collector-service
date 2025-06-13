package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentDtoMapper {
    RecoveredPlasmaShipmentResponseDTO toDto(RecoveredPlasmaShipmentOutput recoveredPlasmaShipmentOutput);
}
