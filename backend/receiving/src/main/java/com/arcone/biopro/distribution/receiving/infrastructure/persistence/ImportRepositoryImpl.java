package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.repository.ImportRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ImportEntityMapper;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ImportItemEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ImportRepositoryImpl implements ImportRepository {
    private final ImportEntityRepository importEntityRepository;
    private final ImportEntityMapper importEntityMapper;
    private final ImportItemEntityRepository importItemEntityRepository;
    private final ImportItemEntityMapper importItemEntityMapper;
    private final ImportItemConsequenceEntityRepository importItemConsequenceEntityRepository;
    private final ImportItemPropertyEntityRepository importItemPropertyEntityRepository;
    private final SystemProcessPropertyEntityRepository systemProcessPropertyEntityRepository;


    @Override
    public Mono<Import> create(Import importModel) {
        return importEntityRepository.save(importEntityMapper.toEntity(importModel))
            .flatMap(created -> {
                return Mono.zip(Mono.just(created), getMaxNumberOfProducts())
                    .map(tuple -> importEntityMapper.mapToDomain(tuple.getT1(), tuple.getT2()));
            });
    }

    @Override
    public Mono<Import> findOneById(Long id) {
        var itemList = findAllByImportId(id).collectList();
        return importEntityRepository.findByIdAndDeleteDateIsNull(id)
            .flatMap(importEntity -> {
                return Mono.zip(Mono.just(importEntity), itemList, getMaxNumberOfProducts())
                    .map(tuple -> importEntityMapper.mapToDomain(tuple.getT1(), tuple.getT2(), tuple.getT3()));
            });
    }

    @Override
    public Mono<ImportItem> createImportItem(ImportItem importItemModel) {
        return importItemEntityRepository.save(importItemEntityMapper.toEntity(importItemModel))
            .flatMap(createImportItem -> {
                return Mono.zip(Mono.just(createImportItem), importItemConsequenceEntityRepository.saveAll(importItemEntityMapper.toConsequenceEntities(createImportItem, importItemModel.getConsequences())).collectList()
                        , importItemPropertyEntityRepository.saveAll(importItemEntityMapper.toPropertyEntities(createImportItem, importItemModel.getProperties())).collectList())
                    .map(tuple -> importItemEntityMapper.mapToDomain(tuple.getT1(), tuple.getT2(), tuple.getT3()));
            });
    }

    private Flux<ImportItem> findAllByImportId(Long importId) {
        return importItemEntityRepository.findAllByImportId(importId)
            .flatMap(importItem -> {
                return Mono.zip(importItemConsequenceEntityRepository.findAllByImportItemId(importItem.getId()).collectList()
                        , importItemPropertyEntityRepository.findAllByImportItemId(importItem.getId()).collectList())
                    .map(tuple -> importItemEntityMapper.mapToDomain(importItem, tuple.getT1(), tuple.getT2()));
            });
    }

    private Mono<Integer> getMaxNumberOfProducts() {
        return systemProcessPropertyEntityRepository.findFirstBySystemProcessTypeAndPropertyKey("IMPORTS", "MAX_NUMBER_OF_PRODUCTS_BATCH")
            .map(property -> Integer.parseInt(property.getPropertyValue()));
    }

}
