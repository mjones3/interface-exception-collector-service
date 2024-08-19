package com.arcone.biopro.distribution.order.application.dto;


import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderItemEventDTO(
    String productFamily,
    String bloodType,
    Integer quantity,
    String comments

) implements Serializable {


}
