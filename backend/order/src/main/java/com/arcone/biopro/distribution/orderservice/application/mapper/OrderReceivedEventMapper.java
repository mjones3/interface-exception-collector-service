package com.arcone.biopro.distribution.orderservice.application.mapper;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Collections;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderReceivedEventMapper {

    private final CustomerService customerService;
    private final LookupService lookupService;
    private final OrderConfigService orderConfigService;

    public Mono<Order> mapToDomain(final OrderReceivedEventPayloadDTO orderReceivedEventPayloadDTO) {
        return Mono.fromCallable(() -> {
            log.info("Mapping OrderReceivedEventPayloadDTO to Order");
            var order =  new Order(
                this.customerService,
                this.lookupService,
                null,
                null,
                orderReceivedEventPayloadDTO.externalId(),
                orderReceivedEventPayloadDTO.locationCode(),
                orderReceivedEventPayloadDTO.shipmentType(),
                orderReceivedEventPayloadDTO.shippingMethod(),
                orderReceivedEventPayloadDTO.shippingCustomerCode(),
                orderReceivedEventPayloadDTO.billingCustomerCode(),
                LocalDate.parse(orderReceivedEventPayloadDTO.desiredShippingDate()),
                orderReceivedEventPayloadDTO.willPickUp(),
                orderReceivedEventPayloadDTO.willPickUpPhoneNumber(),
                orderReceivedEventPayloadDTO.productCategory(),
                orderReceivedEventPayloadDTO.comments(),
                orderReceivedEventPayloadDTO.orderStatus(),
                orderReceivedEventPayloadDTO.deliveryType(),
                orderReceivedEventPayloadDTO.createEmployeeCode(),
                null,
                null,
                null);

            ofNullable(orderReceivedEventPayloadDTO.orderItems())
                .filter(orderItems -> !orderItems.isEmpty())
                .orElseGet(Collections::emptyList)
                .forEach(orderItemDTO -> order.addItem(null
                        ,orderItemDTO.productFamily(),orderItemDTO.bloodType()
                        ,orderItemDTO.quantity(),orderItemDTO.comments(),null
                        ,null,this.orderConfigService
                    )
                );
            return order;
        }).publishOn(Schedulers.boundedElastic());
    }
}
