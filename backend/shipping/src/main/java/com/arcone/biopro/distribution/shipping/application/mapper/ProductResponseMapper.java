package com.arcone.biopro.distribution.shipping.application.mapper;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ProductResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductResponseMapper {

    ProductResponseDTO toResponseDTO(InventoryResponseDTO inventoryResponseDTO);
    List<ProductResponseDTO> toResponseDTO(List<InventoryResponseDTO> inventoryResponseDTOList);
}
