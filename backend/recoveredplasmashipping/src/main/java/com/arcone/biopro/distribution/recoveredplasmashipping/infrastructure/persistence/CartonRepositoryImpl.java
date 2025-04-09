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
        return cartonEntityRepository.findById(id)
            .zipWith(cartonItemEntityRepository.findAllByCartonIdOrderByCreateDateAsc(id).collectList())
            .map(tuple -> cartonEntityMapper.entityToModel(tuple.getT1(),tuple.getT2()));
    }
}
