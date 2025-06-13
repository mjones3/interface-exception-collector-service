package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto;

import java.io.Serializable;

public record QueryOrderByDTO (
    String property,
    String direction
) implements Serializable {
}
