package com.arcone.biopro.distribution.order.domain.repository;

import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LookupRepository {

    Mono<Boolean> existsById(LookupId key);

    Mono<Boolean> existsById(LookupId key, Boolean active);

    Mono<Lookup> findOneById(LookupId id);

    Flux<Lookup> findAllByType(String type);

    Mono<Lookup> insert(Lookup lookup);

    Mono<Lookup> update(Lookup lookup);

}
