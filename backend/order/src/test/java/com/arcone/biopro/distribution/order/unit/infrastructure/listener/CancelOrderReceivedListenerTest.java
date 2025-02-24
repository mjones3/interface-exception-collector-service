package com.arcone.biopro.distribution.order.unit.infrastructure.listener;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import com.arcone.biopro.distribution.order.domain.service.CancelOrderService;
import com.arcone.biopro.distribution.order.infrastructure.listener.CancelOrderReceivedListener;
import com.arcone.biopro.distribution.order.unit.util.TestUtil;
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
class CancelOrderReceivedListenerTest {

    private ReactiveKafkaConsumerTemplate<String, String> consumer;
    private ReactiveKafkaProducerTemplate<String, String> producerTemplate;
    private ObjectMapper objectMapper;
    private ReceiverRecord<String, String> receiverRecord;
    private CancelOrderService cancelOrderService;

    @BeforeEach
    public void setUp(){

        consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        objectMapper = new ObjectMapper();
        cancelOrderService = Mockito.mock(CancelOrderService.class);
        objectMapper.registerModule(new JavaTimeModule());
        receiverRecord = Mockito.mock(ReceiverRecord.class);
    }



    @Test
    public void shouldHandleMessage() throws Exception{

        Mockito.when(cancelOrderService.processCancelOrderReceivedEvent(Mockito.any(CancelOrderReceivedDTO.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(CancelOrderReceivedDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("cancel-order-received.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("CancelOrderReceived");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        var listener = new CancelOrderReceivedListener(consumer,objectMapper,cancelOrderService,producerTemplate,"TEST");

        listener.run(new String[]{""});

        Mockito.verify(cancelOrderService).processCancelOrderReceivedEvent(Mockito.any(CancelOrderReceivedDTO.class));
    }

}
