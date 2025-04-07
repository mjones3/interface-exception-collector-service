package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RecoveredPlasmaShippingRepositoryImpl implements RecoveredPlasmaShippingRepository {

    private final RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShipmentEntityRepository;
    private final RecoveredPlasmaShipmentEntityMapper recoveredPlasmaShipmentEntityMapper;

    @Override
    public Mono<Long> getNextShipmentId() {
        return recoveredPlasmaShipmentEntityRepository.getNextShippingId();
    }

    @Override
    public Mono<RecoveredPlasmaShipment> create(RecoveredPlasmaShipment recoveredPlasmaShipment) {
        return recoveredPlasmaShipmentEntityRepository.save(recoveredPlasmaShipmentEntityMapper.toEntity(recoveredPlasmaShipment))
                .map(recoveredPlasmaShipmentEntityMapper::entityToModel);
    }

    @Override
    public Mono<RecoveredPlasmaShipment> findOneById(Long id) {
        return recoveredPlasmaShipmentEntityRepository.findById(id)
                .map(recoveredPlasmaShipmentEntityMapper::entityToModel);
    }
}
