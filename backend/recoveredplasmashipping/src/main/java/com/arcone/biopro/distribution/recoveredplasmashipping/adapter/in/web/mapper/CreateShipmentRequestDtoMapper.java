package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CreateShipmentRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CreateShipmentInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateShipmentRequestDtoMapper {
    CreateShipmentInput toInput(CreateShipmentRequestDTO createShipmentRequestDTO);
}
