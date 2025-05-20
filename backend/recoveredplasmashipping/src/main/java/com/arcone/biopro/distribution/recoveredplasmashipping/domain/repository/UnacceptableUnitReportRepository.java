package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UnacceptableUnitReportRepository {

    Flux<UnacceptableUnitReportItem> findAllByShipmentId(final Long shipmentId);
    Mono<UnacceptableUnitReportItem> save(UnacceptableUnitReportItem unacceptableUnitReportItem);
    Mono<Void> deleteAllByShipmentId(final Long shipmentId);

}
