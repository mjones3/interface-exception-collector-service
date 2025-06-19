package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record PickListItemDTO(
    String productFamily,
    String bloodType,
    Integer quantity,
    String comments,
    List<PickListItemShortDateDTO> shortDateList
) implements Serializable {
}
