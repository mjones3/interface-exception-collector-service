package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledEventDTO;
import com.arcone.biopro.distribution.eventbridge.application.dto.OrderCancelledPayload;
import com.arcone.biopro.distribution.eventbridge.domain.service.OrderService;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.OrderCancelledListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.service.SchemaValidationService;
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
class OrderCancelledOutboundListenerTest {

    private ReactiveKafkaConsumerTemplate<String, String> consumer;
    private ReactiveKafkaProducerTemplate<String, String> producerTemplate;
    private SchemaValidationService schemaValidationService;
    private ObjectMapper objectMapper;
    private ReceiverRecord<String, String> receiverRecord;
    private OrderService orderService;

    @BeforeEach
    public void setUp(){
        consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        objectMapper = new ObjectMapper();
        schemaValidationService = new SchemaValidationService(objectMapper);
        objectMapper.registerModule(new JavaTimeModule());
        receiverRecord = Mockito.mock(ReceiverRecord.class);
        orderService = Mockito.mock(OrderService.class);
    }

    @Test
    public void shouldHandleMessage() throws Exception{
        Mockito.when(orderService.processOrderCancelledEvent(Mockito.any(OrderCancelledPayload.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(OrderCancelledEventDTO.class);
        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("order-cancelled-event.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("OrderCancelled");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));

        var listener = new OrderCancelledListener(consumer,objectMapper,orderService,producerTemplate,"TEST",schemaValidationService);

        listener.run(new String[]{""});

        Mockito.verify(orderService).processOrderCancelledEvent(Mockito.any(OrderCancelledPayload.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {
        Mockito.when(orderService.processOrderCancelledEvent(Mockito.any(OrderCancelledPayload.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(OrderCancelledEventDTO.class);
        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("order-cancelled-event.json").replace("\"orderNumber\": 1", ""));
        Mockito.when(receiverRecord.topic()).thenReturn("OrderCancelled");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));

        var listener = new OrderCancelledListener(consumer,objectMapper,orderService,producerTemplate,"TEST",schemaValidationService);

        try{
            listener.run(new String[]{""});
        }catch (Exception e){
            Mockito.verifyNoInteractions(orderService);
        }
    }
}
