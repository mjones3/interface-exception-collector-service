package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedEventPayloadDTO;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.repository.OrderShipmentRepository;
import com.arcone.biopro.distribution.order.domain.service.ShipmentCompletedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentCompletedUseCase implements ShipmentCompletedService {

    private final OrderRepository orderRepository;
    private final OrderShipmentRepository orderShipmentRepository;
    private static final String ORDER_SHIPMENT_STATUS_COMPLETED = "COMPLETED";
    private static final String ORDER_STATUS_COMPLETED = "COMPLETED";

    @Override
    @Transactional
    public Mono<Void> processCompletedShipmentEvent(ShipmentCompletedEventPayloadDTO shipmentCompletedEventPayloadDTO) {
        return orderRepository.findOneByOrderNumber(shipmentCompletedEventPayloadDTO.orderNumber())
            .switchIfEmpty(Mono.error(new RuntimeException("Not able to find order by order number")))
            .map(order -> setShippedQuantity(order,shipmentCompletedEventPayloadDTO))
            .publishOn(Schedulers.boundedElastic())
            .flatMap(orderRepository::update)
            .flatMap(this::completeOrderShipment)
            .then(Mono.empty());
    }


    private Order setShippedQuantity(Order order , ShipmentCompletedEventPayloadDTO payloadDTO){
        if(order.getOrderItems() != null && !order.getOrderItems().isEmpty()){
            order.getOrderItems().forEach(orderItem -> {
                if(orderItem.getBloodType().getBloodType().equals(payloadDTO.bloodType())
                    && orderItem.getProductFamily().getProductFamily().equals(payloadDTO.productFamily())){
                    orderItem.defineShippedQuantity(orderItem.getQuantityShipped() + 1);
                    if (order.isCompleted()) {
                        log.debug("Order {} already completed (ShipmentCompletedUseCase)", order.getOrderNumber());
                        order.getOrderStatus().setStatus(ORDER_STATUS_COMPLETED);
                    }
                }
            });
        }

        return order;

    }

    private Mono<OrderShipment> completeOrderShipment(Order order){
        return orderShipmentRepository.findOneByOrderId(order.getId())
            .map(orderShipment -> {
                orderShipment.setShipmentStatus(ORDER_SHIPMENT_STATUS_COMPLETED);
                return orderShipment;
            }).flatMap(orderShipmentRepository::update);
    }
}
