package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import com.arcone.biopro.distribution.receiving.domain.repository.InternalTransferRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.InternalTransferEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InternalTransferRepositoryImpl implements InternalTransferRepository {

    private final InternalTransferEntityMapper internalTransferEntityMapper;
    private final InternalTransferEntityRepository internalTransferEntityRepository;
    private final InternalTransferItemEntityRepository internalTransferItemEntityRepository;

    @Override
    public Mono<InternalTransfer> create(InternalTransfer internalTransfer) {
        return internalTransferEntityRepository.save(internalTransferEntityMapper.mapToEntity(internalTransfer))
            .flatMap(created ->
            Flux.fromIterable(internalTransfer.getInternalTransferItems())
                .map(internalTransferEntityMapper::mapToEntity)
                .map(item -> {
                    item.setInternalTransferId(created.getId());
                    return item;
                })
                .flatMap(this.internalTransferItemEntityRepository::save)
                .collect(Collectors.toList())
                .map(savedItems -> internalTransferEntityMapper.mapToDomain(created, savedItems))
        );

    }

    @Override
    public Mono<InternalTransfer> findOneById(Long id) {
        return internalTransferEntityRepository.findById(id)
            .zipWith(internalTransferItemEntityRepository.findAllByInternalTransferId(id)
                .collectList())
            .map(tuple -> internalTransferEntityMapper.mapToDomain(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Mono<InternalTransfer> findOneByOrderNumber(Long orderNumber) {
        return internalTransferEntityRepository.findByOrderNumber(orderNumber)
            .flatMap(entity -> findOneById(entity.getId()));
    }
}
