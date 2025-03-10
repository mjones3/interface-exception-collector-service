package com.arcone.biopro.distribution.order.unit.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderRejectedOutputEvent;
import com.arcone.biopro.distribution.order.infrastructure.listener.OrderRejectedListener;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class OrderRejectedListenerTest {

    private OrderRejectedListener target;

    private ReactiveKafkaProducerTemplate<String, OrderRejectedOutputEvent> producerTemplate;


    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        target = new OrderRejectedListener(producerTemplate,"TestTopic");
    }

    @Test
    public void shouldHandleOrderRejectedEvents(){

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleOrderRejectedEvent(new OrderRejectedEvent("TEST_ID","ERROR_MSG","CREATE_ORDER"));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));
    }
}
