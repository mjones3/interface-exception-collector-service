package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import reactor.core.publisher.Mono;

public interface ExternalTransferRepository {

    Mono<ExternalTransfer> create(ExternalTransfer externalTransfer);

    Mono<ExternalTransfer> update(ExternalTransfer externalTransfer);

    Mono<ExternalTransfer> findOneById(final Long id);

    Mono<Void> deleteOneById(final Long id);
}
