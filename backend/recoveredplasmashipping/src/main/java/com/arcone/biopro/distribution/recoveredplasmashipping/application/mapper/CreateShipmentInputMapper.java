package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateShipmentInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateShipmentCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateShipmentInputMapper {
    CreateShipmentCommand toCreateCommand(CreateShipmentInput createShipmentInput);
}

