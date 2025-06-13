package com.arcone.biopro.distribution.receiving.adapter.in.web.mapper;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ShippingInformationDTO;
import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",uses = {LookupDTOMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShippingInformationDTOMapper {

    ShippingInformationDTO mapToDTO(ShippingInformationOutput shippingInformationOutput);
}
