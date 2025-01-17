package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedItemPayload;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedItemProductPayload;
import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.order.application.usecase.ShipmentCompletedUseCase;
import com.arcone.biopro.distribution.order.domain.event.OrderCompletedEvent;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductFamily;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.repository.OrderShipmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@SpringJUnitConfig
class ShipmentCompletedUseCaseTest {

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void shouldProcessShipmentCompletedEvent(){

        var orderRepository =  Mockito.mock(OrderRepository.class) ;
        var orderShipmentRepository = Mockito.mock(OrderShipmentRepository.class);

        var target = new ShipmentCompletedUseCase(orderRepository,orderShipmentRepository,applicationEventPublisher);

        var order = Mockito.mock(Order.class);

        Mockito.when(order.getId()).thenReturn(1L);
        Mockito.when(order.getTotalRemaining()).thenReturn(0);
        Mockito.when(order.isCompleted()).thenReturn(true);

        var oderItem = Mockito.mock(OrderItem.class);
        Mockito.when(oderItem.getQuantityShipped()).thenReturn(1);

        var bloodType = Mockito.mock(BloodType.class);
        Mockito.when(bloodType.getBloodType()).thenReturn("AB");

        Mockito.when(oderItem.getBloodType()).thenReturn(bloodType);

        var productFamily = Mockito.mock(ProductFamily.class);
        Mockito.when(productFamily.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(oderItem.getProductFamily()).thenReturn(productFamily);

        Mockito.when(order.getOrderItems()).thenReturn(List.of(oderItem));

        Mockito.when(orderRepository.findOneByOrderNumber(Mockito.any())).thenReturn(Mono.just(order));

        Mockito.when(orderRepository.update(Mockito.any())).thenReturn(Mono.just(order));

        var orderShipment = Mockito.mock(OrderShipment.class);

        Mockito.when(orderShipmentRepository.findOneByOrderId(Mockito.anyLong())).thenReturn(Mono.just(orderShipment));

        Mockito.when(orderShipmentRepository.update(Mockito.any())).thenReturn(Mono.just(orderShipment));

        var response = target.processCompletedShipmentEvent(ShipmentCompletedPayload
            .builder()
            .orderNumber(1L)
            .shipmentId(1L)
            .lineItems(List.of(
                ShipmentCompletedItemPayload.builder()
                    .productFamily("PRODUCT_FAMILY")
                    .bloodType("AB")
                    .products(List.of(
                        ShipmentCompletedItemProductPayload.builder()
                            .aboRh("AB")
                            .unitNumber("UNIT_NUMBER")
                            .productCode("PRODUCT_CODE")
                            .productFamily("PRODUCT_FAMILY")
                            .expirationDate(LocalDateTime.now())
                            .collectionDate(ZonedDateTime.now())
                            .build()
                    ))
                    .build()
            ))
            .build()
        );

        StepVerifier.create(response)
            .verifyComplete();

        Mockito.verify(oderItem).defineShippedQuantity(1);
        Mockito.verify(orderShipment).setShipmentStatus("COMPLETED");
        Mockito.verify(order).completeOrderAutomatic();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderCompletedEvent.class));
    }

}
