package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderModifiedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderModifiedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderModifiedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.OrderModifiedOutboundEventListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.OrderModifiedOutboundMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@RunWith(MockitoJUnitRunner.class)
class OrderModifiedOutboundListenerTest {

    private ReactiveKafkaProducerTemplate<String, EventMessage<OrderModifiedOutboundPayload>> producerTemplate;
    private OrderModifiedOutboundMapper orderModifiedOutboundMapper;
    private OrderModifiedOutboundEventListener listener;

    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        orderModifiedOutboundMapper = Mockito.mock(OrderModifiedOutboundMapper.class);
        listener = new OrderModifiedOutboundEventListener(producerTemplate, "TEST", orderModifiedOutboundMapper);
    }

    @Test
    public void shouldHandleOrderModifiedOutboundEvent() {
        var payload = Mockito.mock(OrderModifiedOutbound.class);
        var event = new OrderModifiedOutboundEvent(payload);
        var mappedPayload = Mockito.mock(OrderModifiedOutboundPayload.class);
        var senderResult = Mockito.mock(SenderResult.class);

        Mockito.when(orderModifiedOutboundMapper.toDto(payload)).thenReturn(mappedPayload);
        Mockito.when(producerTemplate.send(Mockito.any(org.apache.kafka.clients.producer.ProducerRecord.class))).thenReturn(Mono.just(senderResult));
        Mockito.when(payload.externalId()).thenReturn("test-external-id");
        Mockito.when(payload.orderNumber()).thenReturn(123);

        listener.handleOrderModifiedOutboundEvent(event);

        Mockito.verify(orderModifiedOutboundMapper).toDto(payload);
        Mockito.verify(producerTemplate).send(Mockito.any(org.apache.kafka.clients.producer.ProducerRecord.class));
    }
}