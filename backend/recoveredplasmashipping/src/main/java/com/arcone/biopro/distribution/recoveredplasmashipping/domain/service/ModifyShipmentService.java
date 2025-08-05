package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ModifyShipmentCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface ModifyShipmentService {

    Mono<UseCaseOutput<RecoveredPlasmaShipmentOutput>> modifyShipment(ModifyShipmentCommandInput modifyShipmentCommandInput);
}
