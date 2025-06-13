package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ImportEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ImportRepositoryImpl implements ImportRepository {
    private final ImportEntityRepository importEntityRepository;
    private final ImportEntityMapper importEntityMapper;


    @Override
    public Mono<Import> create(Import importModel) {
        return importEntityRepository.save(importEntityMapper.toEntity(importModel))
            .map(importEntityMapper::mapToDomain);
    }

    @Override
    public Mono<Import> findOneById(Long id) {
        return importEntityRepository.findByIdAndDeleteDateIsNull(id)
            .map(importEntityMapper::mapToDomain);
    }
}
