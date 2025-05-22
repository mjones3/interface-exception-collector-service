package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RemoveCartonCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface RemoveCartonService {

    Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> removeCarton (RemoveCartonCommandInput removeCartonCommandInput);
}
