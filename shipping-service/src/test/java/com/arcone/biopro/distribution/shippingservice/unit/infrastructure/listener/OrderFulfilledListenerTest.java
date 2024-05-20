package com.arcone.biopro.distribution.shippingservice.unit.infrastructure.listener;

import com.arcone.biopro.distribution.shippingservice.domain.service.OrderFulfilledService;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.OrderFulfilledListener;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.unit.util.TestUtil;
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


@RunWith(MockitoJUnitRunner.class)
class OrderFulfilledListenerTest {
    @Test
    public void shouldHandleMessage() throws Exception{

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        ConsumerRecord<String,String> consumerRecordMock = Mockito.mock(ConsumerRecord.class);

        OrderFulfilledService service = Mockito.mock(OrderFulfilledService.class);
        Mockito.when(service.create(Mockito.any(OrderFulfilledMessage.class))).thenReturn(Mono.empty());

        OrderFulfilledMessage message = Mockito.mock(OrderFulfilledMessage.class);
        Mockito.when(message.id()).thenReturn(1L);

        Mockito.when(consumerRecordMock.key()).thenReturn("test");
        Mockito.when(consumerRecordMock.value()).thenReturn(TestUtil.resource("order-fulfilled.json"));
        Mockito.when(consumerRecordMock.topic()).thenReturn("order.fulfilled");
        Mockito.when(consumerRecordMock.offset()).thenReturn(1L);

        Mockito.when(consumer.receiveAutoAck()).thenReturn(Flux.just(consumerRecordMock));

        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(OrderFulfilledMessage.class))).thenReturn(message);

        OrderFulfilledListener listener = new OrderFulfilledListener(consumer,objectMapper,service);

        listener.run(new String[]{""});

        Mockito.verify(service).create(Mockito.any(OrderFulfilledMessage.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception{

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        ConsumerRecord<String,String> consumerRecordMock = Mockito.mock(ConsumerRecord.class);

        OrderFulfilledService service = Mockito.mock(OrderFulfilledService.class);
        Mockito.when(service.create(Mockito.any(OrderFulfilledMessage.class))).thenReturn(Mono.empty());

        OrderFulfilledMessage message = Mockito.mock(OrderFulfilledMessage.class);
        Mockito.when(message.id()).thenReturn(1L);

        Mockito.when(consumerRecordMock.key()).thenReturn("test");
        Mockito.when(consumerRecordMock.value()).thenReturn("test");
        Mockito.when(consumerRecordMock.topic()).thenReturn("order.fulfilled");
        Mockito.when(consumerRecordMock.offset()).thenReturn(1L);

        Mockito.when(consumer.receiveAutoAck()).thenReturn(Flux.just(consumerRecordMock));

        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(OrderFulfilledMessage.class))).thenThrow(JsonProcessingException.class);

        OrderFulfilledListener listener = new OrderFulfilledListener(consumer,objectMapper,service);

        listener.run(new String[]{""});

        Mockito.verifyNoInteractions(service);
    }

}
