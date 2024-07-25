package com.arcone.biopro.distribution.partnerorderprovider.unit.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.PartnerOrder;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.OrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.PartnerOrderInboundReceivedListener;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class PartnerOrderInboundReceivedListenerTest {

    private PartnerOrderInboundReceivedListener target;

    private ReactiveKafkaProducerTemplate<String, OrderReceivedEvent> producerTemplate;


    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        target = new PartnerOrderInboundReceivedListener(producerTemplate,"TestTopic");
    }

    @Test
    public void shouldHandlePartnerOrderReceivedEvents(){
        var partnerOrder = Mockito.mock(PartnerOrder.class);

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleUserPartnerOrderReceivedEvent(new PartnerOrderInboundReceived(partnerOrder));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));
    }
}
