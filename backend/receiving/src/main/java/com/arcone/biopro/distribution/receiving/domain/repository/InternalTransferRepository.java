package com.arcone.biopro.distribution.receiving.domain.repository;

import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import reactor.core.publisher.Mono;

public interface InternalTransferRepository {

    Mono<InternalTransfer> create(InternalTransfer internalTransfer);
    Mono<InternalTransfer> findOneById(final Long id);
    Mono<InternalTransfer> findOneByOrderNumber(final Long orderNumber);

}
