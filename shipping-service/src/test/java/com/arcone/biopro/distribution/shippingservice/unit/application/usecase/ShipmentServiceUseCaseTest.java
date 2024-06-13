package com.arcone.biopro.distribution.shippingservice.unit.application.usecase;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShipmentResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shippingservice.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shippingservice.application.usecase.ShipmentServiceUseCase;
import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shippingservice.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shippingservice.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.OrderItemFulfilledMessage;
import com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto.ShortDateItem;
import com.arcone.biopro.distribution.shippingservice.infrastructure.service.InventoryRsocketClient;
import graphql.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
class ShipmentServiceUseCaseTest {

    private ShipmentRepository shipmentRepository;
    private ShipmentItemRepository shipmentItemRepository;

    private ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;

    private InventoryRsocketClient inventoryRsocketClient;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;

    @BeforeEach
    public void setUp(){
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);
        inventoryRsocketClient = Mockito.mock(InventoryRsocketClient.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
    }
    @Test
    public void shouldCreateShipment(){

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository,inventoryRsocketClient,shipmentItemPackedRepository);

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

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);

        Mockito.when(shipmentRepository.findAll()).thenReturn(Flux.just(shipment));

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository, inventoryRsocketClient,shipmentItemPackedRepository);

        StepVerifier
            .create(useCase.listShipments())
            .expectNextCount(1L)
            .verifyComplete();
    }

    @Test
    public void shouldFindShipmentById(){



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

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository, inventoryRsocketClient,shipmentItemPackedRepository);


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
    public void shouldNotPackItemWhenInventoryValidationFails(){

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository, inventoryRsocketClient,shipmentItemPackedRepository);

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryNotificationDTO()).thenReturn(InventoryNotificationDTO.builder()
                .errorMessage("test-error-inventory")
                .errorCode(1)
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .employeeId("test")
                .locationCode(1)
                .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of("test-error-inventory"), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductCriteriaDoesNotMatch(){

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository, inventoryRsocketClient,shipmentItemPackedRepository);

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode(1)
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .bloodType(BloodType.BP)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(10));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode(1)
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of("product-criteria-blood-type-does-not-match.error"), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductCriteriaQuantityHasExceeded(){

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository, inventoryRsocketClient,shipmentItemPackedRepository);

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode(1)
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .bloodType(BloodType.AP)
            .quantity(10)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(10));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));


        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode(1)
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of("product-criteria-quantity-exceeded.error"), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductIsAlreadyFilled(){

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository, inventoryRsocketClient,shipmentItemPackedRepository);

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode(1)
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .bloodType(BloodType.AP)
            .quantity(10)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(1));


        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode(1)
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST), Optional.of(detail.ruleCode()));
                assertEquals(Optional.of(HttpStatus.BAD_REQUEST.value()), Optional.of(detail.notifications().get(0).statusCode()));
                assertEquals(Optional.of("error"), Optional.of(detail.notifications().get(0).notificationType()));
                assertEquals(Optional.of("product-is-already-used.error"), Optional.of(detail.notifications().get(0).message()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldPackItemWhenItIsSuitable(){

        ShipmentServiceUseCase useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository, inventoryRsocketClient,shipmentItemPackedRepository);

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
                .locationCode(1)
                .productCode("123")
                .unitNumber("UN")
                .productFamily("product_family")
                .aboRh("AP")
            .build());

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .id(1L)
            .bloodType(BloodType.AP)
                .quantity(10)
            .build()));


        Mockito.when(shipmentItemPackedRepository.save(Mockito.any(ShipmentItemPacked.class))).thenReturn(Mono.just(ShipmentItemPacked.builder()
                .id(1L)
                .unitNumber("UN")
                .shipmentItemId(1L)
                .productCode("product_code")
                .packedByEmployeeId("test")
                .shipmentItemId(1L)
            .build()));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(ShipmentItemPacked.builder()
            .id(1L)
            .unitNumber("UN")
            .productCode("product_code")
            .packedByEmployeeId("test")
            .shipmentItemId(1L)
            .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(shortDateItem));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode(1)
            .productCode("123")
                .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(Optional.of(HttpStatus.OK), Optional.of(detail.ruleCode()));
                assertNull(detail.notifications());
                assertNotNull(detail.results());
                assertNotNull(detail.results().get("results"));
                assertNotNull(detail.results().get("results").get(0));
                ShipmentItemResponseDTO result = (ShipmentItemResponseDTO) detail.results().get("results").get(0);
                assertEquals(Optional.of("UN"), Optional.of(result.packedItems().get(0).unitNumber()));
                assertEquals(Optional.of("product_code"), Optional.of(result.packedItems().get(0).productCode()));
                assertEquals(Optional.of("test"), Optional.of(result.packedItems().get(0).packedByEmployeeId()));

                assertEquals(Optional.of("UNIT_NUMBER"), Optional.of(result.shortDateProducts().get(0).unitNumber()));
                assertEquals(Optional.of("ABCD"), Optional.of(result.shortDateProducts().get(0).productCode()));


            })
            .verifyComplete();
    }

}
