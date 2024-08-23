package com.arcone.biopro.distribution.order.unit.infrastructure.listener;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEventDTO;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import com.arcone.biopro.distribution.order.infrastructure.listener.ShipmentCreatedListener;
import com.arcone.biopro.distribution.order.unit.util.TestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
class ShipmentCreatedListenerTest {

    @Test
    public void shouldHandleMessage() throws Exception{

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ReceiverRecord<String, String> receiverRecord = Mockito.mock(ReceiverRecord.class);

        var service = Mockito.mock(OrderShipmentService.class);
        Mockito.when(service.processShipmentCreatedEvent(Mockito.any(ShipmentCreatedEvenPayloadDTO.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(ShipmentCreatedEventDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("shipment-created-event.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("ShipmentCreated");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        ShipmentCreatedListener listener = new ShipmentCreatedListener(consumer,objectMapper,service);

        listener.run(new String[]{""});

        Mockito.verify(service).processShipmentCreatedEvent(Mockito.any(ShipmentCreatedEvenPayloadDTO.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        ReceiverRecord<String, String> receiverRecord = Mockito.mock(ReceiverRecord.class);

        var service = Mockito.mock(OrderShipmentService.class);
        Mockito.when(service.processShipmentCreatedEvent(Mockito.any(ShipmentCreatedEvenPayloadDTO.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(ShipmentCreatedEventDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("shipment-created-event.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("ShipmentCreated");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(ShipmentCreatedEventDTO.class))).thenThrow(JsonProcessingException.class);

        try{
            ShipmentCreatedListener listener = new ShipmentCreatedListener(consumer,objectMapper,service);
            listener.run(new String[]{""});
        }catch (Exception e){
            Mockito.verifyNoInteractions(service);
        }



    }
}
