package com.arcone.biopro.distribution.order.unit.infrastructure.service;

import com.arcone.biopro.distribution.order.infrastructure.dto.AvailableInventoryDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.AvailableInventoryShortDateDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.GetAvailableInventoryCommandDTO;
import com.arcone.biopro.distribution.order.infrastructure.dto.GetAvailableInventoryDTO;
import com.arcone.biopro.distribution.order.infrastructure.service.InventoryRsocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

class InventoryRsocketClientTest {

    @Test
    public void shouldGetAvailableInventory() {

        var rsocketRequesterMock = Mockito.mock(RSocketRequester.class);

        var requestSpec = Mockito.mock(RSocketRequester.RequestSpec.class);

        Mockito.when(rsocketRequesterMock.route("getAvailableInventoryWithShortDatedProducts")).thenReturn(requestSpec);
        Mockito.when(requestSpec.data(Mockito.any())).thenReturn(requestSpec) ;
        Mockito.when(requestSpec.retrieveMono(GetAvailableInventoryDTO.class)).thenReturn(Mono.just(GetAvailableInventoryDTO.builder()
                .locationCode("TEST")
                .inventories(List.of(AvailableInventoryDTO.builder()
                        .aboRh("AP")
                        .productFamily("FAMILY")
                        .quantityAvailable(10)
                        .shortDateProducts(List.of(AvailableInventoryShortDateDTO.builder()
                                .productCode("E0869V00")
                                .storageLocation("FREEZER 1, RACK 1, SHELF 1")
                                .unitNumber("W036810946300")
                            .build()))
                    .build()))
            .build()));

        InventoryRsocketClient client = new InventoryRsocketClient(rsocketRequesterMock);

        var response = client.getAvailableInventoryWithShortDatedProducts(GetAvailableInventoryCommandDTO.builder().locationCode("TEST")
            .build());

        StepVerifier.create(response)
            .consumeNextWith(detail -> {
                Assertions.assertNotNull(detail);
                Assertions.assertEquals(Optional.of("FAMILY"), Optional.of(detail.inventories().get(0).productFamily()));
                Assertions.assertEquals(Optional.of(10), Optional.of(detail.inventories().get(0).quantityAvailable()));
                Assertions.assertEquals(Optional.of("AP"), Optional.of(detail.inventories().get(0).aboRh()));
                Assertions.assertEquals(Optional.of("E0869V00"), Optional.of(detail.inventories().get(0).shortDateProducts().get(0).productCode()));
                Assertions.assertEquals(Optional.of("FREEZER 1, RACK 1, SHELF 1"), Optional.of(detail.inventories().get(0).shortDateProducts().get(0).storageLocation()));
                Assertions.assertEquals(Optional.of("W036810946300"), Optional.of(detail.inventories().get(0).shortDateProducts().get(0).unitNumber()));

            })
            .verifyComplete();
    }

    @Test
    public void shouldNotGetAvailableInventory() {

        var rsocketRequesterMock = Mockito.mock(RSocketRequester.class);

        var requestSpec = Mockito.mock(RSocketRequester.RequestSpec.class);

        Mockito.when(rsocketRequesterMock.route("getAvailableInventoryWithShortDatedProducts")).thenReturn(requestSpec);
        Mockito.when(requestSpec.data(Mockito.any())).thenReturn(requestSpec) ;
        Mockito.when(requestSpec.retrieveMono(GetAvailableInventoryDTO.class)).thenReturn(Mono.error(new RuntimeException("TEST")));

        InventoryRsocketClient client = new InventoryRsocketClient(rsocketRequesterMock);

        var response = client.getAvailableInventoryWithShortDatedProducts(GetAvailableInventoryCommandDTO.builder().locationCode("TEST")
            .build());

        StepVerifier.create(response)
            .expectError()
            .verify();
    }
}
