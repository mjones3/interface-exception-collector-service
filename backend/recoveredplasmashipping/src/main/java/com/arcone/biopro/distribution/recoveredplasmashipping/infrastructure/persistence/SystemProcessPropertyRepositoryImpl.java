package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.SystemProcessProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.SystemProcessPropertyEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@Slf4j
@RequiredArgsConstructor
public class SystemProcessPropertyRepositoryImpl implements SystemProcessPropertyRepository {
    private final SystemProcessPropertyEntityRepository systemProcessPropertyEntityRepository;
    private final SystemProcessPropertyEntityMapper systemProcessPropertyEntityMapper;

    @Override
    public Flux<SystemProcessProperty> findAllByType(String type) {
        return systemProcessPropertyEntityRepository.findAllBySystemProcessType(type)
            .map(systemProcessPropertyEntityMapper::mapToModel);
    }
}
