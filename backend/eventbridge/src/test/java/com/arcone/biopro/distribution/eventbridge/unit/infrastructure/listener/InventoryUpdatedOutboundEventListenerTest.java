package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.InventoryUpdatedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.InventoryUpdatedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.InventoryUpdatedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.InventoryUpdatedOutboundEventListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.InventoryUpdatedOutboundMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class InventoryUpdatedOutboundEventListenerTest {


    @Test
    public void shouldHandleInventoryUpdatedOutboundEvents(){

        ReactiveKafkaProducerTemplate<String, InventoryUpdatedOutboundPayload> producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);

        var mapper = new InventoryUpdatedOutboundMapper();

        var model = Mockito.mock(InventoryUpdatedOutbound.class);
        Mockito.when(model.getUpdateType()).thenReturn("UPDATE_TYPE");
        Mockito.when(model.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(model.getProductCode()).thenReturn("PRODUCT_CODE");
        var target = new InventoryUpdatedOutboundEventListener(producerTemplate, "TestTopic",mapper);


        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleInventoryUpdatedOutboundEvent(new InventoryUpdatedOutboundEvent(model));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));

    }

}
