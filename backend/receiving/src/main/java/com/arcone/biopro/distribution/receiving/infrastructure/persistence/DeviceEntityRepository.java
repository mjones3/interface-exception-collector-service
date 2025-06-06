package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DeviceEntityRepository extends ReactiveCrudRepository<DeviceEntity, Long> {

    Mono<DeviceEntity> findFirstByBloodCenterId(String bloodCenterId);

    Mono<DeviceEntity> findFirstByBloodCenterIdAndActiveIsTrue(String bloodCenterId);

    Mono<DeviceEntity> findFirstByBloodCenterIdAndTypeAndActiveIsTrue(String bloodCenterId, String type);

}
