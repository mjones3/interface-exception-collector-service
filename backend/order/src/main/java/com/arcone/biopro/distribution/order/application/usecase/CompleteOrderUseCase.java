package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.domain.event.OrderCompletedEvent;
import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CompleteOrderService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompleteOrderUseCase implements CompleteOrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderShipmentService orderShipmentService;
    private final LookupService lookupService;

    @Override
    @Transactional
    public Mono<UseCaseResponseDTO<Order>> completeOrder(CompleteOrderCommand completeOrderCommand) {
        return this.orderRepository.findOneById(completeOrderCommand.getOrderId())
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s", completeOrderCommand.getOrderId()))))
            .map(order -> {
                order.completeOrder(completeOrderCommand,lookupService,orderShipmentService);
                return order;
            }).flatMap(orderRepository::update)
            .map(order -> {
                return new UseCaseResponseDTO<>(List.of(UseCaseNotificationDTO
                    .builder()
                    .useCaseMessageType(UseCaseMessageType.ORDER_COMPLETED_SUCCESSFULLY)
                    .build()), order);
            })
            .doOnSuccess(this::publishOrderCompletedEvent)
            .publishOn(Schedulers.boundedElastic())
            .onErrorResume(error -> {
                log.error("Error occurred while completing order", error);
                return Mono.just(buildErrorResponse());
            });
    }

    private UseCaseResponseDTO<Order> buildErrorResponse(){
        return new UseCaseResponseDTO<>(List.of(UseCaseNotificationDTO
            .builder()
            .useCaseMessageType(UseCaseMessageType.COMPLETE_ORDER_ERROR)
            .build()), null);
    }

    private void publishOrderCompletedEvent(UseCaseResponseDTO<Order> useCaseResponseDTO) {
        log.debug("Publishing OrderCompletedEvent {} , ID {}", useCaseResponseDTO, useCaseResponseDTO.data().getId());
        applicationEventPublisher.publishEvent(new OrderCompletedEvent(useCaseResponseDTO.data()));
    }
}
