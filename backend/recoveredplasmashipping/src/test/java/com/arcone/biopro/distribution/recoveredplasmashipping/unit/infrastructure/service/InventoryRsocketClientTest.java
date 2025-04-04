package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.exception.InventoryServiceNotAvailableException;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service.InventoryRsocketClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class InventoryRsocketClientTest {

    @Test
    public void shouldGetInventoryDetails(){

        var id = UUID.randomUUID();

        var rsocketRequesterMock = Mockito.mock(RSocketRequester.class);

        var requestSpec = Mockito.mock(RSocketRequester.RequestSpec.class);

        Mockito.when(rsocketRequesterMock.route("validateInventory")).thenReturn(requestSpec);
        Mockito.when(requestSpec.data(Mockito.any())).thenReturn(requestSpec) ;
        Mockito.when(requestSpec.retrieveMono(InventoryValidationResponseDTO.class)).thenReturn(Mono.just(InventoryValidationResponseDTO.builder()
                .inventoryResponseDTO(InventoryResponseDTO.builder()
                    .id(id)
                    .unitNumber("W036898786799")
                    .productCode("E0701V00")
                    .locationCode("123456789")
                    .build())
            .build()));

        var target = new InventoryRsocketClient(rsocketRequesterMock);

        var response = target.validateInventory(InventoryValidationRequest.builder().build());

        StepVerifier.create(response)
            .consumeNextWith(detail -> {
                assertNotNull(detail.inventoryResponseDTO());
                assertEquals(Optional.of(id), Optional.of(detail.inventoryResponseDTO().id()));
                assertEquals(Optional.of("W036898786799"), Optional.of(detail.inventoryResponseDTO().unitNumber()));
                assertEquals(Optional.of("E0701V00"), Optional.of(detail.inventoryResponseDTO().productCode()));
                assertEquals(Optional.of("123456789"), Optional.of(detail.inventoryResponseDTO().locationCode()));
            })
            .verifyComplete();

    }

    @Test
    public void shouldGetErrorWhenInventoryServiceFails(){
        var rsocketRequesterMock = Mockito.mock(RSocketRequester.class);

        var requestSpec = Mockito.mock(RSocketRequester.RequestSpec.class);

        Mockito.when(rsocketRequesterMock.route("validateInventory")).thenReturn(requestSpec);
        Mockito.when(requestSpec.data(Mockito.any())).thenReturn(requestSpec) ;
        Mockito.when(requestSpec.retrieveMono(InventoryValidationResponseDTO.class)).thenReturn(Mono.error(new RuntimeException("Any error")));

        var target = new InventoryRsocketClient(rsocketRequesterMock);

        var response = target.validateInventory(InventoryValidationRequest.builder().build());

        StepVerifier.create(response)
            .expectError(InventoryServiceNotAvailableException.class)
            .verify();
    }

}
