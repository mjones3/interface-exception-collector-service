package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CompleteOrderCommandDTO(
    Long orderId,
    String employeeId,
    String comments
) implements Serializable  {

}
