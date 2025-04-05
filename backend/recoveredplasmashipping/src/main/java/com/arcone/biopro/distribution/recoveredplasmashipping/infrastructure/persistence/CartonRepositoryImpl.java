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
}
