package com.arcone.biopro.distribution.orderservice.unit.infrastructure.listener;

import com.arcone.biopro.distribution.orderservice.domain.event.OrderCreatedEvent;
import com.arcone.biopro.distribution.orderservice.domain.model.Order;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderCustomer;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderExternalId;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderNumber;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ShipmentType;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ShippingMethod;
import com.arcone.biopro.distribution.orderservice.infrastructure.dto.OrderCreatedDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.listener.OrderCreatedListener;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class OrderCreatedListenerTest {

    private OrderCreatedListener target;

    private ReactiveKafkaProducerTemplate<String, OrderCreatedDTO> producerTemplate;


    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        target = new OrderCreatedListener(producerTemplate,"TestTopic");
    }

    @Test
    public void shouldHandleOrderCreatedEvents(){
        var order = Mockito.mock(Order.class);
        Mockito.when(order.getOrderStatus()).thenReturn(Mockito.mock(OrderStatus.class));
        Mockito.when(order.getOrderNumber()).thenReturn(Mockito.mock(OrderNumber.class));
        Mockito.when(order.getOrderExternalId()).thenReturn(Mockito.mock(OrderExternalId.class));
        Mockito.when(order.getOrderPriority()).thenReturn(Mockito.mock(OrderPriority.class));
        Mockito.when(order.getShipmentType()).thenReturn(Mockito.mock(ShipmentType.class));
        Mockito.when(order.getProductCategory()).thenReturn(Mockito.mock(ProductCategory.class));
        Mockito.when(order.getShippingMethod()).thenReturn(Mockito.mock(ShippingMethod.class));
        Mockito.when(order.getBillingCustomer()).thenReturn(Mockito.mock(OrderCustomer.class));
        Mockito.when(order.getShippingCustomer()).thenReturn(Mockito.mock(OrderCustomer.class));

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleOrderCreatedEvent(new OrderCreatedEvent(order));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));
    }
}
