package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import reactor.core.publisher.Mono;

public interface CartonRepository {
    Mono<Long> getNextCartonId();
    Mono<Carton> create(Carton carton);
    Mono<Integer> countByShipment(Long shipmentId);
    Mono<Carton> findOneById(Long id);
}
