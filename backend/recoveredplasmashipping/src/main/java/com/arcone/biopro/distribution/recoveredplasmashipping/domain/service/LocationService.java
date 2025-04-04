package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LocationOutput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LocationService {

   Flux<LocationOutput> findAll();

    Mono<LocationOutput> findById(Long id);
}
