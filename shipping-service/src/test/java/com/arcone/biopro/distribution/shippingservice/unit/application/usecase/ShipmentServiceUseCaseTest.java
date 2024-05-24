package com.arcone.biopro.distribution.shippingservice.unit.application.usecase;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.usecase.ShipmentServiceUseCase;
import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderItemFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.ShortDateItem;
import graphql.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
class ShipmentServiceUseCaseTest {

    @Test
    public void shouldCreateShipment(){

        ShipmentRepository shipmentRepository = Mockito.mock(ShipmentRepository.class);
        ShipmentItemRepository shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);

        ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository);

        OrderFulfilledMessage message = Mockito.mock(OrderFulfilledMessage.class);
        Mockito.when(message.orderNumber()).thenReturn(56L);
        Mockito.when(message.status()).thenReturn("OPEN");
        Mockito.when(message.priority()).thenReturn("ASAP");

        OrderItemFulfilledMessage item = Mockito.mock(OrderItemFulfilledMessage.class);
        Mockito.when(item.bloodType()).thenReturn("AP");
        List<OrderItemFulfilledMessage> items = new ArrayList<>();
        items.add(item);

        ShortDateItem shortDateItem = Mockito.mock(ShortDateItem.class);
        Mockito.when(shortDateItem.productCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.unitNumber()).thenReturn("UNIT_NUMBER");
        List<ShortDateItem> shortDateItems = new ArrayList<>();
        shortDateItems.add(shortDateItem);

        Mockito.when(item.shortDateProducts()).thenReturn(shortDateItems);


        Mockito.when(message.items()).thenReturn(items);

        Shipment shipment = Mockito.mock(Shipment.class);

        Mockito.when(shipmentRepository.save(Mockito.any(Shipment.class))).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemRepository.save(Mockito.any(ShipmentItem.class))).thenReturn(Mono.just(ShipmentItem.builder().id(1L).build()));

        Mockito.when(shipmentItemShortDateProductRepository.save(Mockito.any(ShipmentItemShortDateProduct.class))).thenReturn(Mono.just(ShipmentItemShortDateProduct.builder().id(1L).build()));

        Mono<Shipment> shipmentMono = useCase.create(message);

        StepVerifier.create(shipmentMono)
            .consumeNextWith(orderSaved -> orderSaved.getId())
            .verifyComplete();

    }

    @Test
    public void shouldListShipments(){

        ShipmentRepository shipmentRepository = Mockito.mock(ShipmentRepository.class);
        ShipmentItemRepository shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);

        ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);

        Mockito.when(shipmentRepository.findAll()).thenReturn(Flux.just(shipment));

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository);

        StepVerifier
            .create(useCase.listShipments())
            .expectNextCount(1L)
            .verifyComplete();
    }

    @Test
    public void shouldFindShipmentById(){

        ShipmentRepository shipmentRepository = Mockito.mock(ShipmentRepository.class);
        ShipmentItemRepository shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);

        ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));


        ShipmentItem item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);

        Mockito.when(shipmentItemRepository.findAllByShipmentId(1L)).thenReturn(Flux.just(item));


        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(1L)).thenReturn(Flux.just(shortDateItem));

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository);


        Mono<ShipmentDetailResponseDTO>  orderDetail = useCase.getShipmentById(1L);

        StepVerifier
            .create(orderDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(56L), Optional.of(detail.orderNumber()));
                assertEquals(Optional.of(ShipmentStatus.OPEN), Optional.of(detail.status()));
                assertEquals(Optional.of(ShipmentPriority.ASAP), Optional.of(detail.priority()));
                assertEquals(detail.items().size(), 1);
                assertEquals(Optional.of(BloodType.AP), Optional.of(detail.items().get(0).bloodType()));
                assertEquals(detail.items().get(0).shortDateProducts().size(), 1);
                assertEquals(Optional.of("ABCD"), Optional.of(detail.items().get(0).shortDateProducts().get(0).productCode()));
                assertEquals(Optional.of("UNIT_NUMBER"), Optional.of(detail.items().get(0).shortDateProducts().get(0).unitNumber()));
            })
            .verifyComplete();

    }

    @Test
    public void testStream(){

        List<ShipmentItemShortDateProduct> productList = List.of(ShipmentItemShortDateProduct.builder().productCode("A").build(),ShipmentItemShortDateProduct.builder().productCode("B").build());

        Assert.assertTrue(productList.stream().anyMatch(x -> x.getProductCode().equals("B")));

    }
}
