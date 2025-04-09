package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PageOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentQueryCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface RecoveredPlasmaShipmentReportService {

    Mono<UseCaseOutput<PageOutput<RecoveredPlasmaShipmentReportOutput>>> search(RecoveredPlasmaShipmentQueryCommandInput recoveredPlasmaShipmentQueryCommandInput);
}
