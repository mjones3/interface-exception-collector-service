package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentItemResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.PackItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.ReasonDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ReasonDomainMapper;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.PackItemUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Reason;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemShortDateProduct;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemShortDateProductRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.service.InventoryRsocketClient;
import com.arcone.biopro.distribution.shipping.infrastructure.service.errors.InventoryServiceNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
class PackItemUseCaseTest {

    private ShipmentItemRepository shipmentItemRepository;
    private ShipmentItemShortDateProductRepository shipmentItemShortDateProductRepository;
    private InventoryRsocketClient inventoryRsocketClient;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private ConfigService configService;
    private ShipmentMapper shipmentMapper;
    private PackItemUseCase useCase;
    private ReasonDomainMapper reasonDomainMapper;
    private SecondVerificationService secondVerificationService;
    private ShipmentRepository shipmentRepository;

    @BeforeEach
    public void setUp(){
        shipmentItemRepository = Mockito.mock(ShipmentItemRepository.class);
        shipmentItemShortDateProductRepository = Mockito.mock(ShipmentItemShortDateProductRepository.class);
        inventoryRsocketClient = Mockito.mock(InventoryRsocketClient.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
        configService = Mockito.mock(ConfigService.class);
        secondVerificationService = Mockito.mock(SecondVerificationService.class);
        reasonDomainMapper = new ReasonDomainMapper();
        shipmentMapper = new ShipmentMapper();
        shipmentRepository = Mockito.mock(ShipmentRepository.class);

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));
        Mockito.when(shipmentRepository.findShipmentByItemId(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));

        useCase = new PackItemUseCase(configService,shipmentItemRepository,shipmentItemShortDateProductRepository,shipmentItemPackedRepository,shipmentMapper,reasonDomainMapper,inventoryRsocketClient,secondVerificationService,shipmentRepository);
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
            .details(Arrays.asList("REASON1","REASON2","REASON3"))
            .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                assertEquals(3, firstNotification.details().size());
                assertEquals("REASON1", firstNotification.details().getFirst());


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
                .temperatureCategory("FROZEN")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .shipmentId(1L)
            .bloodType(BloodType.BP)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(10));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
    public void shouldNotPackItemWhenProductVisualInspectionFails(){

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
                .temperatureCategory("FROZEN")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .shipmentId(1L)
            .bloodType(BloodType.AP)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(10));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));


        var reason = Mockito.mock(Reason.class);
        Mockito.when(reason.getId()).thenReturn(1L);
        Mockito.when(reason.getReasonKey()).thenReturn("reason_key");

        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .productCode("123")
            .visualInspection(VisualInspection.UNSATISFACTORY)
            .build());

        StepVerifier
            .create(packDetail)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();
                ReasonDTO firstReason = (ReasonDTO) detail.results().get("reasons").getFirst();
                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.PRODUCT_CRITERIA_VISUAL_INSPECTION_ERROR, firstNotification.message());
                assertEquals("reason_key", firstReason.reasonKey());

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
                .temperatureCategory("FROZEN")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .bloodType(BloodType.AP)
                .shipmentId(1L)
            .quantity(10)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(10));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                .temperatureCategory("FROZEN")
            .aboRh("AP")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .shipmentId(1L)
            .bloodType(BloodType.AP)
            .quantity(10)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(1));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                .temperatureCategory("FROZEN")
            .aboRh("AP")
            .build());

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .shipmentId(1L)
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

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));
        Mockito.when(secondVerificationService.resetVerification(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder().build()));

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
    public void shouldPackItemWhenItIsSuitableAndVisualInspectionDisable(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
                .temperatureCategory("FROZEN")
            .aboRh("AP")
            .build());

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .shipmentId(1L)
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

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.FALSE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        Mockito.when(secondVerificationService.resetVerification(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder().build()));

        Mono<RuleResponseDTO>  packDetail = useCase.packItem(PackItemRequest.builder()
            .unitNumber("UN")
            .shipmentItemId(1L)
            .employeeId("test")
            .locationCode("123456789")
            .productCode("123")
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
    public void shouldNotPackItemWhenInventoryServiceIsDown(){
        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));
        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

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

    @Test
    public void shouldPackItemWhenRequestingBloodTypeAnyAndProductHasAboRh(){


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
                .temperatureCategory("FROZEN")
            .aboRh("AP")
            .build());

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .id(1L)
            .bloodType(BloodType.ANY)
                .shipmentId(1L)
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

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        Mockito.when(secondVerificationService.resetVerification(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder().build()));

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
    public void shouldNotPackItemWhenTemperatureCategoryDoesNotMatch(){

        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .locationCode("123456789")
            .productCode("123")
            .unitNumber("UN")
            .productFamily("product_family")
            .aboRh("AP")
                .temperatureCategory("REFRIGERATED")
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
                .shipmentId(1L)
            .bloodType(BloodType.AP)
                .quantity(1)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));





        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                assertEquals(ShipmentServiceMessages.PRODUCT_CRITERIA_TEMPERATURE_CATEGORY_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }


    @Test
    public void shouldNotPackItemWhenInventoryIsQuarantinedAndShipTypeInternalTransferAndQuarantinedFlagFalse(){

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getQuarantinedProducts()).thenReturn(false);

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));
        Mockito.when(shipmentRepository.findShipmentByItemId(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));


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
            .errorType("INFO")
            .errorName("INVENTORY_IS_QUARANTINED")
            .action("ACTION")
            .errorCode(1)
            .details(Arrays.asList("REASON1","REASON2","REASON3"))
            .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("product_family")
            .shipmentId(1L)
            .bloodType(BloodType.AP)
            .quantity(1)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                assertEquals("INFO", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.INVENTORY_TEST_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldPackItemWhenInventoryIsQuarantinedAndShipTypeInternalTransferAndQuarantinedFlagTrue() {

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getQuarantinedProducts()).thenReturn(true);

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));
        Mockito.when(shipmentRepository.findShipmentByItemId(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .id(UUID.randomUUID())
            .aboRh("AP")
            .locationCode("123456789")
            .productCode("E0701V00")
            .collectionDate(ZonedDateTime.now())
            .unitNumber("W036898786756")
            .temperatureCategory("FROZEN")
            .productDescription("PRODUCT_DESCRIPTION")
                .status("QUARANTINED")
            .expirationDate(LocalDateTime.now())
            .build());
        Mockito.when(validationResponseDTO.inventoryNotificationsDTO()).thenReturn(List.of(InventoryNotificationDTO.builder()
            .errorMessage(ShipmentServiceMessages.INVENTORY_TEST_ERROR)
            .reason("REASON")
            .errorType("INFO")
            .errorName("INVENTORY_IS_QUARANTINED")
            .action("ACTION")
            .errorCode(1)
            .details(Arrays.asList("REASON1", "REASON2", "REASON3"))
            .build()));

        Mockito.when(validationResponseDTO.hasOnlyNotificationType(Mockito.eq("INVENTORY_IS_QUARANTINED"))).thenReturn(true);


        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .id(1L)
            .bloodType(BloodType.ANY)
            .shipmentId(1L)
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

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        Mockito.when(secondVerificationService.resetVerification(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder().build()));

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
    public void shouldNotPackItemWhenInventoryIsQuarantinedAndShipTypeInternalTransferAndQuarantinedFlagTrueMultipleNotifications(){

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getQuarantinedProducts()).thenReturn(true);

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));
        Mockito.when(shipmentRepository.findShipmentByItemId(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .id(UUID.randomUUID())
            .aboRh("AP")
            .locationCode("123456789")
            .productCode("E0701V00")
            .collectionDate(ZonedDateTime.now())
            .unitNumber("W036898786756")
            .productDescription("PRODUCT_DESCRIPTION")
                .temperatureCategory("FROZEN")
            .expirationDate(LocalDateTime.now())
            .build());
        Mockito.when(validationResponseDTO.inventoryNotificationsDTO()).thenReturn(List.of(InventoryNotificationDTO.builder()
            .errorMessage(ShipmentServiceMessages.INVENTORY_QUARANTINED_ERROR)
            .reason("REASON")
            .errorType("INFO")
            .errorName("INVENTORY_IS_QUARANTINED")
            .action("ACTION")
            .errorCode(1)
            .details(Arrays.asList("REASON1","REASON2","REASON3"))
            .build(),
            InventoryNotificationDTO.builder()
                .errorMessage(ShipmentServiceMessages.INVENTORY_EXPIRED_ERROR)
                .reason("REASON")
                .errorType("INFO")
                .errorName("INVENTORY_IS_EXPIRED")
                .action("ACTION")
                .errorCode(1)
                .details(Arrays.asList("REASON1","REASON2","REASON3"))
                .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .shipmentId(1L)
            .bloodType(BloodType.ANY)
            .quantity(1)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                assertEquals("INFO", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.INVENTORY_QUARANTINED_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenInventoryIsNotQuarantinedAndShipTypeInternalTransferAndQuarantinedFlagTrue(){

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getQuarantinedProducts()).thenReturn(true);

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));
        Mockito.when(shipmentRepository.findShipmentByItemId(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .id(UUID.randomUUID())
            .aboRh("AP")
            .locationCode("123456789")
            .productCode("E0701V00")
            .collectionDate(ZonedDateTime.now())
            .unitNumber("W036898786756")
            .productDescription("PRODUCT_DESCRIPTION")
            .temperatureCategory("FROZEN")
            .expirationDate(LocalDateTime.now())
            .build());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .shipmentId(1L)
            .bloodType(BloodType.ANY)
            .quantity(1)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                assertEquals(ShipmentServiceMessages.PRODUCT_CRITERIA_ONLY_QUARANTINED_PRODUCT_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotPackItemWhenInventoryIsNotLabeledAndShipTypeInternalTransferAndLabelStatusIsLabel(){

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getQuarantinedProducts()).thenReturn(false);
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("LABELED");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));
        Mockito.when(shipmentRepository.findShipmentByItemId(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));


        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .id(UUID.randomUUID())
            .aboRh("AP")
            .locationCode("123456789")
            .productCode("E0701V00")
            .collectionDate(ZonedDateTime.now())
            .unitNumber("W036898786756")
            .productDescription("PRODUCT_DESCRIPTION")
            .temperatureCategory("FROZEN")
            .expirationDate(LocalDateTime.now())
                .isLabeled(false)
            .build());
        Mockito.when(validationResponseDTO.inventoryNotificationsDTO()).thenReturn(List.of(InventoryNotificationDTO.builder()
                .errorMessage(ShipmentServiceMessages.INVENTORY_UNLABELED_ERROR)
                .reason("REASON")
                .errorType("WARN")
                .errorName("INVENTORY_IS_UNLABELED")
                .action("ACTION")
                .errorCode(6)

                .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .shipmentId(1L)
            .bloodType(BloodType.ANY)
            .quantity(1)
            .build()));

        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        var reason = Mockito.mock(Reason.class);
        Mockito.when(configService.findVisualInspectionFailedDiscardReasons()).thenReturn(Flux.just(reason));

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
                assertEquals(ShipmentServiceMessages.INVENTORY_UNLABELED_ERROR, firstNotification.message());
            })
            .verifyComplete();
    }

    @Test
    public void shouldPackItemWhenInventoryIsNotLabeledAndShipTypeInternalTransferAndLabelStatusIsUnLabel() {

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(shipmentMock.getShipmentType()).thenReturn("INTERNAL_TRANSFER");
        Mockito.when(shipmentMock.getQuarantinedProducts()).thenReturn(false);
        Mockito.when(shipmentMock.getLabelStatus()).thenReturn("UNLABELED");

        Mockito.when(shipmentRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));
        Mockito.when(shipmentRepository.findShipmentByItemId(Mockito.anyLong())).thenReturn(Mono.just(shipmentMock));



        InventoryValidationResponseDTO validationResponseDTO = Mockito.mock(InventoryValidationResponseDTO.class);
        Mockito.when(validationResponseDTO.inventoryResponseDTO()).thenReturn(InventoryResponseDTO
            .builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .id(UUID.randomUUID())
            .aboRh("AP")
            .locationCode("123456789")
            .productCode("E0701V00")
            .collectionDate(ZonedDateTime.now())
            .unitNumber("W036898786756")
            .productDescription("PRODUCT_DESCRIPTION")
            .temperatureCategory("FROZEN")
            .expirationDate(LocalDateTime.now())
            .isLabeled(false)
            .build());
        Mockito.when(validationResponseDTO.inventoryNotificationsDTO()).thenReturn(List.of(InventoryNotificationDTO.builder()
            .errorMessage(ShipmentServiceMessages.INVENTORY_UNLABELED_ERROR)
            .reason("REASON")
            .errorType("WARN")
            .errorName("INVENTORY_IS_UNLABELED")
            .action("ACTION")
            .errorCode(6)

            .build()));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));


        Mockito.when(validationResponseDTO.hasOnlyNotificationType(Mockito.eq("INVENTORY_IS_UNLABELED"))).thenReturn(true);

        Mockito.when(shipmentItemRepository.findById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentItem.builder()
            .productFamily("PLASMA_TRANSFUSABLE")
            .id(1L)
            .bloodType(BloodType.ANY)
            .shipmentId(1L)
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


        Mockito.when(shipmentItemPackedRepository.countAllByShipmentItemId(Mockito.anyLong())).thenReturn(Mono.just(0));

        ShipmentItemShortDateProduct shortDateItem = Mockito.mock(ShipmentItemShortDateProduct.class);
        Mockito.when(shortDateItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(shortDateItem.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(shipmentItemShortDateProductRepository.findAllByShipmentItemId(Mockito.anyLong())).thenReturn(Flux.just(shortDateItem));

        Mockito.when(shipmentItemPackedRepository.countAllByUnitNumberAndProductCode(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just(0));

        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        Mockito.when(secondVerificationService.resetVerification(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder().build()));

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

}
