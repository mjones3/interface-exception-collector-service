package com.arcone.biopro.distribution.irradiation.adapter.in.listener.quarantine;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.MessageMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.AddQuarantineInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddQuarantinedMessageMapper extends MessageMapper<AddQuarantineInput, ProductQuarantined> {

    @Mapping(target = "product.unitNumber", source = "unitNumber")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "quarantineId", source = "id")
    AddQuarantineInput toInput(ProductQuarantined productMessage);

}
