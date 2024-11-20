package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedEventDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.service.ShipmentCompletedService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.ShipmentCompletedListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
class ShipmentCompletedListenerTest {

    private ReactiveKafkaConsumerTemplate<String, String> consumer;
    private ReactiveKafkaProducerTemplate<String, String> producerTemplate;
    private SchemaValidationService schemaValidationService;
    private ObjectMapper objectMapper;
    private ReceiverRecord<String, String> receiverRecord;
    private ShipmentCompletedService shipmentCompletedService;

    @BeforeEach
    public void setUp(){

        consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        objectMapper = new ObjectMapper();
        schemaValidationService = new SchemaValidationService(objectMapper);
        objectMapper.registerModule(new JavaTimeModule());
        receiverRecord = Mockito.mock(ReceiverRecord.class);
        shipmentCompletedService = Mockito.mock(ShipmentCompletedService.class);
    }



    @Test
    public void shouldHandleMessage() throws Exception{

        Mockito.when(shipmentCompletedService.processCompletedShipmentEvent(Mockito.any(ShipmentCompletedPayload.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(ShipmentCompletedEventDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("shipment-completed-event.json").replace("\"{order-number}\"", "1"));
        Mockito.when(receiverRecord.topic()).thenReturn("ShipmentCompleted");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        var listener = new ShipmentCompletedListener(consumer,objectMapper,shipmentCompletedService,producerTemplate,"TEST",schemaValidationService);

        listener.run(new String[]{""});

        Mockito.verify(shipmentCompletedService).processCompletedShipmentEvent(Mockito.any(ShipmentCompletedPayload.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {

        Mockito.when(shipmentCompletedService.processCompletedShipmentEvent(Mockito.any(ShipmentCompletedPayload.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(ShipmentCompletedEventDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("shipment-completed-event.json").replace("\"{order-number}\"", ""));
        Mockito.when(receiverRecord.topic()).thenReturn("ShipmentCompleted");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));

        var listener = new ShipmentCompletedListener(consumer,objectMapper,shipmentCompletedService,producerTemplate,"TEST",schemaValidationService);

        try{
            listener.run(new String[]{""});
        }catch (Exception e){
            Mockito.verifyNoInteractions(shipmentCompletedService);
        }
    }

}
