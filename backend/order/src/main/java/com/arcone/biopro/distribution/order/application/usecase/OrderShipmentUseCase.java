package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCreatedEvenPayloadDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderShipmentMapper;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.model.vo.ModifyByProcess;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.repository.OrderShipmentRepository;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderShipmentUseCase implements OrderShipmentService {

    private final OrderShipmentRepository orderShipmentRepository;
    private final OrderShipmentMapper orderShipmentMapper;
    private final OrderRepository orderRepository;
    private static final String ORDER_IN_PROGRESS_STATUS = "IN_PROGRESS";

    @Override
    @Transactional
    public Mono<OrderShipment> processShipmentCreatedEvent(ShipmentCreatedEvenPayloadDTO eventPayload) {
        return orderRepository.findOneByOrderNumber(eventPayload.orderNumber())
            .switchIfEmpty(Mono.error(new RuntimeException("Not able to find order by order number")))
            .map(order -> {
                    order.getOrderStatus().setStatus(ORDER_IN_PROGRESS_STATUS);
                    order.setModifiedByProcess(ModifyByProcess.SYSTEM.name());
                    return order;
                })
            .publishOn(Schedulers.boundedElastic())
            .flatMap(orderRepository::update)
            .then(orderShipmentRepository.insert(orderShipmentMapper.mapToDomain(eventPayload)));
    }

    @Override
    public Mono<OrderShipment> findOneByOrderId(Long orderId) {
        return orderShipmentRepository.findOneByOrderId(orderId);
    }
}
