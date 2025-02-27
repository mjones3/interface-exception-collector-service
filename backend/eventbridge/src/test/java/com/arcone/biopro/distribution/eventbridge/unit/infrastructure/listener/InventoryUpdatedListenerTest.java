package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedEventDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import com.arcone.biopro.distribution.eventbridge.domain.service.InventoryUpdatedService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.InventoryUpdatedListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationInventoryUpdatedService;
import com.arcone.biopro.distribution.eventbridge.unit.util.TestUtil;
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
class InventoryUpdatedListenerTest {

    private ReactiveKafkaConsumerTemplate<String, String> consumer;
    private ReactiveKafkaProducerTemplate<String, String> producerTemplate;
    private SchemaValidationInventoryUpdatedService schemaValidationService;
    private ObjectMapper objectMapper;
    private ReceiverRecord<String, String> receiverRecord;
    private InventoryUpdatedService inventoryUpdatedService;

    @BeforeEach
    public void setUp(){

        consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        objectMapper = new ObjectMapper();
        schemaValidationService = new SchemaValidationInventoryUpdatedService(objectMapper);
        objectMapper.registerModule(new JavaTimeModule());
        receiverRecord = Mockito.mock(ReceiverRecord.class);
        inventoryUpdatedService = Mockito.mock(InventoryUpdatedService.class);
    }



    @Test
    public void shouldHandleMessage() throws Exception{

        Mockito.when(inventoryUpdatedService.processInventoryUpdatedEvent(Mockito.any(InventoryUpdatedPayload.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(InventoryUpdatedEventDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("inventory-updated-event.json").replace("{unit-number}", "W035625205983"));
        Mockito.when(receiverRecord.topic()).thenReturn("InventoryUpdated");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        var listener = new InventoryUpdatedListener(consumer,objectMapper,inventoryUpdatedService,producerTemplate,"TEST",schemaValidationService);

        listener.run(new String[]{""});

        Mockito.verify(inventoryUpdatedService).processInventoryUpdatedEvent(Mockito.any(InventoryUpdatedPayload.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {

        Mockito.when(inventoryUpdatedService.processInventoryUpdatedEvent(Mockito.any(InventoryUpdatedPayload.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(InventoryUpdatedEventDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("inventory-updated-event.json").replace("\"bloodType\": \"OP\"", ""));
        Mockito.when(receiverRecord.topic()).thenReturn("InventoryUpdated");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));

        var listener = new InventoryUpdatedListener(consumer,objectMapper,inventoryUpdatedService,producerTemplate,"TEST",schemaValidationService);

        try{
            listener.run(new String[]{""});
        }catch (Exception e){
            Mockito.verifyNoInteractions(inventoryUpdatedService);
        }
    }

}
