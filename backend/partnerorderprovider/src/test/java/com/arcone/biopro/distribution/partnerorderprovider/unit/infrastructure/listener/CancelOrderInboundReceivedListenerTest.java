package com.arcone.biopro.distribution.partnerorderprovider.unit.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.CancelOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.CancelOrder;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.CancelOrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.CancelOrderInboundReceivedListener;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CancelOrderInboundReceivedListenerTest {


    private CancelOrderInboundReceivedListener target;

    private ReactiveKafkaProducerTemplate<String, CancelOrderReceivedEvent> producerTemplate;



    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        target = new CancelOrderInboundReceivedListener(producerTemplate,"TestTopic");
    }

    @Test
    public void shouldHandleCancelOrderReceivedEvents(){
        var cancelOrder = Mockito.mock(CancelOrder.class);
        Mockito.when(cancelOrder.getId()).thenReturn(UUID.randomUUID());

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        ArgumentCaptor<ProducerRecord> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);

        target.handleUserCancelOrderReceivedEvent(new CancelOrderInboundReceived(cancelOrder));

        Mockito.verify(producerTemplate).send(recordCaptor.capture());

        ProducerRecord<String, CancelOrderReceivedEvent> capturedRecord = recordCaptor.getValue();

        CancelOrderReceivedEvent event = capturedRecord.value();

        assertEquals(cancelOrder.getId().toString(), event.getPayload().getTransactionId().toString());


    }

}
