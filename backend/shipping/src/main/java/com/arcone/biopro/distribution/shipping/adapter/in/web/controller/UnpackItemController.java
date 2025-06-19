package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UnpackItemsRequest;
import com.arcone.biopro.distribution.shipping.domain.service.UnpackItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UnpackItemController {

    private final UnpackItemService unpackItemService;

    @MutationMapping("unpackItems")
    public Mono<RuleResponseDTO> unpackItems(@Argument("unpackItemsRequest") UnpackItemsRequest unpackItemsRequest) {
        log.info("Request to unpack products {}", unpackItemsRequest);
        return unpackItemService.unpackItems(unpackItemsRequest);
    }
}
