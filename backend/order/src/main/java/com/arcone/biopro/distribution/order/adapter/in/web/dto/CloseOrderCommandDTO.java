package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record CloseOrderCommandDTO (
    Long orderId,
    String employeeId,
    String reason,
    String comments
) implements Serializable  {

}
