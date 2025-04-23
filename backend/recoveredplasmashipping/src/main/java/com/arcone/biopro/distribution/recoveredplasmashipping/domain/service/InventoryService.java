package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ValidateInventoryCommand;
import reactor.core.publisher.Mono;

public interface InventoryService {
    Mono<InventoryValidation> validateInventory(ValidateInventoryCommand validateInventoryCommand);
}
