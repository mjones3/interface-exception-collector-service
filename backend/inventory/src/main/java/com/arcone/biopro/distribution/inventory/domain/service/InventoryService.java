package com.arcone.biopro.distribution.inventory.domain.service;

import com.arcone.biopro.distribution.inventory.adapter.in.web.dto.InventoryRequestDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.web.dto.InventoryResponseDTO;
import reactor.core.publisher.Mono;

public interface InventoryService {

    Mono<InventoryResponseDTO> create(InventoryRequestDTO request);

}
