package com.arcone.biopro.distribution.shipping.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.mapper.ProductLocationHistoryMapper;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ProductLocationHistoryEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductLocationHistoryListener {
    private  final ProductLocationHistoryEntityRepository productLocationHistoryEntityRepository;
    private final ProductLocationHistoryMapper productLocationHistoryMapper;

    @EventListener
    public void handleShipmentCompletedEvent(ShipmentCompletedEvent event) {
        log.debug("Shipment Completed event trigger Event ID {}", event.getEventId());
        var payload = event.getPayload();

        Flux.fromStream(payload.lineItems().stream())
            .flatMap(lineItem -> Flux.fromStream(lineItem.products().stream())
                .flatMap(product -> productLocationHistoryEntityRepository.save(productLocationHistoryMapper.toEntity(payload,product))
                ).collectList()).subscribe();

    }
}
