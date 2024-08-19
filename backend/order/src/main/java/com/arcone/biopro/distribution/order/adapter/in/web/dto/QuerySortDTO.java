package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import java.io.Serializable;
import java.util.List;

public record QuerySortDTO(
    List<QueryOrderByDTO> orderByList
) implements Serializable {
}
