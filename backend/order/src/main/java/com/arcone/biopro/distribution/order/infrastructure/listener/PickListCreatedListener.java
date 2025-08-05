package com.arcone.biopro.distribution.order.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.PickListCreatedEvent;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderFulfilledEventDTO;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderFulfilledMapper;
import com.arcone.biopro.distribution.order.infrastructure.persistence.LocationEntityRepository;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
@Profile("prod")
public class PickListCreatedListener {


    private final ReactiveKafkaProducerTemplate<String, OrderFulfilledEventDTO> producerTemplate;
    private final String topicName;
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final OrderFulfilledMapper orderFulfilledMapper;
    private final LocationEntityRepository locationRepository;

    public PickListCreatedListener(@Qualifier(KafkaConfiguration.ORDER_FULFILLED_PRODUCER) ReactiveKafkaProducerTemplate<String, OrderFulfilledEventDTO> producerTemplate,
                                @Value("${topics.order.order-fulfilled.topic-name:OrderFulfilled}") String topicName
        , OrderRepository orderRepository
                                   , CustomerService customerService , OrderFulfilledMapper orderFulfilledMapper,LocationEntityRepository locationRepository
    ) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.orderFulfilledMapper = orderFulfilledMapper;
        this.locationRepository = locationRepository;
    }


    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "OrderFulfilled",
        description = "Order Fulfilled Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.order.infrastructure.event.OrderFulfilledEventDTO"
        )),
        message = @AsyncMessage(
            name = "OrderFulfilled",
            title = "OrderFulfilled",
            description = "Order Fulfilled Event Payload"
        ),payloadType = OrderFulfilledEventDTO.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public void handlePickListCreatedEvent(PickListCreatedEvent event) {
        log.debug("Pick List Created event trigger Event {}", event);

        var picklist = event.getPayload();

        orderRepository.findOneByOrderNumber(picklist.getOrderNumber())
            .map((Order order) -> orderFulfilledMapper.buildOrderDetails(order,picklist))
            .flatMap(orderFulfilledEventDTO -> {
                if("INTERNAL_TRANSFER".equals(picklist.getShipmentType())){
                    return locationRepository.findByCode(picklist.getCustomer().getCode())
                        .flatMap(locationEntity -> orderFulfilledMapper.buildShippingCustomerDetailsFromLocation(orderFulfilledEventDTO,locationEntity));
                }else{
                    return customerService.getCustomerByCode(picklist.getCustomer().getCode())
                        .flatMap(customerDTO -> orderFulfilledMapper.buildShippingCustomerDetails(orderFulfilledEventDTO,customerDTO));
                }
            })
            .map(orderFulfilleEventDTO -> new ProducerRecord<>(topicName, String.format("%s", event.getEventId()), orderFulfilleEventDTO))
            .flatMap(producerTemplate::send)
            .log()
            .subscribe();

    }

}
