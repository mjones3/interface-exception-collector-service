package com.arcone.biopro.distribution.shipping.infrastructure.listener;

import com.arcone.biopro.distribution.shipping.domain.event.ExternalTransferCompletedEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.shipping.infrastructure.event.ExternalTransferCompletedOutputEvent;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ExternalTransferCompletedOutputPayload;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ExternalTransferOutputItem;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static java.util.Optional.ofNullable;
import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Service
@Slf4j
@Profile("prod")
public class ExternalTransferCompletedEventListener {


    private final ReactiveKafkaProducerTemplate<String, ExternalTransferCompletedOutputEvent> producerTemplate;
    private final String topicName;

    public ExternalTransferCompletedEventListener(@Qualifier(KafkaConfiguration.EXTERNAL_TRANSFER_COMPLETED_PRODUCER)
                                     ReactiveKafkaProducerTemplate<String, ExternalTransferCompletedOutputEvent> producerTemplate,
                                     @Value("${topics.external-transfer.external-transfer-completed.topic-name:ExternalTransferCompleted}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ExternalTransferCompleted",
        description = "External Transfer Completed Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.event.ExternalTransferCompletedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "ExternalTransferCompleted",
            title = "ExternalTransferCompleted",
            description = "External Transfer Completed Event Payload"
        ),payloadType = ExternalTransferCompletedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding

    @EventListener
    public void handleExternalTransferCompletedEvent(ExternalTransferCompletedEvent event) {
        log.debug("External Transfer Completed event trigger Event {}", event);

        var payload = event.getPayload();

        var message = new ExternalTransferCompletedOutputEvent(ExternalTransferCompletedOutputPayload
            .builder()
            .customerCodeTo(payload.getCustomerTo().getCode())
            .customerCodeFrom(payload.getCustomerFrom().getCode())
            .hospitalTransferId(payload.getHospitalTransferId())
            .createdByEmployeeId(payload.getCreateEmployeeId())
            .createDate(payload.getCreateDate())
            .transferDate(payload.getTransferDate())
            .externalTransferItems( ofNullable(payload.getExternalTransferItems())
                .filter(items -> !items.isEmpty())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(lineItem -> ExternalTransferOutputItem
                    .builder()
                    .productCode(lineItem.getProduct().getProductCode())
                    .unitNumber(lineItem.getProduct().getUnitNumber())
                    .build()).toList())
            .build());

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
