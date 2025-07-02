package com.arcone.biopro.distribution.irradiation.adapter.in.listener.recovered;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.PackedProductInput;
import com.arcone.biopro.distribution.irradiation.application.dto.RecoveredPlasmaCartonPackedInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaCartonPackedMessageMapper extends MessageMapper<RecoveredPlasmaCartonPackedInput, RecoveredPlasmaCartonPacked> {

    @Override
    @Mapping(target = "packedProducts", source = "packedProducts")
    RecoveredPlasmaCartonPackedInput toInput(RecoveredPlasmaCartonPacked message);

    @Mapping(target = "unitNumber", source = "unitNumber")
    @Mapping(target = "productCode", source = "productCode")
    @Mapping(target = "status", source = "status")
    PackedProductInput toPackedProduct(PackedProductMessage packedProduct);

    List<PackedProductInput> toPackedProducts(List<PackedProductMessage> packedProducts);
}
