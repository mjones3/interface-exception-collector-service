package com.arcone.biopro.distribution.order.unit.infrastructure.listener;

import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedDTO;
import com.arcone.biopro.distribution.order.domain.service.ModifyOrderService;
import com.arcone.biopro.distribution.order.infrastructure.listener.ModifyOrderReceivedListener;
import com.arcone.biopro.distribution.order.unit.util.TestUtil;
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

class ModifyOrderReceivedListenerTest {

    private ReactiveKafkaConsumerTemplate<String, String> consumer;
    private ReactiveKafkaProducerTemplate<String, String> producerTemplate;
    private ObjectMapper objectMapper;
    private ReceiverRecord<String, String> receiverRecord;
    private ModifyOrderService modifyOrderService;

    @BeforeEach
    public void setUp(){

        consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        objectMapper = new ObjectMapper();
        modifyOrderService = Mockito.mock(ModifyOrderService.class);
        objectMapper.registerModule(new JavaTimeModule());
        receiverRecord = Mockito.mock(ReceiverRecord.class);
    }



    @Test
    public void shouldHandleMessage() throws Exception{

        Mockito.when(modifyOrderService.processModifyOrderEvent(Mockito.any(ModifyOrderReceivedDTO.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(ModifyOrderReceivedDTO.class);

        Mockito.when(message.eventId()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("modify-order-received.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("ModifyOrderReceived");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        var listener = new ModifyOrderReceivedListener(consumer,objectMapper,modifyOrderService,producerTemplate,"TEST");

        listener.run(new String[]{""});

        Mockito.verify(modifyOrderService).processModifyOrderEvent(Mockito.any(ModifyOrderReceivedDTO.class));
    }

}
