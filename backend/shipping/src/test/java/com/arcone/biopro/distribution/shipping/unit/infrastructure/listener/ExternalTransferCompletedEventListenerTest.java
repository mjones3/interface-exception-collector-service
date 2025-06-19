package com.arcone.biopro.distribution.shipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ExternalTransferCompletedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.infrastructure.event.ExternalTransferCompletedOutputEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.ExternalTransferCompletedEventListener;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class ExternalTransferCompletedEventListenerTest {


    @Test
    public void shouldHandleShipmentCreatedEvents(){

        ReactiveKafkaProducerTemplate<String, ExternalTransferCompletedOutputEvent> producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);

        var payload = Mockito.mock(ExternalTransfer.class);
        var customer = Mockito.mock(Customer.class);
        Mockito.when(payload.getCustomerTo()).thenReturn(customer);
        Mockito.when(payload.getCustomerFrom()).thenReturn(customer);

        var target = new ExternalTransferCompletedEventListener(producerTemplate, "TestTopic");


        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleExternalTransferCompletedEvent(new ExternalTransferCompletedEvent(payload));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));

    }

}
