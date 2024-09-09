package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record OrderResponseDTO (
    List<NotificationDTO> notifications,
    OrderDTO data
) implements Serializable {

}
