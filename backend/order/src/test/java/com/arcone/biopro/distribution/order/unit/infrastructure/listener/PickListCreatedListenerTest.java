package com.arcone.biopro.distribution.order.unit.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.PickListCreatedEvent;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.model.vo.PickListCustomer;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderFulfilledEventDTO;
import com.arcone.biopro.distribution.order.infrastructure.listener.PickListCreatedListener;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderFulfilledMapper;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class PickListCreatedListenerTest {


    @Test
    public void shouldHandlePickListCreatedEvents(){

        var pickList = Mockito.mock(PickList.class);
        var pickListCustomer = Mockito.mock(PickListCustomer.class);
        Mockito.when(pickListCustomer.getCode()).thenReturn("CODE");

        Mockito.when(pickList.getCustomer()).thenReturn(pickListCustomer);

        var orderRepository = Mockito.mock(OrderRepository.class);
        var customerService = Mockito.mock(CustomerService.class);
        var orderFulfilledMapper = Mockito.mock(OrderFulfilledMapper.class);

        Mockito.when(orderRepository.findOneByOrderNumber(Mockito.anyLong())).thenReturn(Mono.just(Mockito.mock(Order.class)));

        var customerDto = Mockito.mock(CustomerDTO.class);


        Mockito.when(customerService.getCustomerByCode(Mockito.any())).thenReturn(Mono.just(customerDto));

        Mockito.when(orderFulfilledMapper.buildOrderDetails(Mockito.any(),Mockito.any())).thenReturn(Mockito.mock(OrderFulfilledEventDTO.class));

        Mockito.when(orderFulfilledMapper.buildShippingCustomerDetails(Mockito.any())).thenReturn(Mono.just(Mockito.mock(OrderFulfilledEventDTO.class)));

        ReactiveKafkaProducerTemplate<String, OrderFulfilledEventDTO> producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        PickListCreatedListener target = new PickListCreatedListener(producerTemplate, "TestTopic", orderRepository, customerService, orderFulfilledMapper);


        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handlePickListCreatedEvent(new PickListCreatedEvent(pickList));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));

    }

}
