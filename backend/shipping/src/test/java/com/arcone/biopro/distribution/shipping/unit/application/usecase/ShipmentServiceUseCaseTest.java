package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.ShipmentServiceUseCase;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCreatedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderItemFulfilledMessage;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShortDateItem;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class ShipmentServiceUseCaseTest {

    private ShipmentRepository shipmentRepository;
    private ShipmentItemRepository shipmentItemRepository;
    private ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;
    private InventoryRsocketClient inventoryRsocketClient;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private ConfigService configService;
    private ShipmentServiceUseCase useCase;
    private ShipmentMapper shipmentMapper;

    @BeforeEach
    public void setUp(){
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);
        inventoryRsocketClient = Mockito.mock(InventoryRsocketClient.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        configService = Mockito.mock(ConfigService.class);
        shipmentMapper = new ShipmentMapper();

        useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository,shipmentItemPackedRepository,applicationEventPublisher,configService,shipmentMapper);
    }

    @Test
    public void shouldCreateShipment(){

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

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(ShipmentCreatedEvent.class));

    }

    @Test
    public void shouldListShipments(){

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);

        Mockito.when(shipmentRepository.findAll()).thenReturn(Flux.just(shipment));

        StepVerifier
            .create(useCase.listShipments())
            .expectNextCount(1L)
            .verifyComplete();
    }

    @Test
    public void shouldFindShipmentById() {
        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));
        Mockito.when(configService.findShippingCheckDigitActive()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        ShipmentItem item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("product_family");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);

        Mockito.when(shipmentItemRepository.findAllByShipmentId(1L)).thenReturn(Flux.just(item));

        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(1L)).thenReturn(Flux.just(shortDateItem));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(Mockito.any())).thenReturn(Flux.just(ShipmentItemPacked.builder()
            .id(1L)
            .shipmentItemId(1L)
            .unitNumber("UN")
            .productCode("product_code")
            .build()));

        Mono<ShipmentDetailResponseDTO>  orderDetail = useCase.getShipmentById(1L);

        StepVerifier
            .create(orderDetail)
            .consumeNextWith(detail -> {
                var firstShipmentItem = detail.items().getFirst();
                var firstShortDatedProducts = firstShipmentItem.shortDateProducts().getFirst();
                var firstPackedItem = firstShipmentItem.packedItems().getFirst();

                assertEquals(56L, detail.orderNumber().longValue());
                assertEquals("EXTERNAL_ID", detail.externalId());
                assertEquals(ShipmentStatus.OPEN, detail.status());
                assertEquals(ShipmentPriority.ASAP, detail.priority());
                assertEquals("TEST_COMMENTS", detail.comments());
                assertEquals(detail.items().size(), 1);
                assertEquals(BloodType.AP, firstShipmentItem.bloodType());
                assertEquals(firstShipmentItem.shortDateProducts().size(), 1);
                assertEquals("ABCD", firstShortDatedProducts.productCode());
                assertEquals("UNIT_NUMBER", firstShortDatedProducts.unitNumber());
                assertEquals(firstShipmentItem.packedItems().size(), 1);
                assertEquals("product_code", firstPackedItem.productCode());
                assertEquals("UN", firstPackedItem.unitNumber());
                assertTrue(detail.checkDigitActive());
                assertTrue(detail.visualInspectionActive());
                assertFalse(detail.secondVerificationActive());
            })
            .verifyComplete();
    }






}
