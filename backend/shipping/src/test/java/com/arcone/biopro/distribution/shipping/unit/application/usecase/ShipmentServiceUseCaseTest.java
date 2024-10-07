package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentEventMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.ShipmentServiceUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCreatedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderFulfilledMessage;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.OrderItemFulfilledMessage;
import com.arcone.biopro.distribution.shipping.infrastructure.listener.dto.ShortDateItem;
import com.arcone.biopro.distribution.shipping.infrastructure.service.FacilityServiceMock;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.FacilityDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.service.errors.InventoryServiceNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
class ShipmentServiceUseCaseTest {

    private ShipmentRepository shipmentRepository;
    private ShipmentItemRepository shipmentItemRepository;
    private ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;
    private InventoryRsocketClient inventoryRsocketClient;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private ShipmentEventMapper shipmentEventMapper;
    private FacilityServiceMock facilityServiceMock;
    private ConfigService configService;

    private ShipmentServiceUseCase useCase;

    @BeforeEach
    public void setUp(){
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);
        inventoryRsocketClient = Mockito.mock(InventoryRsocketClient.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        shipmentEventMapper = new ShipmentEventMapper();
        facilityServiceMock = Mockito.mock(FacilityServiceMock.class);
        configService = Mockito.mock(ConfigService.class);

        useCase = new ShipmentServiceUseCase(shipmentRepository,shipmentItemRepository,shipmentItemShortDateProductRepository,inventoryRsocketClient,shipmentItemPackedRepository,applicationEventPublisher,shipmentEventMapper,facilityServiceMock,configService);
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
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenInventoryValidationFails(){

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
                .builder()
                .productFamily("PLASMA_TRANSFUSABLE")
                .id(UUID.randomUUID())
                .aboRh("AB")
                .locationCode("123456789")
                .productCode("E0701V00")
                .collectionDate(ZonedDateTime.now())
                .unitNumber("W036898786756")
                .productDescription("PRODUCT_DESCRIPTION")
                .expirationDate(LocalDateTime.now())
                .build());
        Mockito.when(validationResponseDTO.inventoryNotificationsDTO()).thenReturn(List.of(InventoryNotificationDTO.builder()
                .errorMessage(ShipmentServiceMessages.INVENTORY_TEST_ERROR)
                .reason("REASON")
                .errorType("TYPE")
                .errorName("NAME")
                .action("ACTION")
                .errorCode(1)
            .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
                .unitNumber("UN")
                .shipmentItemId(1L)
                .employeeId("test")
                .locationCode("123456789")
                .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(ShipmentServiceMessages.INVENTORY_TEST_ERROR, firstNotification.message());
                assertEquals("REASON", firstNotification.reason());
                assertEquals("TYPE", firstNotification.notificationType());
                assertEquals("NAME", firstNotification.name());
                assertEquals("ACTION", firstNotification.action());

                var inventoryResponseDTO = (InventoryResponseDTO) detail.results().get("inventory").getFirst();
                assertEquals("E0701V00", inventoryResponseDTO.productCode());
                assertEquals("W036898786756", inventoryResponseDTO.unitNumber());
                assertEquals("PLASMA_TRANSFUSABLE", inventoryResponseDTO.productFamily());
                assertEquals("PRODUCT_DESCRIPTION", inventoryResponseDTO.productDescription());
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductCriteriaDoesNotMatch(){

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
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
            .locationCode("123456789")
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.PRODUCT_CRITERIA_BLOOD_TYPE_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductCriteriaQuantityHasExceeded(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
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
            .locationCode("123456789")
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.PRODUCT_CRITERIA_QUANTITY_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenProductIsAlreadyFilled(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
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
            .locationCode("123456789")
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.PRODUCT_ALREADY_USED_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldPackItemWhenItIsSuitable(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
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
            .locationCode("123456789")
            .productCode("123")
                .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertNull(detail.notifications());
                assertNotNull(detail.results());

                var ruleResults = detail.results().get("results");
                var firstRuleResult = ruleResults.getFirst();
                assertNotNull(ruleResults);
                assertNotNull(firstRuleResult);

                var shipmentItem = (ShipmentItemResponseDTO) firstRuleResult;
                var firstPackedItem = shipmentItem.packedItems().getFirst();
                assertEquals("UN", firstPackedItem.unitNumber());
                assertEquals("product_code", firstPackedItem.productCode());
                assertEquals("test", firstPackedItem.packedByEmployeeId());

                var firstShortDateProduct = shipmentItem.shortDateProducts().getFirst();
                assertEquals("UNIT_NUMBER", firstShortDateProduct.unitNumber());
                assertEquals("ABCD", firstShortDateProduct.productCode());
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotCompleteShipmentWhenDoesNotExist(){


        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
                .shipmentId(1L)
                .employeeId("test")
            .build());


        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SHIPMENT_NOT_FOUND_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotCompleteShipmentWhenItsAlreadyCompleted(){

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(Shipment.builder()
                .status(ShipmentStatus.COMPLETED)
            .build()));

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(1L)).thenReturn(Flux.empty());

        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
            .shipmentId(1L)
            .employeeId("test")
            .build());


        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SHIPMENT_COMPLETED_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldCompleteShipment() {

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");
        Mockito.when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));
        Mockito.when(configService.findShippingCheckDigitActive()).thenReturn(Mono.just(Boolean.FALSE));

        ShipmentItem item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("product_family");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);

        Mockito.when(shipmentItemRepository.findAllByShipmentId(1L)).thenReturn(Flux.just(item));

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(1L)).thenReturn(Flux.empty());

        Mockito.when(shipmentItemPackedRepository.findAllByShipmentItemId(Mockito.any())).thenReturn(Flux.just(ShipmentItemPacked.builder()
            .id(1L)
            .shipmentItemId(1L)
            .unitNumber("UN")
            .productCode("product_code")
            .build()));


        Mockito.when(shipmentRepository.save(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder()
                .id(1L)
                .status(ShipmentStatus.COMPLETED)
                .locationCode("LOCATION_CODE")
            .build()));



        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
            .shipmentId(1L)
            .employeeId("test")
            .build());

        Mockito.when(facilityServiceMock.getFacilityId(Mockito.anyString())).thenReturn(Mono.just(FacilityDTO.builder()
            .name("Facility Name")
            .build()));


        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                assertEquals("success", firstNotification.notificationType());
                assertEquals("/shipment/1/shipment-details", detail._links().get("next"));
                assertEquals(ShipmentServiceMessages.SHIPMENT_COMPLETED_SUCCESS, firstNotification.message());
            })
            .verifyComplete();

        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(ShipmentCompletedEvent.class));
    }


    @Test
    public void shouldNotPackItemWhenInventoryServiceIsDown(){

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.error(new InventoryServiceNotAvailableException("INVENTORY_SERVICE_DOW")));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .productCode("123")
            .visualInspection(VisualInspection.SATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("INVENTORY_SERVICE_DOW", firstNotification.message());
                assertEquals("SYSTEM", firstNotification.notificationType());
                assertEquals("INVENTORY_SERVICE_IS_DOWN", firstNotification.name());
            })
            .verifyComplete();
    }

}
