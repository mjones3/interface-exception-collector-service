package com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Page;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentReport;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShipmentReportRepository {

    Mono<Page<RecoveredPlasmaShipmentReport>> search(RecoveredPlasmaShipmentQueryCommand recoveredPlasmaShipmentQueryCommand);
}
