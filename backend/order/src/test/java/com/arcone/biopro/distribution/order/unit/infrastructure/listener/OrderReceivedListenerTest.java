package com.arcone.biopro.distribution.order.unit.infrastructure.listener;

import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import com.arcone.biopro.distribution.order.infrastructure.listener.OrderReceivedListener;
import com.arcone.biopro.distribution.order.unit.util.TestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
class OrderReceivedListenerTest {

    @Test
    public void shouldHandleMessage() throws Exception{

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ReceiverRecord<String, String> receiverRecord = Mockito.mock(ReceiverRecord.class);

        OrderService service = Mockito.mock(OrderService.class);
        Mockito.when(service.processOrder(Mockito.any(OrderReceivedEventPayloadDTO.class))).thenReturn(Mono.empty());

        OrderReceivedEventPayloadDTO message = Mockito.mock(OrderReceivedEventPayloadDTO.class);

        Mockito.when(message.id()).thenReturn(UUID.randomUUID());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("order-inbound-scenario-1-happy-path.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("OrderReceived");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        OrderReceivedListener listener = new OrderReceivedListener(consumer,service,new ObjectMapper());

        listener.run(new String[]{""});

        Mockito.verify(service).processOrder(Mockito.any(OrderReceivedEventPayloadDTO.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        ConsumerRecord<String,String> consumerRecordMock = Mockito.mock(ConsumerRecord.class);

        OrderService service = Mockito.mock(OrderService.class);
        Mockito.when(service.processOrder(Mockito.any(OrderReceivedEventPayloadDTO.class))).thenReturn(Mono.empty());

        OrderReceivedEventPayloadDTO message = Mockito.mock(OrderReceivedEventPayloadDTO.class);

        Mockito.when(message.id()).thenReturn(UUID.randomUUID());

        Mockito.when(consumerRecordMock.key()).thenReturn("test");
        Mockito.when(consumerRecordMock.value()).thenReturn(TestUtil.resource("order-inbound-scenario-1-happy-path.json"));
        Mockito.when(consumerRecordMock.topic()).thenReturn("OrderReceived");
        Mockito.when(consumerRecordMock.offset()).thenReturn(1L);

        Mockito.when(consumer.receiveAutoAck()).thenReturn(Flux.just(consumerRecordMock));

        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(OrderReceivedEventPayloadDTO.class))).thenThrow(JsonProcessingException.class);

        try{
            OrderReceivedListener listener = new OrderReceivedListener(consumer,service,objectMapper);
            listener.run(new String[]{""});
        }catch (Exception e){
            Mockito.verifyNoInteractions(service);
        }

    }

}
