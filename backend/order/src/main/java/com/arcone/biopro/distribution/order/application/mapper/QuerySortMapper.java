package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.QueryOrderByDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.order.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static java.util.Optional.ofNullable;

@Component
public class QuerySortMapper {

    public QuerySortDTO mapToDTO(QuerySort querySort) {
        var orderByListDTO = ofNullable(querySort)
            .map(QuerySort::getQueryOrderByList)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(orderBy -> new QueryOrderByDTO(orderBy.getProperty(), orderBy.getDirection()))
            .toList();
        return new QuerySortDTO(orderByListDTO);
    }

    public QuerySort mapToDomain(QuerySortDTO querySortDTO) {
        var orderByList = ofNullable(querySortDTO)
            .map(QuerySortDTO::orderByList)
            .orElseGet(Collections::emptyList)
            .stream()
            .map(orderBy -> new QueryOrderBy(orderBy.property(),orderBy.direction()))
            .toList();
        return new QuerySort(orderByList);
    }

}
