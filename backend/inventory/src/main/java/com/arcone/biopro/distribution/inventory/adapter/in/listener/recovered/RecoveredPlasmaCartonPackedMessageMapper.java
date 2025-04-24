package com.arcone.biopro.distribution.inventory.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.RecoveredPlasmaCartonPackedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaCartonPackedMessageMapper extends MessageMapper<RecoveredPlasmaCartonPackedInput, RecoveredPlasmaCartonPackedMessage> {

    @Override
    @Mapping(target = "packedProducts", source = "packedProducts")
    RecoveredPlasmaCartonPackedInput toInput(RecoveredPlasmaCartonPackedMessage message);

    @Mapping(target = "unitNumber", source = "unitNumber")
    @Mapping(target = "productCode", source = "productCode")
    @Mapping(target = "status", source = "status")
    RecoveredPlasmaCartonPackedInput.PackedProduct toPackedProduct(RecoveredPlasmaCartonPackedMessage.PackedProduct packedProduct);

    List<RecoveredPlasmaCartonPackedInput.PackedProduct> toPackedProducts(List<RecoveredPlasmaCartonPackedMessage.PackedProduct> packedProducts);
}