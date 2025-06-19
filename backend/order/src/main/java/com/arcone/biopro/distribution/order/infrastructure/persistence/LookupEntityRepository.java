package com.arcone.biopro.distribution.order.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

public interface LookupEntityRepository extends ReactiveCrudRepository<LookupEntity, Long>, ReactiveSortingRepository<LookupEntity, Long> {

    Flux<LookupEntity> findAllByTypeAndActiveIsTrueOrderByOrderNumberAsc(@NotNull String type);

}
