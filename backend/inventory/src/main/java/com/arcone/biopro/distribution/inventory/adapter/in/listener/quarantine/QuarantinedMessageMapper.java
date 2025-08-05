package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import com.arcone.biopro.distribution.inventory.application.dto.AddQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.dto.RemoveQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.dto.UpdateQuarantineInput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuarantinedMessageMapper {

    @Mapping(target = "product.unitNumber", source = "unitNumber")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "quarantineId", source = "id")
    RemoveQuarantineInput toInput(QuarantineRemoved productMessage);

    @Mapping(target = "product.unitNumber", source = "unitNumber")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "quarantineId", source = "id")
    AddQuarantineInput toInput(ProductQuarantined productMessage);

    @Mapping(target = "product.unitNumber", source = "unitNumber")
    @Mapping(target = "product.productCode", source = "productCode")
    @Mapping(target = "reason", source = "newReason")
    @Mapping(target = "quarantineId", source = "id")
    UpdateQuarantineInput toInput(QuarantineUpdated productMessage);
}
