package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.domain.event.OrderCompletedEvent;
import com.arcone.biopro.distribution.order.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.order.domain.exception.DomainException;
import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.*;
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
    private final CustomerService customerService;
    private final OrderConfigService orderConfigService;

    @Override
    @Transactional
    public Mono<UseCaseResponseDTO<Order>> completeOrder(CompleteOrderCommand completeOrderCommand) {
        return this.orderRepository.findOneById(completeOrderCommand.getOrderId())
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s", completeOrderCommand.getOrderId()))))
            .map(order -> {
                order.completeOrder(completeOrderCommand,lookupService,orderShipmentService);
                return order;
            })
            .flatMap(orderRepository::update)
            .map(order -> {
                return new UseCaseResponseDTO<>(List.of(UseCaseNotificationDTO
                    .builder()
                    .useCaseMessageType(UseCaseMessageType.ORDER_COMPLETED_SUCCESSFULLY)
                    .build()), order);
            })
            .doOnSuccess(this::publishOrderCompletedEvent)
            .doOnSuccess(orderUseCaseResponseDTO -> this.createBackOrder(orderUseCaseResponseDTO,completeOrderCommand))
            .publishOn(Schedulers.boundedElastic())
            .onErrorResume(error -> {
                log.error("Error occurred while completing order", error);
                return Mono.just(buildErrorResponse(error));
            });
    }

    private UseCaseResponseDTO<Order> buildErrorResponse(Throwable throwable) {
        var messageType = UseCaseMessageType.COMPLETE_ORDER_ERROR;
        if (throwable instanceof DomainException e) {
            messageType = e.getUseCaseMessageType();
        }

        return new UseCaseResponseDTO<>(List.of(UseCaseNotificationDTO
            .builder()
            .useCaseMessageType(messageType)
            .build()), null);
    }

    private void publishOrderCompletedEvent(UseCaseResponseDTO<Order> useCaseResponseDTO) {
        log.debug("Publishing OrderCompletedEvent {} , ID {}", useCaseResponseDTO, useCaseResponseDTO.data().getId());
        applicationEventPublisher.publishEvent(new OrderCompletedEvent(useCaseResponseDTO.data()));
    }

    private void createBackOrder(UseCaseResponseDTO<Order> useCaseResponseDTO,CompleteOrderCommand completeOrderCommand){
        if(Boolean.TRUE.equals(completeOrderCommand.getCreateBackOrder())){
            var backOrder = useCaseResponseDTO.data().createBackOrder(completeOrderCommand.getEmployeeId(),customerService,lookupService,orderConfigService);
            this.orderRepository.insert(backOrder)
                .doOnSuccess(this::publishOrderCreatedEvent)
                .subscribe();
        }
    }
    private void publishOrderCreatedEvent(Order order) {
        log.debug("Publishing OrderCreatedEvent {} , ID {}", order, order.getId());
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(order));
    }


}
