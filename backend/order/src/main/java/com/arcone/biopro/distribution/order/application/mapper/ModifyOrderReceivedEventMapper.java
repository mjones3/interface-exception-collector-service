package com.arcone.biopro.distribution.order.application.mapper;

import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedPayloadDTO;
import com.arcone.biopro.distribution.order.domain.model.ModifyOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.ModifyOrderItem;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.vo.ModifyByProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModifyOrderReceivedEventMapper {


    public ModifyOrderCommand mapToCommand(final ModifyOrderReceivedPayloadDTO modifyOrderReceivedPayloadDTO , ModifyByProcess modifyByProcess) {

        var command =  ModifyOrderCommand.builder()
            .externalId(modifyOrderReceivedPayloadDTO.externalId())
            .locationCode(modifyOrderReceivedPayloadDTO.locationCode())
            .modifyEmployeeCode(modifyOrderReceivedPayloadDTO.modifyEmployeeCode())
            .modifyByProcess(modifyByProcess)
            .modifyDate(modifyOrderReceivedPayloadDTO.modifyDate())
            .modifyReason(modifyOrderReceivedPayloadDTO.modifyReason())
            .productCategory(modifyOrderReceivedPayloadDTO.productCategory())
            .shippingMethod(modifyOrderReceivedPayloadDTO.shippingMethod())
            .comments(modifyOrderReceivedPayloadDTO.comments())
            .deliveryType(modifyOrderReceivedPayloadDTO.deliveryType())
            .desiredShippingDate(modifyOrderReceivedPayloadDTO.desiredShippingDate())
            .willPickUp(modifyOrderReceivedPayloadDTO.willPickUp())
            .orderItems(new ArrayList<>())
            .willPickUpPhoneNumber(modifyOrderReceivedPayloadDTO.willPickUpPhoneNumber())
            .transactionId(modifyOrderReceivedPayloadDTO.transactionId())
            .quarantinedProducts(modifyOrderReceivedPayloadDTO.quarantinedProducts())
            .shipToLocationCode(modifyOrderReceivedPayloadDTO.shipToLocationCode())
            .build();

        ofNullable(modifyOrderReceivedPayloadDTO.orderItems())
            .filter(orderItems -> !orderItems.isEmpty())
            .orElseGet(Collections::emptyList)
            .forEach(orderItemDTO ->
                command.getOrderItems().add(ModifyOrderItem
                .builder()
                .quantity(orderItemDTO.quantity())
                .comment(orderItemDTO.comments())
                .productFamily(orderItemDTO.productFamily())
                .bloodType(orderItemDTO.bloodType())
                .build()
            ));

        return  command;

    }
}
