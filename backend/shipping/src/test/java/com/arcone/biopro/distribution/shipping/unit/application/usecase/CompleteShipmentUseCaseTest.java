package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentEventMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.CompleteShipmentUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.domain.service.ShipmentService;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class CompleteShipmentUseCaseTest {

    private ShipmentRepository shipmentRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private ShipmentEventMapper shipmentEventMapper;
    private FacilityServiceMock facilityServiceMock;
    private ConfigService configService;
    private CompleteShipmentUseCase useCase;
    private ShipmentService shipmentService;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private InventoryRsocketClient inventoryRsocketClient;


    @BeforeEach
    public void setUp(){
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        shipmentEventMapper = new ShipmentEventMapper();
        facilityServiceMock = Mockito.mock(FacilityServiceMock.class);
        configService = Mockito.mock(ConfigService.class);
        shipmentService = Mockito.mock(ShipmentService.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);
        inventoryRsocketClient = Mockito.mock(InventoryRsocketClient.class);

        useCase = new CompleteShipmentUseCase(shipmentRepository,applicationEventPublisher,shipmentEventMapper,facilityServiceMock,configService,shipmentService,shipmentItemPackedRepository,inventoryRsocketClient);

    }

    @Test
    public void shouldNotCompleteShipmentWhenDoesNotExist(){


        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.empty());

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));
        Mockito.when(shipmentItemPackedRepository.countVerificationPendingByShipmentId(1L)).thenReturn(Mono.just(0));

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
                .id(1L)
            .status(ShipmentStatus.COMPLETED)
            .build()));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        Mockito.when(shipmentItemPackedRepository.countVerificationPendingByShipmentId(1L)).thenReturn(Mono.just(0));

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
        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        ShipmentItem item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("product_family");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);


        Mockito.when(shipmentRepository.save(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder()
            .id(1L)
            .status(ShipmentStatus.COMPLETED)
            .locationCode("LOCATION_CODE")
            .build()));


        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.FALSE));

        Mockito.when(facilityServiceMock.getFacilityId(Mockito.anyString())).thenReturn(Mono.just(FacilityDTO.builder()
            .name("Facility Name")
            .build()));

        Mockito.when(shipmentService.getShipmentById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentDetailResponseDTO
            .builder()
            .id(1L)
            .build()));


        Mockito.when(shipmentItemPackedRepository.countVerificationPendingByShipmentId(1L)).thenReturn(Mono.just(0));


        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
            .shipmentId(1L)
            .employeeId("test")
            .build());



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
    public void shouldNotCompleteShipmentWhenSecondVerificationIsEnableAndVerificationIsNotCompleted(){

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getId()).thenReturn(1L);

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipmentMock));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(shipmentItemPackedRepository.countVerificationPendingByShipmentId(1L)).thenReturn(Mono.just(1));

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
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_NOT_COMPLETED_ERROR, firstNotification.message());
                assertEquals("/shipment/1/verify-products", detail._links().get("next"));
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotCompleteShipmentWhenSecondVerificationIsEnableAndHasUnsuitableProducts(){

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getId()).thenReturn(1L);

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipmentMock));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(shipmentItemPackedRepository.countVerificationPendingByShipmentId(1L)).thenReturn(Mono.just(0));

        var completePackedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(completePackedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(completePackedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(completePackedItem.getSecondVerification()).thenReturn(SecondVerification.COMPLETED);
        Mockito.when(completePackedItem.getVerifiedByEmployeeId()).thenReturn("VERIFY_EMPLOYEE_ID");
        Mockito.when(completePackedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");

        Mockito.when(shipmentItemPackedRepository.listAllVerifiedByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(completePackedItem));


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



        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
            .shipmentId(1L)
            .employeeId("test")
            .build());


        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                var inventoryValidationResponseDTO = (InventoryValidationResponseDTO) detail.results().get("validations").getFirst();

                var inventoryNotification = inventoryValidationResponseDTO.inventoryNotificationsDTO().getFirst();

                assertEquals("REASON", inventoryNotification.reason());
                assertEquals("TYPE", inventoryNotification.errorType());
                assertEquals("NAME", inventoryNotification.errorName());
                assertEquals("ACTION", inventoryNotification.action());

                var inventoryResponseDTO = (InventoryResponseDTO) inventoryValidationResponseDTO.inventoryResponseDTO();

                assertEquals("E0701V00", inventoryResponseDTO.productCode());
                assertEquals("W036898786756", inventoryResponseDTO.unitNumber());
                assertEquals("PLASMA_TRANSFUSABLE", inventoryResponseDTO.productFamily());
                assertEquals("PRODUCT_DESCRIPTION", inventoryResponseDTO.productDescription());


                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals(NotificationType.CONFIRMATION.name(), firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SHIPMENT_VALIDATION_COMPLETED_ERROR, firstNotification.message());
                assertEquals("/shipment/1/verify-products", detail._links().get("next"));
            })
            .verifyComplete();

    }


    @Test
    public void shouldCompleteShipmentWhenSecondVerificationIsEnable() {

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
        Mockito.when(configService.findShippingVisualInspectionActive()).thenReturn(Mono.just(Boolean.TRUE));

        ShipmentItem item = Mockito.mock(ShipmentItem.class);
        Mockito.when(item.getId()).thenReturn(1L);
        Mockito.when(item.getProductFamily()).thenReturn("product_family");
        Mockito.when(item.getBloodType()).thenReturn(BloodType.AP);


        Mockito.when(shipmentRepository.save(Mockito.any(Shipment.class))).thenReturn(Mono.just(Shipment.builder()
            .id(1L)
            .status(ShipmentStatus.COMPLETED)
            .locationCode("LOCATION_CODE")
            .build()));


        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(facilityServiceMock.getFacilityId(Mockito.anyString())).thenReturn(Mono.just(FacilityDTO.builder()
            .name("Facility Name")
            .build()));

        Mockito.when(shipmentService.getShipmentById(Mockito.anyLong())).thenReturn(Mono.just(ShipmentDetailResponseDTO
            .builder()
            .id(1L)
            .build()));


        Mockito.when(shipmentItemPackedRepository.countVerificationPendingByShipmentId(1L)).thenReturn(Mono.just(0));


        var completePackedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(completePackedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(completePackedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(completePackedItem.getSecondVerification()).thenReturn(SecondVerification.COMPLETED);
        Mockito.when(completePackedItem.getVerifiedByEmployeeId()).thenReturn("VERIFY_EMPLOYEE_ID");
        Mockito.when(completePackedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");

        Mockito.when(shipmentItemPackedRepository.listAllVerifiedByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(completePackedItem));


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
        Mockito.when(validationResponseDTO.inventoryNotificationsDTO()).thenReturn(Collections.emptyList());

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenReturn(Mono.just(validationResponseDTO));


        Mono<RuleResponseDTO> result = useCase.completeShipment(CompleteShipmentRequest.builder()
            .shipmentId(1L)
            .employeeId("test")
            .build());



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
    public void shouldNotCompleteShipmentWhenSecondVerificationIsEnableAndInventoryServiceDown(){

        var shipmentMock = Mockito.mock(Shipment.class);
        Mockito.when(shipmentMock.getId()).thenReturn(1L);

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipmentMock));

        Mockito.when(configService.findShippingSecondVerificationActive()).thenReturn(Mono.just(Boolean.TRUE));

        Mockito.when(shipmentItemPackedRepository.countVerificationPendingByShipmentId(1L)).thenReturn(Mono.just(0));

        var completePackedItem = Mockito.mock(ShipmentItemPacked.class);
        Mockito.when(completePackedItem.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(completePackedItem.getProductCode()).thenReturn("ABCD");
        Mockito.when(completePackedItem.getSecondVerification()).thenReturn(SecondVerification.COMPLETED);
        Mockito.when(completePackedItem.getVerifiedByEmployeeId()).thenReturn("VERIFY_EMPLOYEE_ID");
        Mockito.when(completePackedItem.getPackedByEmployeeId()).thenReturn("PACK_EMPLOYEE_ID");

        Mockito.when(shipmentItemPackedRepository.listAllVerifiedByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(completePackedItem));

        Mockito.when(inventoryRsocketClient.validateInventory(Mockito.any(InventoryValidationRequest.class))).thenThrow(new InventoryServiceNotAvailableException("ERROR"));

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
                assertEquals("SYSTEM", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.INVENTORY_SERVICE_NOT_AVAILABLE_ERROR, firstNotification.message());
                assertEquals("/shipment/1/verify-products", detail._links().get("next"));
            })
            .verifyComplete();

    }

}
