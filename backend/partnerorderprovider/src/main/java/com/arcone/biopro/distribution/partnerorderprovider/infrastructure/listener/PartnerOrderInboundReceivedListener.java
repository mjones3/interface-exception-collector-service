package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.OrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderItemDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.service.FacilityServiceMock;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PartnerOrderInboundReceivedListener {

    private final ReactiveKafkaProducerTemplate<String, OrderReceivedEvent> producerTemplate;
    private final String topicName;
    private final FacilityServiceMock facilityServiceMock;

    public PartnerOrderInboundReceivedListener(ReactiveKafkaProducerTemplate<String, OrderReceivedEvent> producerTemplate,  @Value("${topics.order-received.name:OrderReceived}") String topicName ,FacilityServiceMock facilityServiceMock) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.facilityServiceMock = facilityServiceMock;
    }

    @EventListener
    public void handleUserPartnerOrderReceivedEvent(PartnerOrderInboundReceived event) {
        log.info("Partner Order Received event trigger {}",event);
        var partnerOrder = event.partnerOrder();
        var facility = facilityServiceMock.getFacilityByExternalCode(partnerOrder.getLocationCode());
        var message = new OrderReceivedEvent(OrderDTO
            .builder()
            .id(partnerOrder.getId())
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
        var producerRecord = new ProducerRecord<>(topicName, String.format("%s", event.partnerOrder().getId()), message);
        producerTemplate.send(producerRecord)
            .log()
            .subscribe();
    }

}
