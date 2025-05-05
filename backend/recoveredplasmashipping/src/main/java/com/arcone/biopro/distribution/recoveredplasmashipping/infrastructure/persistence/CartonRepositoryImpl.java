package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CartonEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CartonRepositoryImpl implements CartonRepository {

    private final CartonEntityRepository cartonEntityRepository;
    private final CartonEntityMapper cartonEntityMapper;
    private final CartonItemEntityRepository cartonItemEntityRepository;
    private final RecoveredPlasmaShipmentCriteriaItemEntityRepository recoveredPlasmaShipmentCriteriaItemEntityRepository;
    private static final String MINIMUM_UNITS_BY_CARTON_TYPE = "MINIMUM_UNITS_BY_CARTON";
    private static final String MAXIMUM_UNITS_BY_CARTON_TYPE = "MAXIMUM_UNITS_BY_CARTON";



    @Override
    public Mono<Long> getNextCartonId() {
        return cartonEntityRepository.getNextCartonId();
    }

    @Override
    public Mono<Carton> create(Carton carton) {
        return cartonEntityRepository.save(cartonEntityMapper.toEntity(carton))
            .map(cartonEntityMapper::entityToModel);
    }

    @Override
    public Mono<Integer> countByShipment(Long shipmentId) {
        return cartonEntityRepository.countByShipmentIdAndDeleteDateIsNull(shipmentId);
    }

    @Override
    public Mono<Carton> findOneById(Long id) {

        var cartonEntity = cartonEntityRepository.findById(id);
        var cartonItems = cartonItemEntityRepository.findAllByCartonIdOrderByCreateDateAsc(id).collectList();
        var minNumberOfUnits = recoveredPlasmaShipmentCriteriaItemEntityRepository.findByCartonIdAndType(id,MINIMUM_UNITS_BY_CARTON_TYPE).switchIfEmpty(Mono.just(0));
        var maxNumberOfUnits = recoveredPlasmaShipmentCriteriaItemEntityRepository.findByCartonIdAndType(id,MAXIMUM_UNITS_BY_CARTON_TYPE).switchIfEmpty(Mono.just(0));

        return Mono.zip(cartonEntity,cartonItems,minNumberOfUnits,maxNumberOfUnits)
            .map(tuple -> cartonEntityMapper.entityToModel(tuple.getT1(),tuple.getT2(), tuple.getT3(), tuple.getT4()));

    }

    @Override
    public Mono<Carton> update(Carton carton) {
        return cartonEntityRepository.save(cartonEntityMapper.toEntity(carton))
            .flatMap(cartonEntity -> findOneById(cartonEntity.getId()));
    }
}
