package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalTransferEntityRepository extends ReactiveCrudRepository<ExternalTransferEntity, Long> {

}
