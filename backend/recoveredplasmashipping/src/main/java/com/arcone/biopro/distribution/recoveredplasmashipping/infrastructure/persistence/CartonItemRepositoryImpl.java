package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CartonItemEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CartonItemRepositoryImpl implements CartonItemRepository {

    private final CartonItemEntityRepository cartonItemEntityRepository;
    private final CartonItemEntityMapper cartonItemEntityMapper;

    @Override
    public Mono<Integer> countByProduct(String unitNumber, String productCode) {
        return cartonItemEntityRepository.countByUnitNumberAndProductCode(unitNumber,productCode);
    }

    @Override
    public Mono<CartonItem> save(CartonItem cartonItem) {
        return cartonItemEntityRepository.save(cartonItemEntityMapper.toEntity(cartonItem))
            .map(cartonItemEntityMapper::entityToModel);
    }

    @Override
    public Mono<CartonItem> findByCartonAndProduct(Long cartonId, String unitNumber, String productCode) {
        return cartonItemEntityRepository.findByCartonIdAndProductCodeAndUnitNumber(cartonId,productCode,unitNumber)
            .map(cartonItemEntityMapper::entityToModel);
    }
    @Override
    public Mono<Void> deleteAllByCartonId(Long cartonId) {
        return cartonItemEntityRepository.deleteAllByCartonId(cartonId);
    }
}
