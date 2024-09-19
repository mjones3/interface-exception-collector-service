package com.arcone.biopro.distribution.shipping.unit.infrastructure.controller;

import com.arcone.biopro.distribution.shipping.application.util.ShipmentServiceMessages;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.InventoryMockController;
import com.arcone.biopro.distribution.shipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

class InventoryMockControllerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldGetInventoryDetailsWhenItsAvailable(){


        var target = new InventoryMockController(objectMapper);
        var inventoryResponse = target.validateInventory(InventoryValidationRequest.builder()
            .unitNumber("W036898786799")
                .productCode("E1624V00")
                .locationCode("123456789")
            .build());

        StepVerifier.create(inventoryResponse)
            .consumeNextWith(detail -> {
                assertNotNull(detail.inventoryResponseDTO());
                assertEquals(Optional.of(UUID.fromString("dd8af54c-a40a-438c-a870-b9619447e91f")), Optional.of(detail.inventoryResponseDTO().id()));
                assertEquals(Optional.of("W036898786799"), Optional.of(detail.inventoryResponseDTO().unitNumber()));
                assertEquals(Optional.of("E1624V00"), Optional.of(detail.inventoryResponseDTO().productCode()));
                assertEquals(Optional.of("123456789"), Optional.of(detail.inventoryResponseDTO().locationCode()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetInventoryDetailsWhenItsNotAvailable(){


        var target = new InventoryMockController(objectMapper);
        var inventoryResponse = target.validateInventory(InventoryValidationRequest.builder()
            .unitNumber("W036898786755")
            .productCode("E0701V00")
            .locationCode("123456789")
            .build());


        StepVerifier.create(inventoryResponse)
            .consumeNextWith(detail -> {
                assertNull(detail.inventoryResponseDTO());
                assertNotNull(detail.inventoryNotificationsDTO().getFirst());
                assertEquals(Optional.of(1), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorCode()));
                assertEquals(Optional.of(ShipmentServiceMessages.INVENTORY_NOT_FOUND_ERROR), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorMessage()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetInventoryDetailsWhenItsQuarantine(){

        var target = new InventoryMockController(objectMapper);

        var inventoryResponse = target.validateInventory(InventoryValidationRequest.builder()
            .unitNumber("W036898786758")
            .productCode("E0701V00")
            .locationCode("123456789")
            .build());


        StepVerifier.create(inventoryResponse)
            .consumeNextWith(detail -> {
                assertNull(detail.inventoryResponseDTO());
                assertNotNull(detail.inventoryNotificationsDTO().getFirst());
                assertEquals(Optional.of(4), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorCode()));
                assertEquals(Optional.of(ShipmentServiceMessages.INVENTORY_QUARANTINED_ERROR), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorMessage()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetInventoryDetailsWhenItsDiscarded(){

        var target = new InventoryMockController(objectMapper);
        var inventoryResponse = target.validateInventory(InventoryValidationRequest.builder()
            .unitNumber("W036898786757")
            .productCode("E0701V00")
            .locationCode("123456789")
            .build());


        StepVerifier.create(inventoryResponse)
            .consumeNextWith(detail -> {
                assertNull(detail.inventoryResponseDTO());
                assertNotNull(detail.inventoryNotificationsDTO().getFirst());
                assertEquals(Optional.of(3), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorCode()));
                assertEquals(Optional.of(ShipmentServiceMessages.INVENTORY_DISCARDED_ERROR), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorMessage()));
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetInventoryDetailsWhenItsExpired(){

        var target = new InventoryMockController(objectMapper);

        var inventoryResponse = target.validateInventory(InventoryValidationRequest.builder()
            .unitNumber("W036898786756")
            .productCode("E0701V00")
            .locationCode("123456789")
            .build());


        StepVerifier.create(inventoryResponse)
            .consumeNextWith(detail -> {
                assertNotNull(detail.inventoryResponseDTO());
                assertNotNull(detail.inventoryNotificationsDTO().getFirst());
                assertEquals(Optional.of(2), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorCode()));
                assertEquals(Optional.of(ShipmentServiceMessages.INVENTORY_EXPIRED_ERROR), Optional.of(detail.inventoryNotificationsDTO().getFirst().errorMessage()));
            })
            .verifyComplete();
    }

}
