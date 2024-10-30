package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.ShipmentDetailResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ShipmentEventMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.CompleteShipmentUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.event.ShipmentCompletedEvent;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
import com.arcone.biopro.distribution.shipping.domain.service.ConfigService;
import com.arcone.biopro.distribution.shipping.domain.service.ShipmentService;
import com.arcone.biopro.distribution.shipping.infrastructure.service.FacilityServiceMock;
import com.arcone.biopro.distribution.shipping.infrastructure.service.dto.FacilityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

    @BeforeEach
    public void setUp(){
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        shipmentEventMapper = new ShipmentEventMapper();
        facilityServiceMock = Mockito.mock(FacilityServiceMock.class);
        configService = Mockito.mock(ConfigService.class);
        shipmentService = Mockito.mock(ShipmentService.class);
        shipmentItemPackedRepository = Mockito.mock( ShipmentItemPackedRepository.class);

        useCase = new CompleteShipmentUseCase(shipmentRepository,applicationEventPublisher,shipmentEventMapper,facilityServiceMock,configService,shipmentService,shipmentItemPackedRepository);

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
                assertEquals("/shipment/1/shipment-verification", detail._links().get("next"));
            })
            .verifyComplete();

    }

}
