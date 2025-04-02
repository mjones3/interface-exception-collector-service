package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.QuerySort;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record PageOutput<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalRecords,
        QuerySort querySort
) implements Serializable {
}
