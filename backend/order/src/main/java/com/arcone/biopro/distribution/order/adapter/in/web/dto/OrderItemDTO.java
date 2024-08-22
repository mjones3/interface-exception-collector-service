package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record OrderItemDTO(
    Long id,
    Long orderId,
    String productFamily,
    String bloodType,
    Integer quantity,
    String comments,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate,
    Integer quantityAvailable
) {}
