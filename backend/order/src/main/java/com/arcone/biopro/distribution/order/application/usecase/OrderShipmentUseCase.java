package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderShipmentMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.repository.OrderShipmentRepository;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderShipmentUseCase implements OrderShipmentService {

    private final OrderShipmentRepository orderShipmentRepository;
    private final OrderShipmentMapper orderShipmentMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Mono<OrderShipment> processShipmentCreatedEvent(ShipmentCreatedEvenPayloadDTO eventPayload) {
        return orderRepository.findOneByOrderNumber(eventPayload.orderNumber())
            .switchIfEmpty(Mono.error(new RuntimeException(STR."Not able to find order by order number \{eventPayload.orderNumber()}")))
            .map(order -> {
                order.getOrderStatus().setStatus("IN_PROGRESS");
                return orderRepository.update(order);
                })
            .then(orderShipmentRepository.insert(orderShipmentMapper.mapToDomain(eventPayload)));
    }
}
