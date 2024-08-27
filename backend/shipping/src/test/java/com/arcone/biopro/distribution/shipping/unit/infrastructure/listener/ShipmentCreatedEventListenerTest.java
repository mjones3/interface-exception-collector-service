package com.arcone.biopro.distribution.shipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCreatedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.ShipmentCreatedEventListener;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShipmentCreatedEventDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class ShipmentCreatedEventListenerTest {


    @Test
    public void shouldHandleShipmentCreatedEvents(){

        ReactiveKafkaProducerTemplate<String, ShipmentCreatedEventDTO> producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);

        var shipment = Mockito.mock(Shipment.class);

        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);

        var target = new ShipmentCreatedEventListener(producerTemplate, "TestTopic");


        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleShipmentCreatedEvent(new ShipmentCreatedEvent(shipment));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));

    }

}
