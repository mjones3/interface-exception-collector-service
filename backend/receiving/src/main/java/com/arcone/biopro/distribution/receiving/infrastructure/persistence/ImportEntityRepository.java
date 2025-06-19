package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ImportEntityRepository extends ReactiveCrudRepository<ImportEntity, Long>{

    Mono<ImportEntity> findByIdAndDeleteDateIsNull(Long id);
}
