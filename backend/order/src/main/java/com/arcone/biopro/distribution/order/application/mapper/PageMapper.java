package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.order.domain.model.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class PageMapper {

    private final QuerySortMapper querySortMapper;

    public <DOMAIN, DTO> PageDTO<DTO> mapToDTO(Page<DOMAIN> page, Function<DOMAIN, DTO> contentTransformer) {
        return new PageDTO<>(
            ofNullable(page.getContent())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(contentTransformer)
                .toList(),
            page.getPageNumber(),
            page.getPageSize(),
            page.getTotalRecords(),
            ofNullable(page.getQuerySort())
                .map(querySortMapper::mapToDTO)
                .orElse(null)
        );
    }

    public <DTO, DOMAIN> Page<DOMAIN> mapToDomain(PageDTO<DTO> pageDTO, Function<DTO, DOMAIN> contentTransformer) {
        return new Page<>(
            ofNullable(pageDTO.content())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(contentTransformer)
                .toList(),
            pageDTO.pageNumber(),
            pageDTO.pageSize(),
            pageDTO.totalRecords(),
            ofNullable(pageDTO.querySort())
                .map(querySortMapper::mapToDomain)
                .orElse(null)
        );
    }

}
