package com.arcone.biopro.distribution.order.unit.infrastructure.listener;

import com.arcone.biopro.distribution.order.domain.event.OrderModifiedEvent;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderExternalId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderLocation;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderNumber;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.order.domain.model.vo.ShipmentType;
import com.arcone.biopro.distribution.order.domain.model.vo.ShippingMethod;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderModifiedOutputEvent;
import com.arcone.biopro.distribution.order.infrastructure.listener.OrderModifiedListener;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

class OrderModifiedListenerTest {


    private OrderModifiedListener target;

    private ReactiveKafkaProducerTemplate<String, OrderModifiedOutputEvent> producerTemplate;


    @BeforeEach
    public void setUp(){
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        target = new OrderModifiedListener(producerTemplate,"TestTopic");
    }

    @Test
    public void shouldHandleOrderCompletedEvents(){

        var orderLocation = Mockito.mock(OrderLocation.class);
        Mockito.when(orderLocation.getCode()).thenReturn("LOCATION_CODE");

        var order = Mockito.mock(Order.class);
        Mockito.when(order.getLocationFrom()).thenReturn(orderLocation);


        Mockito.when(order.getOrderStatus()).thenReturn(Mockito.mock(OrderStatus.class));
        Mockito.when(order.getOrderNumber()).thenReturn(Mockito.mock(OrderNumber.class));
        Mockito.when(order.getOrderExternalId()).thenReturn(Mockito.mock(OrderExternalId.class));
        Mockito.when(order.getOrderPriority()).thenReturn(Mockito.mock(OrderPriority.class));
        Mockito.when(order.getShipmentType()).thenReturn(Mockito.mock(ShipmentType.class));
        Mockito.when(order.getProductCategory()).thenReturn(Mockito.mock(ProductCategory.class));
        Mockito.when(order.getShippingMethod()).thenReturn(Mockito.mock(ShippingMethod.class));
        Mockito.when(order.getBillingCustomer()).thenReturn(Mockito.mock(OrderCustomer.class));
        Mockito.when(order.getShippingCustomer()).thenReturn(Mockito.mock(OrderCustomer.class));
        Mockito.when(order.getTransactionId()).thenReturn(java.util.UUID.randomUUID());

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        target.handleOrderModifiedEvent(new OrderModifiedEvent(order));

        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));

    }

}
