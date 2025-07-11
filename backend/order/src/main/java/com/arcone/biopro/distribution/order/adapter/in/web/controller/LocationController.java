package com.arcone.biopro.distribution.order.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.LocationDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.mapper.LocationMapper;
import com.arcone.biopro.distribution.order.domain.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @QueryMapping("findAllLocations")
    public Flux<LocationDTO> findAll() {
        return locationService.findAll()
            .map(locationMapper::toDto)
            .flatMap(Mono::just);
    }
}
