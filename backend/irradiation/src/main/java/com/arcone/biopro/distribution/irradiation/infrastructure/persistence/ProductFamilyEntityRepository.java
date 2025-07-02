package com.arcone.biopro.distribution.irradiation.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductFamilyEntityRepository extends ReactiveCrudRepository<ProductFamilyEntity, UUID> {
    Mono<ProductFamilyEntity> findByProductFamily(String productFamily);
}
