package com.arcone.biopro.distribution.shippingservice.application.usecase;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulFilledItemResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulFilledResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.OrderFulfilledDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.domain.model.Order;
import com.arcone.biopro.distribution.shippingservice.domain.model.OrderItem;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import com.arcone.biopro.distribution.shippingservice.domain.repository.OrderItemRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.shippingservice.domain.service.OrderFulfilledService;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFulfilledServiceUseCase implements OrderFulfilledService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Mono<Order> create(OrderFulfilledMessage message) {

        log.info("Creating order with number {}", message.orderNumber());
        var order = Order.builder()
            .orderNumber(message.orderNumber())
            .status(OrderStatus.valueOf(message.status()))
            .priority(OrderPriority.valueOf(message.priority()))
            .deliveryType(message.deliveryType())
            .billingCustomerCode(message.billingCustomerCode())
            .shippingDate(message.shippingDate())
            .shippingMethod(message.shippingMethod())
            .billingCustomerName(message.billingCustomerName())
            .shippingCustomerCode(message.shippingCustomerCode())
            .shippingCustomerName(message.shippingCustomerName())
            .locationCode(message.locationCode())
            .productCategory(message.productCategory())
            .customerAddressAddressLine1(message.customerAddressAddressLine1())
            .customerAddressAddressLine2(message.customerAddressAddressLine2())
            .customerAddressCity(message.customerAddressCity())
            .customerPhoneNumber(message.customerPhoneNumber())
            .customerAddressCountry(message.customerAddressCountry())
            .customerAddressCountryCode(message.customerAddressCountryCode())
            .customerAddressPostalCode(message.customerAddressPostalCode())
            .customerAddressState(message.customerAddressState())
            .customerAddressDistrict(message.customerAddressDistrict())
            .build();


        return
            orderRepository.save(order)
                .flatMap(savedOrder ->
                    orderItemRepository.saveAll(convertOrderItems(message, order))
                        .then(Mono.just(order)));
    }

    private Flux<OrderItem> convertOrderItems(OrderFulfilledMessage message, Order parentOrder) {

        return Flux.fromIterable(message.items().stream().map(orderItem -> OrderItem.builder()
            .orderId(parentOrder.getId())
            .quantity(orderItem.quantity())
            .bloodType(BloodType.valueOf(orderItem.bloodType()))
            .productFamily(orderItem.productFamily())
            .comments(orderItem.comments())
            .build()).collect(Collectors.toList()));
    }

    @WithSpan("listOrderFulfilledRequests")
    public Mono<List<OrderFulFilledResponseDTO>> listOrderFulfilledRequests() {
        log.info("Listing Pending order requests.....");
        return orderRepository.findAll().switchIfEmpty(Flux.empty()).flatMap(order -> convertOrderResponse(order)).collectList();
    }

    private Mono<OrderFulFilledResponseDTO> convertOrderResponse(Order order) {

        return Mono.just(OrderFulFilledResponseDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .status(order.getStatus())
            .priority(order.getPriority())
            .createDate(order.getCreateDate())
            .build());
    }

    private Mono<OrderFulfilledDetailResponseDTO> convertOrderResponseDetail(Order order) {

        List<OrderFulFilledItemResponseDTO> orderItemList = new ArrayList<>();

        return orderItemRepository.findAllByOrderId(order.getId())
            .map(orderItem -> orderItemList.add(OrderFulFilledItemResponseDTO.builder()
                .id(orderItem.getId())
                .productFamily(orderItem.getProductFamily())
                .quantity(orderItem.getQuantity())
                .orderId(orderItem.getOrderId())
                .comments(orderItem.getComments())
                .bloodType(orderItem.getBloodType())
                .build()))
            .then(Mono.just(OrderFulfilledDetailResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .priority(order.getPriority())
                .createDate(order.getCreateDate())
                .deliveryType(order.getDeliveryType())
                .billingCustomerCode(order.getBillingCustomerCode())
                .shippingDate(order.getShippingDate())
                .shippingMethod(order.getShippingMethod())
                .billingCustomerName(order.getBillingCustomerName())
                .shippingCustomerCode(order.getShippingCustomerCode())
                .shippingCustomerName(order.getShippingCustomerName())
                .locationCode(order.getLocationCode())
                .productCategory(order.getProductCategory())
                .customerAddressAddressLine1(order.getCustomerAddressAddressLine1())
                .customerAddressAddressLine2(order.getCustomerAddressAddressLine2())
                .customerAddressCity(order.getCustomerAddressCity())
                .customerPhoneNumber(order.getCustomerPhoneNumber())
                .customerAddressCountry(order.getCustomerAddressCountry())
                .customerAddressCountryCode(order.getCustomerAddressCountryCode())
                .customerAddressPostalCode(order.getCustomerAddressPostalCode())
                .customerAddressState(order.getCustomerAddressState())
                .customerAddressDistrict(order.getCustomerAddressDistrict())
                .items(orderItemList)
                .build()));


    }

    @WithSpan("getOrderByNumber")
    public Mono<OrderFulfilledDetailResponseDTO> getOrderByNumber(Long orderNumber) {
        log.info("getting order detail by Number {}.....", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber).switchIfEmpty(Mono.empty()).flatMap(order -> convertOrderResponseDetail(order));
    }
}
