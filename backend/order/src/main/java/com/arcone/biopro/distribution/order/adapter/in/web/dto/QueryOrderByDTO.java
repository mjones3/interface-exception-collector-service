package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record QueryOrderByDTO (
    String property,
    String direction

) implements Serializable {

}
