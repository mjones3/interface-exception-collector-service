package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PackingSlipProductOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.PackingSlipProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PackingSlipProductMapper {
    @Mapping(source = "collectionDateFormatted", target = "collectionDate")
    PackingSlipProductOutput  toOutput(PackingSlipProduct packingSlipProduct);

}
