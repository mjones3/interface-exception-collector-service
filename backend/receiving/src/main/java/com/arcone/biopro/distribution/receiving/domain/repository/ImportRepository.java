package com.arcone.biopro.distribution.receiving.domain.repository;

import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import reactor.core.publisher.Mono;

public interface ImportRepository {
    Mono<Import> create(Import importModel);
    Mono<Import> findOneById(Long id);
    Mono<ImportItem> createImportItem(ImportItem importItemModel);
    Mono<Import> update(Import importModel);
    Mono<Void> deleteOneById(Long id);
}
