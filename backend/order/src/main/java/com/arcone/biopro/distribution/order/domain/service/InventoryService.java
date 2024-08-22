package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.domain.model.AvailableInventory;
import com.arcone.biopro.distribution.order.domain.model.GeneratePickListCommand;
import reactor.core.publisher.Flux;

public interface InventoryService {

    Flux<AvailableInventory> getAvailableInventories(GeneratePickListCommand generatePickListCommand);
}
