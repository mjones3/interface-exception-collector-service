package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.CartonInput;
import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaShipmentClosedInput;
import com.arcone.biopro.distribution.inventory.application.dto.ShipmentPackedProductInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentClosedMessageMapper extends MessageMapper<RecoveredPlasmaShipmentClosedInput, RecoveredPlasmaShipmentClosed> {

    @Override
    @Mapping(target = "cartonList", source = "cartonList")
    RecoveredPlasmaShipmentClosedInput toInput(RecoveredPlasmaShipmentClosed message);

    @Mapping(target = "packedProducts", source = "packedProducts")
    CartonInput toCartonInput(CartonMessage carton);

    List<CartonInput> toCartonInputs(List<CartonMessage> cartons);

    ShipmentPackedProductInput toShipmentPackedProductInput(PackedProductMessage packedProduct);

    List<ShipmentPackedProductInput> toShipmentPackedProductInput(List<PackedProductMessage> packedProduct);

}
