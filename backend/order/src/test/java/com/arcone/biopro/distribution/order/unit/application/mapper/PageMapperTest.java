package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QueryOrderByDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.QuerySortDTO;
import com.arcone.biopro.distribution.order.application.mapper.PageMapper;
import com.arcone.biopro.distribution.order.application.mapper.QuerySortMapper;
import com.arcone.biopro.distribution.order.domain.model.Page;
import com.arcone.biopro.distribution.order.domain.model.QueryOrderBy;
import com.arcone.biopro.distribution.order.domain.model.QuerySort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(classes = { PageMapper.class, QuerySortMapper.class })
class PageMapperTest {

    @Autowired
    PageMapper pageMapper;

    @Test
    void shouldMapToDTO() {
        var page = new Page<>(
            singletonList("CONTENT"),
            5,
            10,
            100,
            new QuerySort(singletonList(new QueryOrderBy("A", "ASC")))
        );

        var pageDTO = pageMapper.mapToDTO(page, value -> "TRANSFORMED " + value); // Adding prefix "TRANSFORMED " to content
        assertEquals(page.getContent().size(), pageDTO.content().size());
        assertEquals("TRANSFORMED " + page.getContent().getFirst(), pageDTO.content().getFirst());
        assertEquals(page.getPageNumber(), pageDTO.pageNumber());
        assertEquals(page.getPageSize(), pageDTO.pageSize());
        assertEquals(page.getTotalRecords(), pageDTO.totalRecords());
        assertEquals(page.getQuerySort().getQueryOrderByList().size(), pageDTO.querySort().orderByList().size());
        assertEquals(page.getQuerySort().getQueryOrderByList().getFirst().getProperty(), pageDTO.querySort().orderByList().getFirst().property());
        assertEquals(page.getQuerySort().getQueryOrderByList().getFirst().getDirection(), pageDTO.querySort().orderByList().getFirst().direction());
    }

    @Test
    void shouldMapToDomain() {
        var pageDTO = new PageDTO<>(
            singletonList("CONTENT"),
            5,
            10,
            100,
            new QuerySortDTO(singletonList(new QueryOrderByDTO("B", "DESC")))
        );

        var page = pageMapper.mapToDomain(pageDTO, value -> "TRANSFORMED " + value); // Adding prefix "TRANSFORMED " to content
        assertEquals(pageDTO.content().size(), page.getContent().size());
        assertEquals("TRANSFORMED " + pageDTO.content().getFirst(), page.getContent().getFirst());
        assertEquals(pageDTO.pageNumber(), page.getPageNumber());
        assertEquals(pageDTO.pageSize(), page.getPageSize());
        assertEquals(pageDTO.totalRecords(), page.getTotalRecords());
        assertEquals(pageDTO.querySort().orderByList().size(), page.getQuerySort().getQueryOrderByList().size());
        assertEquals(pageDTO.querySort().orderByList().getFirst().property(), page.getQuerySort().getQueryOrderByList().getFirst().getProperty());
        assertEquals(pageDTO.querySort().orderByList().getFirst().direction(), page.getQuerySort().getQueryOrderByList().getFirst().getDirection());
    }

}
