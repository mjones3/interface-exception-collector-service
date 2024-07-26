package com.arcone.biopro.distribution.orderservice.adapter.in.web.dto;

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
    ZonedDateTime modificationDate
) {}
