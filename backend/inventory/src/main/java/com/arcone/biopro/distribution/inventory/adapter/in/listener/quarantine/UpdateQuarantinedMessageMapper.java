package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.inventory.application.dto.UpdateQuarantineInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UpdateQuarantinedMessageMapper extends MessageMapper<UpdateQuarantineInput, QuarantineUpdated> {

    @Mapping(target = "product.unitNumber", source = "unitNumber")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "reason", source = "newReason")
    @Mapping(target = "quarantineId", source = "id")
    UpdateQuarantineInput toInput(QuarantineUpdated productMessage);
}
