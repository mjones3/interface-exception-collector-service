package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferReceiptEntityRepository extends ReactiveCrudRepository<TransferReceiptEntity, Long> {

}
