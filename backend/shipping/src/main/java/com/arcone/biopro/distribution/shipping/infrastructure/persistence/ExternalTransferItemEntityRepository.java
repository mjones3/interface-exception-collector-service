package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalTransferItemEntityRepository extends ReactiveCrudRepository<ExternalTransferItemEntity, Long> {

}
