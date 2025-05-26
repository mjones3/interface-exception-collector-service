package com.arcone.biopro.distribution.receiving.adapter.in.web.controller;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.LookupDTOMapper;
import com.arcone.biopro.distribution.receiving.domain.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;
    private final LookupDTOMapper lookupDTOMapper;

    @QueryMapping("findAllLookupsByType")
    public Flux<LookupDTO> findAllLookupsByType(@Argument("type") String type) {
        return lookupService.findAllByType(type)
            .map(lookupDTOMapper::mapToDTO)
            .flatMap(Mono::just);
    }

}
