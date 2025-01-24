package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.PickListResponseDTO;
import com.arcone.biopro.distribution.order.application.mapper.PickListMapper;
import com.arcone.biopro.distribution.order.domain.service.PickListService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class PickListController {

    private final PickListMapper pickListMapper;
    private final PickListService pickListService;

    @MutationMapping
    public Mono<PickListResponseDTO> generatePickList(@Argument Long orderId , @Argument boolean skipInventoryUnavailable) {
        return pickListService.generatePickList(orderId , skipInventoryUnavailable)
            .map(pickListMapper::mapToDTO);
    }
}
