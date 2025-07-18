package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.GetUnlabeledProductsRequestDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.mapper.RequestMapper;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.domain.service.GetUnlabeledProductsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ProductSelectionController {

    private final GetUnlabeledProductsService getUnlabeledProductsService;
    private final RequestMapper requestMapper;

    @QueryMapping("getUnlabeledProducts")
    public Mono<RuleResponseDTO> getUnlabeledProducts(@Argument("getUnlabeledProductsRequest") GetUnlabeledProductsRequestDTO getUnlabeledProductsRequest) {
        log.debug("Request to get unlabeled products {}", getUnlabeledProductsRequest);
        return getUnlabeledProductsService.getUnlabeledProducts(requestMapper.toApplicationRequest(getUnlabeledProductsRequest));
    }
}
