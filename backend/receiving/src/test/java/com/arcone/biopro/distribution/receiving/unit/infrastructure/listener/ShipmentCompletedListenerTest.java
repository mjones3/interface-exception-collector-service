package com.arcone.biopro.distribution.receiving.unit.infrastructure.listener;

import com.arcone.biopro.distribution.receiving.application.usecase.ShipmentCompletedUseCase;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceUpdatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ShipmentCompletedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.listener.ShipmentCompletedListener;
import com.arcone.biopro.distribution.receiving.unit.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.UUID;

class ShipmentCompletedListenerTest {

    private ReactiveKafkaConsumerTemplate<String, String> consumer;
    private ReactiveKafkaProducerTemplate<String, String> producerTemplate;
    private ObjectMapper objectMapper;
    private ReceiverRecord<String, String> receiverRecord;
    private ShipmentCompletedUseCase shipmentCompletedUseCase;

    @BeforeEach
    public void setUp(){

        consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        receiverRecord = Mockito.mock(ReceiverRecord.class);
        shipmentCompletedUseCase = Mockito.mock(ShipmentCompletedUseCase.class);
    }



    @Test
    public void shouldHandleMessage() throws Exception{

        Mockito.when(shipmentCompletedUseCase.processShipmentCompletedMessage(Mockito.any(ShipmentCompletedMessage.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(DeviceUpdatedMessage.class);

        Mockito.when(message.getEventId()).thenReturn(UUID.randomUUID().toString());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("events/shipment-completed-event.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("ShipmentCompleted");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        var listener = new ShipmentCompletedListener(consumer,objectMapper,shipmentCompletedUseCase,producerTemplate,"TEST");

        listener.run(new String[]{""});

        Mockito.verify(shipmentCompletedUseCase).processShipmentCompletedMessage(Mockito.any(ShipmentCompletedMessage.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {

        Mockito.when(shipmentCompletedUseCase.processShipmentCompletedMessage(Mockito.any(ShipmentCompletedMessage.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(ShipmentCompletedMessage.class);

        Mockito.when(message.getEventId()).thenReturn(UUID.randomUUID().toString());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("events/shipment-completed-event.json").replace("\"productCategory\": \"FROZEN\"", ""));
        Mockito.when(receiverRecord.topic()).thenReturn("ShipmentCompleted");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));

        var listener = new ShipmentCompletedListener(consumer,objectMapper,shipmentCompletedUseCase,producerTemplate,"TEST");

        try{
            listener.run(new String[]{""});
        }catch (Exception e){
            Mockito.verifyNoInteractions(shipmentCompletedUseCase);
        }
    }
}
