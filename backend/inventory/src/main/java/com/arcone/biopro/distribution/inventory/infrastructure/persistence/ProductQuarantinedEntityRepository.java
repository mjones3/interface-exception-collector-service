package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@GraphQlRepository
public interface ProductQuarantinedEntityRepository extends ReactiveCrudRepository<ProductQuarantinedEntity, UUID> {

    Flux<ProductQuarantinedEntity> findAllByProductId(UUID productId);
}
