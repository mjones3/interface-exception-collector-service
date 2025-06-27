package com.arcone.biopro.distribution.eventbridge.unit.infrastructure.listener;

import com.arcone.biopro.distribution.eventbridge.domain.event.EventMessage;
import com.arcone.biopro.distribution.eventbridge.domain.event.OrderRejectedOutboundEvent;
import com.arcone.biopro.distribution.eventbridge.domain.model.OrderRejectedOutbound;
import com.arcone.biopro.distribution.eventbridge.infrastructure.dto.OrderRejectedOutboundPayload;
import com.arcone.biopro.distribution.eventbridge.infrastructure.listener.OrderRejectedOutboundEventListener;
import com.arcone.biopro.distribution.eventbridge.infrastructure.mapper.OrderRejectedOutboundMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@RunWith(MockitoJUnitRunner.class)
class OrderRejectedOutboundListenerTest {

    private ReactiveKafkaProducerTemplate<String, EventMessage<OrderRejectedOutboundPayload>> producerTemplate;
    private OrderRejectedOutboundMapper orderRejectedOutboundMapper;
    private OrderRejectedOutboundEventListener listener;

    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        orderRejectedOutboundMapper = Mockito.mock(OrderRejectedOutboundMapper.class);
        listener = new OrderRejectedOutboundEventListener(producerTemplate, "TEST", orderRejectedOutboundMapper);
    }

    @Test
    public void shouldHandleOrderRejectedOutboundEvent() {
        var payload = Mockito.mock(OrderRejectedOutbound.class);
        var event = new OrderRejectedOutboundEvent(payload);
        var mappedPayload = Mockito.mock(OrderRejectedOutboundPayload.class);
        var senderResult = Mockito.mock(SenderResult.class);

        Mockito.when(orderRejectedOutboundMapper.toDto(payload)).thenReturn(mappedPayload);
        Mockito.when(producerTemplate.send(Mockito.any(org.apache.kafka.clients.producer.ProducerRecord.class))).thenReturn(Mono.just(senderResult));
        Mockito.when(payload.externalId()).thenReturn("test-external-id");
        Mockito.when(payload.operation()).thenReturn("REJECT_ORDER");

        listener.handleOrderRejectedOutboundEvent(event);

        Mockito.verify(orderRejectedOutboundMapper).toDto(payload);
        Mockito.verify(producerTemplate).send(Mockito.any(org.apache.kafka.clients.producer.ProducerRecord.class));
    }
}