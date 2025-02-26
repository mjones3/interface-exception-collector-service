package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.ModifyOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.ModifyOrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.ModifyOrderDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderItemDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.FacilityServiceMock;
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

import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
public class ModifyOrderInboundReceivedListener {

    private final ReactiveKafkaProducerTemplate<String, ModifyOrderReceivedEvent> producerTemplate;
    private final String topicName;
    private final FacilityServiceMock facilityServiceMock;

    public ModifyOrderInboundReceivedListener(@Qualifier(KafkaConfiguration.MODIFY_ORDER_PRODUCER) ReactiveKafkaProducerTemplate<String, ModifyOrderReceivedEvent> producerTemplate
        , @Value("${topics.modify-order-received.name:ModifyOrderReceived}") String topicName , FacilityServiceMock facilityServiceMock) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.facilityServiceMock = facilityServiceMock;
    }


    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ModifyOrderReceived",
        description = "Modify Order Received Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.ModifyOrderReceivedEvent"
        )),
        message = @AsyncMessage(
            name = "ModifyOrderReceived",
            title = "ModifyOrderReceived",
            description = "Modify Order Received Event"
        ),payloadType = ModifyOrderReceivedEvent.class
    ))
    @EventListener
    public void handleModifyOrderReceivedEvent(ModifyOrderInboundReceived event) {
        log.debug("Modify Order Received event trigger {}", event);

        var eventPayload = event.getPayload();
        var facility = facilityServiceMock.getFacilityByExternalCode(eventPayload.getLocationCode());

        var message = new ModifyOrderReceivedEvent(ModifyOrderDTO
            .builder()
                .externalId(eventPayload.getExternalId())
                .locationCode(facility.code())
                .modifyDate(eventPayload.getModifyDate())
                .modifyEmployeeCode(eventPayload.getModifyEmployeeCode())
                .productCategory(eventPayload.getProductCategory())
                .comments(eventPayload.getComments())
                .shippingMethod(eventPayload.getShippingMethod())
                .deliveryType(eventPayload.getDeliveryType())
                .desiredShippingDate(eventPayload.getDesiredShippingDate())
                .comments(eventPayload.getComments())
                .willPickUp(Objects.nonNull(eventPayload.getPartnerOrderPickUpType()))
                .willPickUpPhoneNumber(Objects.nonNull(eventPayload.getPartnerOrderPickUpType()) ? eventPayload.getPartnerOrderPickUpType().getPhoneNumber(): null)
                .orderItems(eventPayload.getOrderItems().stream()
                    .map(partnerItem -> OrderItemDTO
                        .builder()
                        .productFamily(partnerItem.getProductFamily())
                        .quantity(partnerItem.getQuantity())
                        .bloodType(partnerItem.getBloodType())
                        .comments(partnerItem.getComments())
                        .build())
                    .collect(Collectors.toList()))
                .build());

        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", message.getEventId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }
}
