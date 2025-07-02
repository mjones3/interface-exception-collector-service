package com.arcone.biopro.distribution.irradiation.adapter.in.listener.shipment;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.ShipmentCompletedInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentCompletedMessageMapper extends MessageMapper<ShipmentCompletedInput, ShipmentCompleted> {

    ShipmentCompletedInput toInput(ShipmentCompleted message);

}
