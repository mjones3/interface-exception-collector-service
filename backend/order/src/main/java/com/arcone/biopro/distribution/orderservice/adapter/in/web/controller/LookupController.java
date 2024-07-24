package com.arcone.biopro.distribution.orderservice.adapter.in.web.controller;

import com.arcone.biopro.distribution.orderservice.adapter.in.web.dto.LookupDTO;
import com.arcone.biopro.distribution.orderservice.application.mapper.LookupMapper;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class LookupController {

    private final LookupService lookupService;
    private final LookupMapper lookupMapper;

    @QueryMapping
    public Flux<LookupDTO> findAllLookupsByType(@Argument("type") String type) {
        return lookupService.findAllByType(type)
            .map(lookupMapper::mapToDTO)
            .flatMap(Mono::just);
    }

    @MutationMapping
    public Mono<LookupDTO> insertLookup(@Argument("lookupRequest") LookupDTO lookupRequest) {
        return lookupService.insert(lookupMapper.mapToDomain(lookupRequest))
            .map(lookupMapper::mapToDTO)
            .flatMap(Mono::just);
    }

    @MutationMapping
    public Mono<LookupDTO> updateLookup(@Argument("lookupRequest") LookupDTO lookupRequest) {
        return lookupService.update(lookupMapper.mapToDomain(lookupRequest))
            .map(lookupMapper::mapToDTO)
            .flatMap(Mono::just);
    }

    @MutationMapping
    public Mono<LookupDTO> deleteLookup(@Argument("lookupRequest") LookupDTO lookupRequest) {
        return lookupService.delete(lookupMapper.mapToDomain(lookupRequest))
            .map(lookupMapper::mapToDTO)
            .flatMap(Mono::just);
    }

}
