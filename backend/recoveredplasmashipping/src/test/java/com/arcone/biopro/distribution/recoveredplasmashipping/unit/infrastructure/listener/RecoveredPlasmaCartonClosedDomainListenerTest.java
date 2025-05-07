package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonPackedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener.RecoveredPlasmaCartonClosedDomainListener;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaCartonEventMapper;
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

import java.util.List;

class RecoveredPlasmaCartonClosedDomainListenerTest {

    private RecoveredPlasmaCartonClosedDomainListener target;
    private RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper;

    private ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonPackedOutputEvent> producerTemplate;


    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        recoveredPlasmaCartonEventMapper = Mappers.getMapper(RecoveredPlasmaCartonEventMapper.class);
        target = new RecoveredPlasmaCartonClosedDomainListener(producerTemplate,"TestTopic", recoveredPlasmaCartonEventMapper);
    }

    @Test
    public void shouldHandleCartonClosedDomainEvents(){

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        CartonItem cartonItem = Mockito.mock(CartonItem.class);

        var domainPayload = Mockito.mock(Carton.class);
        Mockito.when(domainPayload.getProducts()).thenReturn(List.of(cartonItem));

        target.handleCartonClosedEvent(new RecoveredPlasmaCartonClosedEvent(domainPayload));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));
    }

}
