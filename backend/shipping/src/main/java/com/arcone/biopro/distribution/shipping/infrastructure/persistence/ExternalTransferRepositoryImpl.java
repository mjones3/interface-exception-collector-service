package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ExternalTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ExternalTransferRepositoryImpl implements ExternalTransferRepository {

    private final ExternalTransferEntityRepository externalTransferEntityRepository;
    private final ExternalTransferMapper externalTransferMapper;

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
            .map(externalTransferMapper::toDomain);
    }
}
