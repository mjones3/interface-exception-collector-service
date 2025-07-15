package com.arcone.biopro.distribution.shipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.GetUnlabeledProductsRequestDTO;
import com.arcone.biopro.distribution.shipping.application.dto.GetUnlabeledProductsRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    GetUnlabeledProductsRequest toApplicationRequest(GetUnlabeledProductsRequestDTO getUnlabeledProductsRequestDTO);
}
