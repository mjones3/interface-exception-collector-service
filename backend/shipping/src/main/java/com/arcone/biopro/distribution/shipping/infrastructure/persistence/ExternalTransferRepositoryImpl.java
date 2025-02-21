package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ExternalTransferItemEntityMapper;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ExternalTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ExternalTransferRepositoryImpl implements ExternalTransferRepository {

    private final ExternalTransferEntityRepository externalTransferEntityRepository;
    private final ExternalTransferMapper externalTransferMapper;
    private final ExternalTransferItemEntityMapper externalTransferItemEntityMapper;
    private final ExternalTransferItemEntityRepository externalTransferItemEntityRepository;

    @Override
    public Mono<ExternalTransfer> create(ExternalTransfer externalTransfer) {
        return externalTransferEntityRepository
            .save(externalTransferMapper.toEntity(externalTransfer))
            .map(externalTransferMapper::toDomain);
    }

    @Override
    public Mono<ExternalTransfer> update(ExternalTransfer externalTransfer) {
        return externalTransferEntityRepository
            .save(externalTransferMapper.toEntity(externalTransfer))
            .flatMap(externalTransferEntity ->
                Flux.fromIterable(externalTransfer.getExternalTransferItems())
                    .map(externalTransferItemEntityMapper::toEntity)
                    .flatMap(externalTransferItemEntityRepository::save)
                    .collect(Collectors.toList())
                    .map(externalTransferItemEntities -> {
                        return externalTransferMapper.toDomain(externalTransferEntity, externalTransferItemEntities);
                    })
            );
    }

    @Override
    public Mono<ExternalTransfer> findOneById(Long id) {
        return externalTransferEntityRepository.findById(id)
            .map(externalTransferMapper::toDomain);
    }
}
