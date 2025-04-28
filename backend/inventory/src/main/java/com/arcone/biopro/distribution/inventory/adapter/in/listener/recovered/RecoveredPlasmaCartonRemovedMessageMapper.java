package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.PackedProductInput;
import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaCartonRemovedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaCartonRemovedMessageMapper extends MessageMapper<RecoveredPlasmaCartonRemovedInput, RecoveredPlasmaCartonRemovedMessage> {

    @Override
    @Mapping(target = "packedProducts", source = "packedProducts")
    RecoveredPlasmaCartonRemovedInput toInput(RecoveredPlasmaCartonRemovedMessage message);

    @Mapping(target = "unitNumber", source = "unitNumber")
    @Mapping(target = "productCode", source = "productCode")
    @Mapping(target = "status", source = "status")
    PackedProductInput toPackedProduct(PackedProductMessage packedProduct);

    List<PackedProductInput> toPackedProducts(List<PackedProductMessage> packedProducts);
}
