package com.arcone.biopro.distribution.eventbridge.unit.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.ShipmentCompletedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.ShipmentCompletedMapper;
import com.arcone.biopro.distribution.eventbridge.application.usecase.ShipmentCompletedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;

class ShipmentCompletedUseCaseTest {

    private ApplicationEventPublisher applicationEventPublisher;
    private ShipmentCompletedMapper shipmentCompletedMapper;

    @BeforeEach
    public void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        shipmentCompletedMapper = new ShipmentCompletedMapper();
    }

    @Test
    public void shouldProcessShipmentCompletedEvent() {


        var mockPayload = Mockito.mock(ShipmentCompletedPayload.class);
        Mockito.when(mockPayload.shipmentId()).thenReturn(1L);
        Mockito.when(mockPayload.createDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(mockPayload.externalOrderId()).thenReturn("EXTERNAL_ID");
        Mockito.when(mockPayload.customerCode()).thenReturn("CUSTOMER_CODE");
        Mockito.when(mockPayload.customerName()).thenReturn("CUSTOMER_NAME");
        Mockito.when(mockPayload.departmentCode()).thenReturn("DPT_CODE");
        Mockito.when(mockPayload.customerType()).thenReturn("CUSTOMER_TYPE");
        Mockito.when(mockPayload.locationName()).thenReturn("LOCATION_NAME");
        Mockito.when(mockPayload.locationCode()).thenReturn("LOCATION_CODE");
        Mockito.when(mockPayload.deliveryType()).thenReturn("DELIVERY_TYPE");

        var target = new ShipmentCompletedUseCase(applicationEventPublisher, shipmentCompletedMapper);

        StepVerifier
            .create(target.processCompletedShipmentEvent(mockPayload))
            .verifyComplete();

    }

}
