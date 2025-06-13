package com.arcone.biopro.distribution.receiving.domain.repository;

import com.arcone.biopro.distribution.receiving.domain.model.Lookup;
import reactor.core.publisher.Flux;

public interface LookupRepository {

    Flux<Lookup> findAllByType(String type);

}
