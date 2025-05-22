package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaCartonRemovedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaCartonRemovedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener.RecoveredPlasmaCartonRemovedDomainListener;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaCartonEventMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntityRepository;
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
import reactor.test.StepVerifier;

import java.util.List;

class RecoveredPlasmaCartonRemovedDomainListenerTest {

    private RecoveredPlasmaCartonRemovedDomainListener target;
    private RecoveredPlasmaCartonEventMapper recoveredPlasmaCartonEventMapper;

    private ReactiveKafkaProducerTemplate<String, RecoveredPlasmaCartonRemovedOutputEvent> producerTemplate;
    private RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShipmentEntityRepository;


    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        recoveredPlasmaCartonEventMapper = Mappers.getMapper(RecoveredPlasmaCartonEventMapper.class);
        recoveredPlasmaShipmentEntityRepository = Mockito.mock(RecoveredPlasmaShipmentEntityRepository.class);
        target = new RecoveredPlasmaCartonRemovedDomainListener(producerTemplate,"TestTopic", recoveredPlasmaCartonEventMapper,recoveredPlasmaShipmentEntityRepository);
    }

    @Test
    public void shouldHandleCartonRemovedDomainEvents(){

        Mockito.when(recoveredPlasmaShipmentEntityRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(RecoveredPlasmaShipmentEntity.builder().locationCode("LOCATION_CODE").build()));

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        CartonItem cartonItem = Mockito.mock(CartonItem.class);

        var domainPayload = Mockito.mock(Carton.class);
        Mockito.when(domainPayload.getProducts()).thenReturn(List.of(cartonItem));

        StepVerifier.create(target.handleCartonRemovedEvent(new RecoveredPlasmaCartonRemovedEvent(domainPayload)))
            .verifyComplete();

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));
    }

}
