package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener;

import com.arcone.biopro.distribution.partnerorderprovider.domain.event.PartnerOrderInboundReceived;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event.OrderReceivedEvent;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderItemDTO;
import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderPickUpTypeDTO;
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

    public PartnerOrderInboundReceivedListener(ReactiveKafkaProducerTemplate<String, OrderReceivedEvent> producerTemplate,  @Value("${topics.order-received.name:OrderReceived}") String topicName) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
    }

    @EventListener
    public void handleUserPartnerOrderReceivedEvent(PartnerOrderInboundReceived event) {
        log.info("Partner Order Received event trigger {}",event);
        var partnerOrder = event.partnerOrder();
        var message = new OrderReceivedEvent(OrderDTO
            .builder()
            .id(partnerOrder.getId())
            .orderStatus(partnerOrder.getOrderStatus())
            .createDate(partnerOrder.getCreateDate())
            .externalId(partnerOrder.getExternalId())
            .locationCode(partnerOrder.getLocationCode())
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
            .orderPickUpType(Objects.nonNull(partnerOrder.getPartnerOrderPickUpType()) ? OrderPickUpTypeDTO
                .builder()
                .willCallPickUp(partnerOrder.getPartnerOrderPickUpType().isWillCallPickUp())
                .phoneNumber(partnerOrder.getPartnerOrderPickUpType().getPhoneNumber())
                .build() : null)
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
