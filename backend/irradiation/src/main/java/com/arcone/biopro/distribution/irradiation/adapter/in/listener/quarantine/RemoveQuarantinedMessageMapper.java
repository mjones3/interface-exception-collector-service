package com.arcone.biopro.distribution.irradiation.adapter.in.listener.quarantine;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.RemoveQuarantineInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemoveQuarantinedMessageMapper extends MessageMapper<RemoveQuarantineInput, QuarantineRemoved> {

    @Mapping(target = "product.unitNumber", source = "unitNumber")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "quarantineId", source = "id")
    RemoveQuarantineInput toInput(QuarantineRemoved productMessage);
}
