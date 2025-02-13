package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.CancelOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.CancelOrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.CancelOrderDTO;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
public class CancelOrderInboundReceivedListener {

    private final ReactiveKafkaProducerTemplate<String, CancelOrderReceivedEvent> producerTemplate;
    private final String topicName;

    public CancelOrderInboundReceivedListener(@Qualifier(KafkaConfiguration.ORDER_CANCEL_PRODUCER) ReactiveKafkaProducerTemplate<String, CancelOrderReceivedEvent> producerTemplate
        , @Value("${topics.cancel-order-received.name:CancelOrderReceived}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }


    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "CancelOrderReceived",
        description = "Cancel Order Received Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.CancelOrderReceivedEvent"
        )),
        message = @AsyncMessage(
            name = "CancelOrderReceived",
            title = "CancelOrderReceived",
            description = "Cancel Order Received Event"
        ),payloadType = com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.CancelOrderReceivedEvent.class
    ))
    @EventListener
    public void handleUserCancelOrderReceivedEvent(CancelOrderInboundReceived event) {
        log.debug("Cancel Order Received event trigger {}", event);

        var eventPayload = event.getPayload();

        var message = new CancelOrderReceivedEvent(CancelOrderDTO.builder()
            .externalId(eventPayload.getExternalId())
            .cancelReason(eventPayload.getCancelReason())
            .cancelEmployeeCode(eventPayload.getCancelEmployeeCode())
            .cancelDate(eventPayload.getCancelDate())
            .build());

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
