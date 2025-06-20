package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.OrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderItemDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.FacilityServiceMock;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
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
public class PartnerOrderInboundReceivedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderReceivedEvent> producerTemplate;
    private final String topicName;
    private final FacilityServiceMock facilityServiceMock;

    public PartnerOrderInboundReceivedListener(@Qualifier(KafkaConfiguration.ORDER_RECEIVED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderReceivedEvent> producerTemplate, @Value("${topics.order-received.name:OrderReceived}") String topicName , FacilityServiceMock facilityServiceMock) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.facilityServiceMock = facilityServiceMock;
    }
    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderReceived",
        description = "Order Received Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.OrderReceived"
        )),
        message = @AsyncMessage(
            name = "OrderReceived",
            title = "OrderReceived",
            description = "Order Received Event"
        ),payloadType = com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.OrderReceivedEvent.class
    ))
    @KafkaAsyncOperationBinding

    @EventListener
    public void handleUserPartnerOrderReceivedEvent(PartnerOrderInboundReceived event) {
        log.info("Partner Order Received event trigger {}",event);
        var partnerOrder = event.getPayload();
        var facility = facilityServiceMock.getFacilityByExternalCode(partnerOrder.getLocationCode());
        var message = new OrderReceivedEvent(OrderDTO
            .builder()
            .transactionId(partnerOrder.getId())
            .orderStatus(partnerOrder.getOrderStatus())
            .createDate(partnerOrder.getCreateDate())
            .externalId(partnerOrder.getExternalId())
            .locationCode(facility.code())
            .createEmployeeCode(partnerOrder.getCreateEmployeeCode())
            .productCategory(partnerOrder.getProductCategory())
            .comments(partnerOrder.getComments())
            .billingCustomerCode(partnerOrder.getBillingCustomerCode())
            .shippingCustomerCode(partnerOrder.getShippingCustomerCode())
            .shippingMethod(partnerOrder.getShippingMethod())
            .deliveryType(partnerOrder.getDeliveryType())
            .shipmentType(partnerOrder.getShipmentType())
            .desiredShippingDate(partnerOrder.getDesiredShippingDate())
            .orderStatus(partnerOrder.getOrderStatus())
            .comments(partnerOrder.getComments())
            .willPickUp(Objects.nonNull(partnerOrder.getPartnerOrderPickUpType()))
            .willPickUpPhoneNumber(Objects.nonNull(partnerOrder.getPartnerOrderPickUpType()) ? partnerOrder.getPartnerOrderPickUpType().getPhoneNumber(): null)
            .orderItems(partnerOrder.getOrderItems().stream()
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
