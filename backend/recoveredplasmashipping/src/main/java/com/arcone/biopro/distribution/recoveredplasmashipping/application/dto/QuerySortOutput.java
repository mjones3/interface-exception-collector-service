package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record QuerySortOutput(
    List<QueryOrderByOutput> queryOrderByList

) {
}
