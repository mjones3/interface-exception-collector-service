package com.arcone.biopro.distribution.shipping.unit.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.CancelSecondVerificationRequest;
import com.arcone.biopro.distribution.shipping.application.usecase.CancelSecondVerificationUseCase;
import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentItemPackedRepository;
import com.arcone.biopro.distribution.shipping.domain.repository.ShipmentRepository;
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

@RunWith(MockitoJUnitRunner.class)
class CancelSecondVerificationUseCaseTest {

    private ShipmentRepository shipmentRepository;
    private ShipmentItemPackedRepository shipmentItemPackedRepository;
    private CancelSecondVerificationUseCase useCase;

    @BeforeEach
    public void setUp() {
        shipmentRepository = Mockito.mock(ShipmentRepository.class);
        shipmentItemPackedRepository = Mockito.mock(ShipmentItemPackedRepository.class);
        useCase = new CancelSecondVerificationUseCase(shipmentRepository, shipmentItemPackedRepository);
    }


    @Test
    public void shouldCancelSecondVerification() {

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");
        Mockito.when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemPackedRepository.countIneligibleByShipmentId(Mockito.anyLong())).thenReturn(Mono.just(0));

        var result = useCase.cancelSecondVerification(CancelSecondVerificationRequest
            .builder()
                .employeeId("EMPLOYEE_ID")
                .shipmentId(1L)
            .build());

        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                assertEquals("CONFIRMATION", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_CANCEL_CONFIRMATION, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldConfirmCancelSecondVerification() {

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");
        Mockito.when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemPackedRepository.countIneligibleByShipmentId(Mockito.anyLong())).thenReturn(Mono.just(0));

        ShipmentItemPacked item = Mockito.mock(ShipmentItemPacked.class);

        Mockito.when(shipmentItemPackedRepository.listAllByShipmentId(Mockito.anyLong())).thenReturn(Flux.just(item));

        Mockito.when(shipmentItemPackedRepository.save(Mockito.any())).thenReturn(Mono.just(item));

        var result = useCase.confirmCancelSecondVerification(CancelSecondVerificationRequest
            .builder()
            .employeeId("EMPLOYEE_ID")
            .shipmentId(1L)
            .build());

        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.OK, detail.ruleCode());
                assertEquals(HttpStatus.OK.value(), firstNotification.statusCode());
                assertEquals("SUCCESS", firstNotification.notificationType());
                assertEquals("/shipment/1/shipment-details", detail._links().get("next"));
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_CANCEL_SUCCESS, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotCancelSecondVerificationWhenContainsIneligibleProductsTobeRemoved() {

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");
        Mockito.when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemPackedRepository.countIneligibleByShipmentId(Mockito.anyLong())).thenReturn(Mono.just(1));

        var result = useCase.cancelSecondVerification(CancelSecondVerificationRequest
            .builder()
            .employeeId("EMPLOYEE_ID")
            .shipmentId(1L)
            .build());

        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_WITH_INELIGIBLE_PRODUCTS_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotCancelSecondVerificationWhenShipmentCompleted() {

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.COMPLETED);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");
        Mockito.when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemPackedRepository.countIneligibleByShipmentId(Mockito.anyLong())).thenReturn(Mono.just(1));

        var result = useCase.cancelSecondVerification(CancelSecondVerificationRequest
            .builder()
            .employeeId("EMPLOYEE_ID")
            .shipmentId(1L)
            .build());

        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_WITH_SHIPMENT_COMPLETED_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotConfirmCancelSecondVerificationWhenContainsIneligibleProductsTobeRemoved() {

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.OPEN);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");
        Mockito.when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemPackedRepository.countIneligibleByShipmentId(Mockito.anyLong())).thenReturn(Mono.just(1));

        var result = useCase.confirmCancelSecondVerification(CancelSecondVerificationRequest
            .builder()
            .employeeId("EMPLOYEE_ID")
            .shipmentId(1L)
            .build());

        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_WITH_INELIGIBLE_PRODUCTS_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotConfirmCancelSecondVerificationWhenShipmentCompleted() {

        Shipment shipment = Mockito.mock(Shipment.class);
        Mockito.when(shipment.getId()).thenReturn(1L);
        Mockito.when(shipment.getOrderNumber()).thenReturn(56L);
        Mockito.when(shipment.getExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(shipment.getStatus()).thenReturn(ShipmentStatus.COMPLETED);
        Mockito.when(shipment.getPriority()).thenReturn(ShipmentPriority.ASAP);
        Mockito.when(shipment.getComments()).thenReturn("TEST_COMMENTS");
        Mockito.when(shipment.getLocationCode()).thenReturn("LOCATION_CODE");

        Mockito.when(shipmentRepository.findById(1L)).thenReturn(Mono.just(shipment));

        Mockito.when(shipmentItemPackedRepository.countIneligibleByShipmentId(Mockito.anyLong())).thenReturn(Mono.just(1));

        var result = useCase.confirmCancelSecondVerification(CancelSecondVerificationRequest
            .builder()
            .employeeId("EMPLOYEE_ID")
            .shipmentId(1L)
            .build());

        StepVerifier
            .create(result)
            .consumeNextWith(detail -> {
                var firstNotification = detail.notifications().getFirst();

                assertEquals(HttpStatus.BAD_REQUEST, detail.ruleCode());
                assertEquals(HttpStatus.BAD_REQUEST.value(), firstNotification.statusCode());
                assertEquals("WARN", firstNotification.notificationType());
                assertEquals(ShipmentServiceMessages.SECOND_VERIFICATION_WITH_SHIPMENT_COMPLETED_ERROR, firstNotification.message());
            })
            .verifyComplete();

    }

}
