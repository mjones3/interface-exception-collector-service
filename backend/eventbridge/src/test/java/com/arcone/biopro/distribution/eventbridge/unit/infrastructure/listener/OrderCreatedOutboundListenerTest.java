package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderCreatedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderCreatedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderCreatedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.OrderCreatedOutboundEventListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.OrderCreatedOutboundMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@RunWith(MockitoJUnitRunner.class)
class OrderCreatedOutboundListenerTest {

    private ReactiveKafkaProducerTemplate<String, EventMessage<OrderCreatedOutboundPayload>> producerTemplate;
    private OrderCreatedOutboundMapper orderCreatedOutboundMapper;
    private OrderCreatedOutboundEventListener listener;

    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        orderCreatedOutboundMapper = Mockito.mock(OrderCreatedOutboundMapper.class);
        listener = new OrderCreatedOutboundEventListener(producerTemplate, "TEST", orderCreatedOutboundMapper);
    }

    @Test
    public void shouldHandleOrderCreatedOutboundEvent() {
        var payload = Mockito.mock(OrderCreatedOutbound.class);
        var event = new OrderCreatedOutboundEvent(payload);
        var mappedPayload = Mockito.mock(OrderCreatedOutboundPayload.class);
        var senderResult = Mockito.mock(SenderResult.class);

        Mockito.when(orderCreatedOutboundMapper.toDto(payload)).thenReturn(mappedPayload);
        Mockito.when(producerTemplate.send(Mockito.any(org.apache.kafka.clients.producer.ProducerRecord.class))).thenReturn(Mono.just(senderResult));
        Mockito.when(payload.externalId()).thenReturn("test-external-id");
        Mockito.when(payload.orderNumber()).thenReturn(123);

        listener.handleOrderCreatedOutboundEvent(event);

        Mockito.verify(orderCreatedOutboundMapper).toDto(payload);
        Mockito.verify(producerTemplate).send(Mockito.any(org.apache.kafka.clients.producer.ProducerRecord.class));
    }
}