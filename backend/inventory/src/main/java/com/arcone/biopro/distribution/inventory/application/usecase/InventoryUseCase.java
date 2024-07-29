package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.adapter.in.web.dto.InventoryRequestDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.web.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryCreatedEvent;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryRepository;
import com.arcone.biopro.distribution.inventory.domain.service.InventoryService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Service class for Inventory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryUseCase implements InventoryService {

    private final InventoryRepository repository;
    private final ReactiveKafkaProducerTemplate<String, InventoryCreatedEvent> producerTemplate;

    /**
     * Create Inventory.
     *
     * @param dto InventoryRequestDTO
     * @return Mono<InventoryResponseDTO>
     */
    @WithSpan("createInventory")
    @Transactional
    public Mono<InventoryResponseDTO> create(InventoryRequestDTO dto) {
        var entity = Inventory.builder().id(dto.id()).build();
        return repository.save(entity)
            .mapNotNull(Inventory::getId)
            .map(InventoryResponseDTO::new)
            .doOnNext(this::accept)
            .doOnSuccess(response -> log.info("Inventory created: {}", response));
    }

    /**
     * Send the event to Kafka.
     *
     * @param response InventoryResponseDTO
     */
    private void accept(InventoryResponseDTO response) {
        producerTemplate.send("inventory.created", new InventoryCreatedEvent(response.id()));
    }

}
