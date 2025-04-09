package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CartonItemRepositoryImpl implements CartonItemRepository {

    private final CartonItemEntityRepository cartonItemEntityRepository;

    @Override
    public Mono<Integer> countByProduct(String unitNumber, String productCode) {
        return cartonItemEntityRepository.countByUnitNumberAndProductCode(unitNumber,productCode);
    }
}
