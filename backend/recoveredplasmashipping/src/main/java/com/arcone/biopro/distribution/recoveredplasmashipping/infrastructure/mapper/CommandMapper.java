package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ValidateInventoryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommandMapper {

   InventoryValidationRequest toRequest(ValidateInventoryCommand command);
}
