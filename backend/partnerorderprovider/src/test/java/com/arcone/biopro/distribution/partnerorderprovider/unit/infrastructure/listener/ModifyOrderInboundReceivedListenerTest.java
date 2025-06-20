package com.arcone.biopro.distribution.partnerorderprovider.unit.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.ModifyOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.domain.model.ModifyOrder;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.CancelOrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.ModifyOrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.ModifyOrderInboundReceivedListener;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.FacilityServiceMock;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.dto.FacilityDTO;
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

class ModifyOrderInboundReceivedListenerTest {

    private ModifyOrderInboundReceivedListener target;

    private ReactiveKafkaProducerTemplate<String, ModifyOrderReceivedEvent> producerTemplate;
    private FacilityServiceMock facilityServiceMock;


    @BeforeEach
    public void setUp() {
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        facilityServiceMock = Mockito.mock(FacilityServiceMock.class);
        target = new ModifyOrderInboundReceivedListener(producerTemplate, "TestTopic", facilityServiceMock);
    }

    @Test
    public void shouldHandleModifyOrderReceivedEvents() {
        var modifyOrder = Mockito.mock(ModifyOrder.class);
        Mockito.when(modifyOrder.getId()).thenReturn(UUID.randomUUID());

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        var facilityMock = Mockito.mock(FacilityDTO.class);

        Mockito.when(facilityServiceMock.getFacilityByExternalCode(Mockito.any())).thenReturn(facilityMock);

        ArgumentCaptor<ProducerRecord> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);

        target.handleModifyOrderReceivedEvent(new ModifyOrderInboundReceived(modifyOrder));

        Mockito.verify(producerTemplate).send(recordCaptor.capture());

        ProducerRecord<String, ModifyOrderReceivedEvent> capturedRecord = recordCaptor.getValue();

        ModifyOrderReceivedEvent event = capturedRecord.value();

        assertEquals(modifyOrder.getId().toString(), event.getPayload().transactionId().toString());
    }

}
