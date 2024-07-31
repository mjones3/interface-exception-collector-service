package com.arcone.biopro.distribution.shipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.service.ShipmentService;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.OrderFulfilledListener;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shipping.unit.util.TestUtil;
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


@RunWith(MockitoJUnitRunner.class)
class OrderFulfilledListenerTest {
    @Test
    public void shouldHandleMessage() throws Exception{

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        ReceiverRecord<String, String> receiverRecord = Mockito.mock(ReceiverRecord.class);

        ShipmentService service = Mockito.mock(ShipmentService.class);
        Mockito.when(service.create(Mockito.any(OrderFulfilledMessage.class))).thenReturn(Mono.empty());

        OrderFulfilledMessage message = Mockito.mock(OrderFulfilledMessage.class);
        Mockito.when(message.id()).thenReturn(1L);

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("order-fulfilled.json"));
        Mockito.when(receiverRecord.topic()).thenReturn("order.fulfilled");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));

        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(OrderFulfilledMessage.class))).thenReturn(message);

        OrderFulfilledListener listener = new OrderFulfilledListener(consumer,objectMapper,service);

        listener.run(new String[]{""});

        Mockito.verify(service).create(Mockito.any(OrderFulfilledMessage.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {

        ReactiveKafkaConsumerTemplate<String, String> consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);

        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        ConsumerRecord<String,String> consumerRecordMock = Mockito.mock(ConsumerRecord.class);

        ShipmentService service = Mockito.mock(ShipmentService.class);
        Mockito.when(service.create(Mockito.any(OrderFulfilledMessage.class))).thenReturn(Mono.empty());

        OrderFulfilledMessage message = Mockito.mock(OrderFulfilledMessage.class);
        Mockito.when(message.id()).thenReturn(1L);

        Mockito.when(consumerRecordMock.key()).thenReturn("test");
        Mockito.when(consumerRecordMock.value()).thenReturn("test");
        Mockito.when(consumerRecordMock.topic()).thenReturn("order.fulfilled");
        Mockito.when(consumerRecordMock.offset()).thenReturn(1L);

        Mockito.when(consumer.receiveAutoAck()).thenReturn(Flux.just(consumerRecordMock));

        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.eq(OrderFulfilledMessage.class))).thenThrow(JsonProcessingException.class);

        try{
            OrderFulfilledListener listener = new OrderFulfilledListener(consumer,objectMapper,service);

            listener.run(new String[]{""});

        }catch (Exception e){
            Mockito.verifyNoInteractions(service);
        }

    }

}
