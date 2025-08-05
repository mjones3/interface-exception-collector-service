package com.arcone.biopro.exceptioncollector.infrastructure.listener;

import com.arcone.biopro.exceptioncollector.application.usecase.ExceptionCollectorUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterfaceEventListener {

    private final ExceptionCollectorUseCase exceptionCollectorUseCase;

    @KafkaListener(topics = "OrderRejected")
    public void handleOrderRejected(@Payload JsonNode event, 
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received OrderRejected event from topic: {}", topic);
        exceptionCollectorUseCase.collectOrderRejection(event);
    }

    @KafkaListener(topics = "OrderCancelled")
    public void handleOrderCancelled(@Payload JsonNode event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received OrderCancelled event from topic: {}", topic);
        exceptionCollectorUseCase.collectOrderCancellation(event);
    }

    @KafkaListener(topics = "CollectionRejected")
    public void handleCollectionRejected(@Payload JsonNode event,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received CollectionRejected event from topic: {}", topic);
        exceptionCollectorUseCase.collectCollectionRejection(event);
    }

    @KafkaListener(topics = "DistributionFailed")
    public void handleDistributionFailed(@Payload JsonNode event,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received DistributionFailed event from topic: {}", topic);
        exceptionCollectorUseCase.collectDistributionFailure(event);
    }
}
