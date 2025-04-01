package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Lookup;
import reactor.core.publisher.Flux;

public interface LookupRepository {

    Flux<Lookup> findAllByType(String type);

}
