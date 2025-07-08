package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.LocationOutput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LocationService {

   Flux<LocationOutput> findAll();

    Mono<LocationOutput> findById(Long id);

    Mono<LocationOutput> findByCode(String code);
}
