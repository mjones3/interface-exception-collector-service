package com.arcone.biopro.distribution.receiving.application.mapper;

import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.domain.model.ShippingInformation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",uses = {LookupOutputMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShippingInformationOutputMapper {
    ShippingInformationOutput mapToOutput(ShippingInformation shippingInformation);
}
