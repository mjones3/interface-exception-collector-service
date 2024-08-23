package com.arcone.biopro.distribution.inventory.adapter.in.listener.shipment;

import com.arcone.biopro.distribution.inventory.application.dto.ShipmentCompletedInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentCompletedMessageMapper {

    ShipmentCompletedInput toInput(ShipmentCompletedMessage message);

}
