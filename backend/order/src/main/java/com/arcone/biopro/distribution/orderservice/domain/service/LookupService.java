package com.arcone.biopro.distribution.orderservice.domain.service;

import com.arcone.biopro.distribution.orderservice.domain.model.Lookup;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LookupService {

    Flux<Lookup> findAllByType(String type);

    Mono<Lookup> insert(Lookup lookup);

    Mono<Lookup> update(Lookup lookup);

    Mono<Lookup> delete(Lookup id);

}
