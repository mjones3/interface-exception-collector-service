package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.domain.event.OrderCancelledEvent;
import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.domain.model.CancelOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CancelOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CancelOrderUseCase implements CancelOrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Mono<Void> processCancelOrderReceivedEvent(CancelOrderReceivedDTO cancelOrderReceivedDTO) {
        return this.orderRepository.findByExternalId(cancelOrderReceivedDTO.payload().externalId())
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s", cancelOrderReceivedDTO.payload().externalId()))))
            .flatMap(order -> {
                var cancelCommand = new CancelOrderCommand(cancelOrderReceivedDTO.payload().externalId()
                    ,cancelOrderReceivedDTO.payload().cancelDate()
                    , cancelOrderReceivedDTO.payload().cancelReason());
                order.cancel(cancelCommand);
                return this.orderRepository.update(order)
                    .doOnSuccess(this::publishOrderCancelledEvent);
            }).collectList()
            .then()
            .onErrorResume(error -> {
                log.error("Not able to process order cancel event {}",error.getMessage());
                publishOrderRejectedEvent(cancelOrderReceivedDTO.payload().externalId(), error.getMessage());
                    return Mono.empty();
                }
            );
    }

    private void publishOrderRejectedEvent(String externalId, String errorMessage) {
        log.debug("Publishing OrderRejected {} , ID {}", externalId, errorMessage);
        applicationEventPublisher.publishEvent(new OrderRejectedEvent(externalId, errorMessage));
    }

    private void publishOrderCancelledEvent(Order order) {
        log.debug("Publishing OrderCancelledEvent {} ", order );
        applicationEventPublisher.publishEvent(new OrderCancelledEvent(order));
    }
}
