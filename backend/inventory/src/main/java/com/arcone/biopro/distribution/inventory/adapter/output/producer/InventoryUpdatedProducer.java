package com.arcone.biopro.distribution.inventory.adapter.output.producer;

import com.arcone.biopro.distribution.inventory.adapter.in.listener.EventMessage;
import com.arcone.biopro.distribution.inventory.adapter.output.producer.event.InventoryUpdatedEvent;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryUpdatedApplicationEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryUpdatedProducer {

    final ReactiveKafkaProducerTemplate<String, EventMessage<InventoryUpdatedEvent>> producerInventoryUpdatedTemplate;

    final InventoryUpdatedMapper mapper;

    @Value("${topic.inventory-updated.name}")
    String topicName;

    @EventListener
    public void send(InventoryUpdatedApplicationEvent event) {
        var message = new EventMessage<>("InventoryUpdated","1.0", mapper.toEvent(event.inventory(), event.inventoryUpdateType()));
        producerInventoryUpdatedTemplate.send(topicName, message)
            .doOnError(e-> log.error("Send failed", e))
            .doOnNext(senderResult -> log.info("Inventory Updated Message {}-{} (updateType {}). Event produced: {}",
                event.inventory().getUnitNumber().value(),
                event.inventory().getProductCode().value(),
                event.inventoryUpdateType().name(),
                senderResult.recordMetadata()))
            .subscribe();

    }

}
