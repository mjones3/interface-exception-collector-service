package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.ShipmentHistoryDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShipmentHistoryOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentHistoryDtoMapper {

    ShipmentHistoryDTO toDto(ShipmentHistoryOutput shipmentHistoryOutput);
}
