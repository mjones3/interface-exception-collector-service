package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentQueryCommandInputMapper {

    RecoveredPlasmaShipmentQueryCommand toModel(RecoveredPlasmaShipmentQueryCommandInput recoveredPlasmaShipmentQueryCommandInput);

}
