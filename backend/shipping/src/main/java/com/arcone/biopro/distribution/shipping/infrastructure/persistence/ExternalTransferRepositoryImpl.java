package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.repository.ExternalTransferRepository;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ExternalTransferItemEntityMapper;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ExternalTransferMapper;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ProductLocationHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ExternalTransferRepositoryImpl implements ExternalTransferRepository {

    private final ExternalTransferEntityRepository externalTransferEntityRepository;
    private final ExternalTransferMapper externalTransferMapper;
    private final ExternalTransferItemEntityMapper externalTransferItemEntityMapper;
    private final ExternalTransferItemEntityRepository externalTransferItemEntityRepository;
    private final ProductLocationHistoryMapper productLocationHistoryMapper;
    private final ProductLocationHistoryEntityRepository productLocationHistoryEntityRepository;

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
                    .zipWith(Flux.fromIterable(Optional.ofNullable(externalTransfer.getProductLocationHistories()).orElse(Collections.emptyList()))
                        .flatMap(productLocationHistory -> {
                            return productLocationHistoryEntityRepository.save(productLocationHistoryMapper.toEntity(productLocationHistory));
                        }).collectList()
                    )
                    .map(tuple -> {
                        return externalTransferMapper.toDomain(externalTransferEntity, tuple.getT1());
                    })
            );
    }

    @Override
    public Mono<ExternalTransfer> findOneById(Long id) {
        return externalTransferEntityRepository.findById(id)
            .flatMap(externalTransferEntity -> {
                return externalTransferItemEntityRepository.findAllByExternalTransferId(externalTransferEntity.getId())
                    .collectList()
                    .map(externalTransferItemEntities -> externalTransferMapper.toDomain(externalTransferEntity, externalTransferItemEntities));
            });
    }

    @Override
    public Mono<Void> deleteOneById(Long id) {
        return externalTransferItemEntityRepository.findAllByExternalTransferId(id)
            .collectList()
            .flatMap(externalTransferItemEntityRepository::deleteAll)
            .then(externalTransferEntityRepository.deleteById(id));
    }
}
