package com.arcone.biopro.distribution.inventory.adapter.in.web.controller;

import com.arcone.biopro.distribution.inventory.domain.service.InventoryService;
import com.arcone.biopro.distribution.inventory.adapter.in.web.dto.InventoryRequestDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.web.dto.InventoryResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for Inventory.
 */
@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryResource {

    private final InventoryService service;

    /**
     * {@code POST  /v1/inventory : Inventory
     *
     * @param dto
     * @return the {@link ResponseEntity} with status {@code 201 (Created)}.
     */
    @PostMapping
    public Mono<ResponseEntity<InventoryResponseDTO>> createInventory(@RequestBody InventoryRequestDTO dto) {
        return service
            .create(dto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
}
