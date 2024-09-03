package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import com.arcone.biopro.distribution.inventory.application.dto.QuarantineProductInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemoveQuarantinedMessageMapper {

    @Mapping(target = "product.unitNumber", source = "unitNumber")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "quarantineReason", source = "reason")
    QuarantineProductInput toInput(RemoveQuarantinedMessage productMessage);
}
