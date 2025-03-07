package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.domain.event.OrderRejectedEvent;
import com.arcone.biopro.distribution.order.domain.exception.DomainException;
import com.arcone.biopro.distribution.order.domain.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public abstract class AbstractProcessOrderUseCase {

    public void publishOrderRejectedEvent(ApplicationEventPublisher applicationEventPublisher, String externalId, Throwable error , String operation) {
        log.debug("Publishing OrderRejected {} , ID {}", externalId, error.getMessage());
        var errorMessage = "";
        if(error instanceof DomainException de){
            errorMessage = de.getUseCaseMessageType().getMessage();
        }else{
            errorMessage = error.getMessage();
        }

        log.debug("Rejected Reason : {}",errorMessage);

        applicationEventPublisher.publishEvent(new OrderRejectedEvent(externalId, errorMessage,operation));
    }

    abstract void publishOrderProcessedEvent(Order order);


}
