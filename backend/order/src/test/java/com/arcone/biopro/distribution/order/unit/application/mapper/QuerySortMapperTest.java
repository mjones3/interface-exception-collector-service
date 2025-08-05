package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.QueryOrderByDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.order.application.mapper.QuerySortMapper;
import com.arcone.biopro.distribution.order.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = { QuerySortMapper.class })
class QuerySortMapperTest {

    @Autowired
    QuerySortMapper querySortMapper;

    @Test
    void shouldMapToDTO() {
        var querySort = new QuerySort(singletonList(new QueryOrderBy("PROPERTY", "ASC")));

        var querySortDTO = querySortMapper.mapToDTO(querySort);
        assertEquals(querySort.getQueryOrderByList().size(), querySortDTO.orderByList().size());
        assertEquals(querySort.getQueryOrderByList().getFirst().getProperty(), querySortDTO.orderByList().getFirst().property());
        assertEquals(querySort.getQueryOrderByList().getFirst().getDirection(), querySortDTO.orderByList().getFirst().direction());
    }

    @Test
    void shouldMapToDomain() {
        var querySortDTO = new QuerySortDTO(singletonList(new QueryOrderByDTO("PROPERTY", "DESC")));

        var querySort = querySortMapper.mapToDomain(querySortDTO);
        assertEquals(querySortDTO.orderByList().size(), querySort.getQueryOrderByList().size());
        assertEquals(querySortDTO.orderByList().getFirst().property(), querySort.getQueryOrderByList().getFirst().getProperty());
        assertEquals(querySortDTO.orderByList().getFirst().direction(), querySort.getQueryOrderByList().getFirst().getDirection());
    }

}
