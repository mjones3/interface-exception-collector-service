package com.arcone.biopro.distribution.shippingservice.application.usecase;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShippingServiceRequestDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShippingServiceResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.event.ShippingServiceCreatedEvent;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShippingService;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShippingServiceRepository;
import com.arcone.biopro.distribution.shippingservice.domain.service.ShippingServiceService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Service class for Shipping Service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceUseCase implements ShippingServiceService {

    private final ShippingServiceRepository repository;
    private final ReactiveKafkaProducerTemplate<String, ShippingServiceCreatedEvent> producerTemplate;

    /**
     * Create Shipping Service.
     *
     * @param dto ShippingServiceRequestDTO
     * @return Mono<ShippingServiceResponseDTO>
     */
    @WithSpan("createShippingService")
    @Transactional
    public Mono<ShippingServiceResponseDTO> create(ShippingServiceRequestDTO dto) {
        var entity = ShippingService.builder().id(dto.id()).build();
        return repository.save(entity)
            .mapNotNull(ShippingService::getId)
            .map(ShippingServiceResponseDTO::new)
            .doOnNext(this::accept)
            .doOnSuccess(response -> log.info("Shipping Service created: {}", response));
    }

    /**
     * Send the event to Kafka.
     *
     * @param response ShippingServiceResponseDTO
     */
    private void accept(ShippingServiceResponseDTO response) {
        producerTemplate.send("shipping-service.created", new ShippingServiceCreatedEvent(response.id()));
    }

}
