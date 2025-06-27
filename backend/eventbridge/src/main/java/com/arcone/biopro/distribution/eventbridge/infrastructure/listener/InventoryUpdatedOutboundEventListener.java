package com.arcone.biopro.distribution.eventbridge.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.InventoryUpdatedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.InventoryUpdatedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.InventoryUpdatedOutboundMapper;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@Slf4j
@Profile("prod")
public class InventoryUpdatedOutboundEventListener {

    private final ReactiveKafkaProducerTemplate<String, EventMessage<InventoryUpdatedOutboundPayload>> producerTemplate;
    private final String topicName;
    private final InventoryUpdatedOutboundMapper inventoryUpdatedOutboundMapper;

    public InventoryUpdatedOutboundEventListener(@Qualifier(KafkaConfiguration.INVENTORY_UPDATED_OUTBOUND_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, EventMessage<InventoryUpdatedOutboundPayload>> producerTemplate,
                                                 @Value("${topics.inventory.inventory-updated-outbound.topic-name:InventoryUpdatedOutbound}") String topicName
    , InventoryUpdatedOutboundMapper inventoryUpdatedOutboundMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.inventoryUpdatedOutboundMapper = inventoryUpdatedOutboundMapper;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "InventoryUpdatedOutbound",
        description = "Inventory Updated Outbound Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "import com.arcone.biopro.distribution.eventbridge.domain.event.InventoryUpdatedOutboundEvent"
        )),
        message = @AsyncMessage(
            name = "InventoryUpdatedOutbound",
            title = "InventoryUpdatedOutbound",
            description = "Inventory Updated Outbound Event"
        )
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handleInventoryUpdatedOutboundEvent(InventoryUpdatedOutboundEvent event) {
        log.debug("Inventory Updated Outbound event trigger Event ID {}", event.getEventId());

        var message = new EventMessage<>("InventoryUpdatedOutbound","1.0",inventoryUpdatedOutboundMapper.toDto(event.getPayload()));

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
                .doOnError(e-> log.error("Send failed", e))
                .doOnNext(senderResult -> log.info("Inventory Updated Outbound Message {}-{} (updateType {}). Event produced: {}",
                        event.getPayload().getUnitNumber(),
                        event.getPayload().getProductCode(),
                        event.getPayload().getUpdateType(),
                        senderResult.recordMetadata()))
                .subscribe();
    }

}
