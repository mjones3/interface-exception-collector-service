package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.SearchOrderCriteriaDTO;
import com.arcone.biopro.distribution.order.application.mapper.SearchOrderCriteriaMapper;
import com.arcone.biopro.distribution.order.domain.service.SearchOrderCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class SearchOrderCriteriaController {

    private final SearchOrderCriteriaService searchOrderCriteriaService;
    private final SearchOrderCriteriaMapper searchOrderCriteriaMapper;

    @QueryMapping
    public Mono<SearchOrderCriteriaDTO> searchOrderCriteria(){
        return searchOrderCriteriaService.searchOrderCriteria().map(searchOrderCriteriaMapper::mapToDTO);
    }
}
