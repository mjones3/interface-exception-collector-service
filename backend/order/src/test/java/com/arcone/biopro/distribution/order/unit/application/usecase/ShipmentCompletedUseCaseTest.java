package com.arcone.biopro.distribution.order.unit.application.usecase;

import com.arcone.biopro.distribution.order.application.dto.ShipmentCompletedEventPayloadDTO;
import com.arcone.biopro.distribution.order.application.usecase.ShipmentCompletedUseCase;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductFamily;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;


class ShipmentCompletedUseCaseTest {

    @Test
    public void shouldProcessShipmentCompletedEvent(){

        var orderRepository =  Mockito.mock(OrderRepository.class) ;

        var target = new ShipmentCompletedUseCase(orderRepository);

        var order = Mockito.mock(Order.class);

        var oderItem = Mockito.mock(OrderItem.class);
        Mockito.when(oderItem.getQuantityShipped()).thenReturn(1);

        var bloodType = Mockito.mock(BloodType.class);
        Mockito.when(bloodType.getBloodType()).thenReturn("AB");

        Mockito.when(oderItem.getBloodType()).thenReturn(bloodType);

        var productFamily = Mockito.mock(ProductFamily.class);
        Mockito.when(productFamily.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(oderItem.getProductFamily()).thenReturn(productFamily);

        Mockito.when(order.getOrderItems()).thenReturn(List.of(oderItem));

        var orderStatus = Mockito.mock(OrderStatus.class);

        Mockito.when(order.getOrderStatus()).thenReturn(orderStatus);

        Mockito.when(orderRepository.findOneByOrderNumber(Mockito.any())).thenReturn(Mono.just(order));

        Mockito.when(orderRepository.update(Mockito.any())).thenReturn(Mono.just(order));

        var response = target.processCompletedShipmentEvent(ShipmentCompletedEventPayloadDTO
            .builder()
            .orderNumber(1L)
            .shipmentId(1L)
                .bloodType("AB")
                .productCode("PRODUCT_CODE")
                .productFamily("PRODUCT_FAMILY")
            .build());

        StepVerifier.create(response)
            .verifyComplete();

        Mockito.verify(oderItem).defineShippedQuantity(2);

    }

}
