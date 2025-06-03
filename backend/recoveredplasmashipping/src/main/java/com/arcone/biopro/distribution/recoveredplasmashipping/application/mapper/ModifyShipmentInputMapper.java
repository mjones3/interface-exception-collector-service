package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ModifyShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ModifyShipmentCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModifyShipmentInputMapper {
    ModifyShipmentCommand toModifyCommand(ModifyShipmentCommandInput modifyShipmentCommandInput);
}

