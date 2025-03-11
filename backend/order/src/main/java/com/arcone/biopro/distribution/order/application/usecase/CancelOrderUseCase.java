package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.domain.event.OrderCancelledEvent;
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
public class CancelOrderUseCase extends AbstractProcessOrderUseCase implements CancelOrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final static String USE_CASE_OPERATION = "CANCEL_ORDER";

    @Override
    public Mono<Void> processCancelOrderReceivedEvent(CancelOrderReceivedDTO cancelOrderReceivedDTO) {
        return this.orderRepository.findByExternalId(cancelOrderReceivedDTO.payload().externalId())
            .switchIfEmpty(Mono.error(new DomainNotFoundForKeyException(String.format("%s", cancelOrderReceivedDTO.payload().externalId()))))
            .collectList()
            .flatMap(orderList -> {
                var cancelCommand = new CancelOrderCommand(cancelOrderReceivedDTO.payload().externalId()
                    ,cancelOrderReceivedDTO.payload().cancelEmployeeCode()
                    , cancelOrderReceivedDTO.payload().cancelReason(), cancelOrderReceivedDTO.payload().cancelDate());
                var orderCancelled = orderList.getFirst().cancel(cancelCommand,orderList);
                return this.orderRepository.update(orderCancelled)
                    .doOnSuccess(this::publishOrderProcessedEvent);
            })
            .then()
            .onErrorResume(error -> {
                log.error("Not able to process order cancel event {}",error.getMessage());
                publishOrderRejectedEvent(applicationEventPublisher,cancelOrderReceivedDTO.payload().externalId(), error,USE_CASE_OPERATION);
                    return Mono.empty();
                }
            );
    }



    @Override
    void publishOrderProcessedEvent(Order order) {
        log.debug("Publishing OrderCancelledEvent {} ", order );
        applicationEventPublisher.publishEvent(new OrderCancelledEvent(order));
    }
}
