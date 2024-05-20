package com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderFulFilledItemResponseDTO(
    Long id,

    Long orderId,
    String productFamily,

    BloodType bloodType,
    Integer quantity,
    String comments
) implements Serializable {}
