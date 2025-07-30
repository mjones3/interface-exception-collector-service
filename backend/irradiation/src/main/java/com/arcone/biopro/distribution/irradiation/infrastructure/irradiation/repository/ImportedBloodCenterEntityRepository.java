package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.repository;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.ImportedBloodCenterEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportedBloodCenterEntityRepository extends ReactiveCrudRepository<ImportedBloodCenterEntity, Long> {
}
