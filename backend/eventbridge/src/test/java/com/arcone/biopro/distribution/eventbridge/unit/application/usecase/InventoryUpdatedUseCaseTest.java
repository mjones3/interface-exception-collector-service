package com.arcone.biopro.distribution.eventbridge.unit.application.usecase;

import com.arcone.biopro.distribution.eventbridge.application.dto.InventoryUpdatedPayload;
import com.arcone.biopro.distribution.eventbridge.application.mapper.InventoryUpdatedMapper;
import com.arcone.biopro.distribution.eventbridge.application.usecase.InventoryUpdatedUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class InventoryUpdatedUseCaseTest {

    private ApplicationEventPublisher applicationEventPublisher;
    private InventoryUpdatedMapper inventoryUpdatedMapper;

    @BeforeEach
    public void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        inventoryUpdatedMapper = new InventoryUpdatedMapper();
    }

    @Test
    public void shouldProcessInventoryUpdatedEvent() {
        var mockPayload = Mockito.mock(InventoryUpdatedPayload.class);
        Mockito.when(mockPayload.updateType()).thenReturn("UPDATE_TYPE");
        Mockito.when(mockPayload.unitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(mockPayload.productCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(mockPayload.productDescription()).thenReturn("PRODUCT_DESCRIPTION");
        Mockito.when(mockPayload.productFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(mockPayload.bloodType()).thenReturn("BLOOD_TYPE");
        Mockito.when(mockPayload.expirationDate()).thenReturn(LocalDate.now());
        Mockito.when(mockPayload.locationCode()).thenReturn("LOCATION_CODE");
        Mockito.when(mockPayload.storageLocation()).thenReturn("STORAGE_LOCATION");
        Mockito.when(mockPayload.inventoryStatus()).thenReturn(List.of("INVENTORY_STATUS"));
        Mockito.when(mockPayload.properties()).thenReturn(Map.of());
        Mockito.when(mockPayload.inputProducts()).thenReturn(List.of());

        var target = new InventoryUpdatedUseCase(applicationEventPublisher, inventoryUpdatedMapper);

        StepVerifier
            .create(target.processInventoryUpdatedEvent(mockPayload))
            .verifyComplete();

    }

}
