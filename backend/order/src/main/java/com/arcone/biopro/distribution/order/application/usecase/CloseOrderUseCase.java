package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.domain.event.OrderCompletedEvent;
import com.arcone.biopro.distribution.order.domain.model.CloseOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CloseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloseOrderUseCase implements CloseOrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public Mono<UseCaseResponseDTO<Order>> closeOrder(CloseOrderCommand closeOrderCommand) {
        return this.orderRepository.findOneById(closeOrderCommand.getOrderId())
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s",closeOrderCommand.getOrderId()))))
            .flatMap(order -> {
                order.closeOrder(closeOrderCommand);
                return orderRepository.update(order);
            }).flatMap(orderClosed -> Mono.just(new UseCaseResponseDTO<>(List.of(UseCaseNotificationDTO
                .builder()
                .useCaseMessageType(UseCaseMessageType.ORDER_CLOSED_SUCCESSFULLY)
                .build()), orderClosed)))
            .doOnSuccess(this::publishOrderCompletedEvent)
            .onErrorResume(error -> {
                log.error("Not able to close order",error);
                return Mono.just(buildErrorResponse());
            });
    }

    private UseCaseResponseDTO<Order> buildErrorResponse(){
        return new UseCaseResponseDTO<>(List.of(UseCaseNotificationDTO
            .builder()
            .useCaseMessageType(UseCaseMessageType.CLOSE_ORDER_ERROR)
            .build()), null);
    }

    private void publishOrderCompletedEvent(UseCaseResponseDTO<Order> responseDTO) {
        log.debug("Publishing OrderCompletedEvent {} , ID {}", responseDTO.data(), responseDTO.data().getId());
        applicationEventPublisher.publishEvent(new OrderCompletedEvent(responseDTO.data()));
    }
}
