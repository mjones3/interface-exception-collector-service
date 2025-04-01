package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentCreatedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentCreatedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener.RecoveredPlasmaShipmentCreatedListener;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentEventMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class RecoveredPlasmaShipmentCreatedListenerTest {

    private RecoveredPlasmaShipmentCreatedListener target;
    private RecoveredPlasmaShipmentEventMapper recoveredPlasmaShipmentEventMapper;

    private ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentCreatedOutputEvent> producerTemplate;


    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        recoveredPlasmaShipmentEventMapper = Mappers.getMapper(RecoveredPlasmaShipmentEventMapper.class);
        target = new RecoveredPlasmaShipmentCreatedListener(producerTemplate,"TestTopic", recoveredPlasmaShipmentEventMapper);
    }

    @Test
    public void shouldHandleShipmentCreatedDomainEvents(){

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        var domainShipment = Mockito.mock(RecoveredPlasmaShipment.class);

        target.handleShipmentCreatedEvent(new RecoveredPlasmaShipmentCreatedEvent(domainShipment));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));
    }

}
