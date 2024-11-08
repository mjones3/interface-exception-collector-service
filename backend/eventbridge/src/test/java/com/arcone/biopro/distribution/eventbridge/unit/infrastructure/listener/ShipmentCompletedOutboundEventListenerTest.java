package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.ShipmentCompletedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.ShipmentCompletedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.ShipmentCompletedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.ShipmentCompletedOutboundEventListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.ShipmentCompletedOutboundMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class ShipmentCompletedOutboundEventListenerTest {


    @Test
    public void shouldHandleShipmentCompletedOutboundEvents(){

        ReactiveKafkaProducerTemplate<String, ShipmentCompletedOutboundPayload> producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);

        var mapper = new ShipmentCompletedOutboundMapper();

        var model = Mockito.mock(ShipmentCompletedOutbound.class);

        var target = new ShipmentCompletedOutboundEventListener(producerTemplate, "TestTopic",mapper);


        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleShipmentCompletedOutboundEvent(new ShipmentCompletedOutboundEvent(model));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));

    }

}
