package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LookupEntityRepository extends ReactiveCrudRepository<LookupEntity, Long> {

    Flux<LookupEntity> findAllByTypeAndActiveIsTrueOrderByOrderNumberAsc(@NotNull String type);

}
