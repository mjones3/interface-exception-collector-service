package com.arcone.biopro.distribution.orderservice.infrastructure.listener;

import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.orderservice.application.dto.OrderReceivedEventPayloadDTO;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderReceivedListener implements CommandLineRunner {


    private final ReactiveKafkaConsumerTemplate<String, String> consumer;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    private final Scheduler scheduler = Schedulers.newBoundedElastic(
        16,
        128,
        "schedulers"
    );

    @Override
    public void run(String... args) throws Exception {
        consumeOrderReceived().publishOn(scheduler).subscribe();

    }


    private Flux<Order> consumeOrderReceived() {
        return consumer
            .receiveAutoAck()
            .doOnNext(
                consumerRecord ->
                    log.info(
                        "received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(),
                        consumerRecord.value(),
                        consumerRecord.topic(),
                        consumerRecord.offset()
                    )
            )
            .map(ConsumerRecord::value)
            .flatMap(this::handleMessage)
            .doOnNext(message -> log.info("successfully consumed {}={}", String.class.getSimpleName(), message))
            .doOnError(throwable -> log.error("something bad happened while consuming : {}", throwable.getMessage()));
    }

    private Mono<Order> handleMessage(String value) {
        try {
            var message = objectMapper.readValue(value, OrderReceivedEventDTO.class);
            log.info("Message Handled....{}",message);
            return orderService.processOrder(message.payload());
        } catch (JsonProcessingException e) {
            log.error(String.format("Problem deserializing an instance of [%s] " +
                "with the following json: %s ", OrderReceivedEventPayloadDTO.class.getSimpleName(), value), e);
            return Mono.error(new RuntimeException(e));
        }
    }
}
