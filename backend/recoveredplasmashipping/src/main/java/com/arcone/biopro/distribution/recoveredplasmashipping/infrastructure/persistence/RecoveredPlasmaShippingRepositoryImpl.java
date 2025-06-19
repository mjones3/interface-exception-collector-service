package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipmentHistory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.ShipmentHistoryEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RecoveredPlasmaShippingRepositoryImpl implements RecoveredPlasmaShippingRepository {

    private final RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShipmentEntityRepository;
    private final RecoveredPlasmaShipmentEntityMapper recoveredPlasmaShipmentEntityMapper;
    private final CartonEntityRepository cartonEntityRepository;
    private final ShipmentHistoryEntityRepository shipmentHistoryEntityRepository;
    private final ShipmentHistoryEntityMapper shipmentHistoryEntityMapper;

    @Override
    public Mono<Long> getNextShipmentId() {
        return recoveredPlasmaShipmentEntityRepository.getNextShippingId();
    }

    @Override
    public Mono<RecoveredPlasmaShipment> create(RecoveredPlasmaShipment recoveredPlasmaShipment) {
        return recoveredPlasmaShipmentEntityRepository.save(recoveredPlasmaShipmentEntityMapper.toEntity(recoveredPlasmaShipment))
                .map(savedRecord -> recoveredPlasmaShipmentEntityMapper.entityToModel(savedRecord,null));
    }

    @Override
    public Mono<RecoveredPlasmaShipment> findOneById(Long id) {
        return recoveredPlasmaShipmentEntityRepository.findById(id)
            .zipWith(cartonEntityRepository.findAllByShipmentIdAndDeleteDateIsNullOrderByCartonSequenceNumberAsc(id).collectList())
            .map(tuple -> recoveredPlasmaShipmentEntityMapper.entityToModel(tuple.getT1(),tuple.getT2()));
    }

    @Override
    public Mono<RecoveredPlasmaShipment> update(RecoveredPlasmaShipment recoveredPlasmaShipment) {
        return recoveredPlasmaShipmentEntityRepository.save(recoveredPlasmaShipmentEntityMapper.toEntity(recoveredPlasmaShipment))
            .map(updated -> recoveredPlasmaShipmentEntityMapper.entityToModel(updated,null));
    }

    @Override
    public Mono<ShipmentHistory> createShipmentHistory(ShipmentHistory shipmentHistory) {
        return shipmentHistoryEntityRepository.save(shipmentHistoryEntityMapper.toEntity(shipmentHistory))
            .map(shipmentHistoryEntityMapper::toModel);
    }

    @Override
    public Flux<ShipmentHistory> findAllByShipmentId(Long shipmentId) {
        return shipmentHistoryEntityRepository.findAllByShipmentIdOrderByCreateDateDesc(shipmentId)
            .map(shipmentHistoryEntityMapper::toModel);
    }
}
