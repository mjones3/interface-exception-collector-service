package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record PickListResponseDTO(
    List<NotificationDTO> notifications,
    PickListDTO data
) implements Serializable {
}
