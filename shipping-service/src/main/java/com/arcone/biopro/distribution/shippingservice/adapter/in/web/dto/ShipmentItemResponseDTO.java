package com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record ShipmentItemResponseDTO(
    Long id,

    Long shipmentId,
    String productFamily,

    BloodType bloodType,
    Integer quantity,

    String comments,

    List<ShipmentItemShortDateProductResponseDTO> shortDateProducts
) implements Serializable {}
