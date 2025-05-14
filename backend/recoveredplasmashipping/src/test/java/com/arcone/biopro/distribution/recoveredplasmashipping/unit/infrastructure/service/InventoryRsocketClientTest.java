package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ValidateInventoryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryNotificationDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.dto.InventoryVolumeDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.exception.InventoryServiceNotAvailableException;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CommandMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.InventoryMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.service.InventoryRsocketClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryRsocketClientTest {

    private InventoryMapper inventoryMapper;
    private CommandMapper commandMapper;

    @BeforeEach
    public void setup() {
        inventoryMapper = Mappers.getMapper(InventoryMapper.class);
        commandMapper = Mappers.getMapper(CommandMapper.class);
    }

    @Test
    public void shouldGetInventoryDetails(){

        var id = UUID.randomUUID();

        var rsocketRequesterMock = Mockito.mock(RSocketRequester.class);

        var requestSpec = Mockito.mock(RSocketRequester.RequestSpec.class);

        Mockito.when(rsocketRequesterMock.route("validateInventory")).thenReturn(requestSpec);
        Mockito.when(requestSpec.data(Mockito.any())).thenReturn(requestSpec) ;
        var dto = InventoryValidationResponseDTO.builder()
            .inventoryResponseDTO(InventoryResponseDTO.builder()
                .id(UUID.randomUUID())
                .unitNumber("W036898786799")
                .locationCode("123456789")
                .productCode("E0701V00")
                .productDescription("Description")
                .expirationDate(LocalDateTime.now())
                .aboRh("AP")
                .productFamily("Product family")
                .collectionDate(ZonedDateTime.now())
                .storageLocation("Storage Location")
                .createDate(ZonedDateTime.now())
                .modificationDate(ZonedDateTime.now())
                .volumes(List.of(InventoryVolumeDTO.builder()
                        .type("volume")
                        .value(150)
                        .unit("MILLILITERS")
                    .build()))
                .build())
            .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                .builder()
                .errorName("NAME")
                .errorCode(1)
                .errorMessage("Notification message")
                .errorType("TYPE")
                .action("ACTION")
                .reason("REASON")
                .details(List.of("DETAILS_1"))
                .build()))
            .build();

        Mockito.when(requestSpec.retrieveMono(InventoryValidationResponseDTO.class)).thenReturn(Mono.just(dto));

        var target = new InventoryRsocketClient(rsocketRequesterMock,commandMapper,inventoryMapper);

        var response = target.validateInventory(new ValidateInventoryCommand("W036898786799","E0701V00","123456789"));

        StepVerifier.create(response)
            .consumeNextWith(detail -> {
                Assertions.assertNotNull(detail.getInventory());
                Assertions.assertEquals(dto.inventoryResponseDTO().id(), detail.getInventory().getId());
                Assertions.assertEquals(dto.inventoryResponseDTO().unitNumber(), detail.getInventory().getUnitNumber());
                Assertions.assertEquals(dto.inventoryResponseDTO().productCode(), detail.getInventory().getProductCode());
                Assertions.assertEquals(dto.inventoryResponseDTO().locationCode(), detail.getInventory().getLocationCode());
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

        var target = new InventoryRsocketClient(rsocketRequesterMock,commandMapper,inventoryMapper);

        var response = target.validateInventory(new ValidateInventoryCommand("W036898786799","E0701V00","123456789"));

        StepVerifier.create(response)
            .expectError(InventoryServiceNotAvailableException.class)
            .verify();
    }

    @Test
    public void shouldValidateInventoryInBatch() {

        var batchSize = 10;

        var rsocketRequesterMock = Mockito.mock(RSocketRequester.class);

        var requestSpec = Mockito.mock(RSocketRequester.RequestSpec.class);

        Mockito.when(rsocketRequesterMock.route("validateInventoryBatch")).thenReturn(requestSpec);
        Mockito.when(requestSpec.data(Mockito.any())).thenReturn(requestSpec);

        Flux<InventoryValidationResponseDTO> responseFlux = Flux.range(0, batchSize)
            .map(i -> {
                String unitNumber = String.format("W0368250%05d", i);
                return InventoryValidationResponseDTO.builder()
                    .inventoryResponseDTO(InventoryResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .unitNumber(unitNumber)
                        .locationCode("123456789")
                        .productCode("E0701V00")
                        .productDescription("Description")
                        .expirationDate(LocalDateTime.now())
                        .aboRh("AP")
                        .productFamily("Product family")
                        .collectionDate(ZonedDateTime.now())
                        .storageLocation("Storage Location")
                        .createDate(ZonedDateTime.now())
                        .modificationDate(ZonedDateTime.now())
                        .volumes(List.of(InventoryVolumeDTO.builder()
                            .type("volume")
                            .value(150)
                            .unit("MILLILITERS")
                            .build()))
                        .build())
                    .inventoryNotificationsDTO(List.of(InventoryNotificationDTO
                        .builder()
                        .errorName("NAME")
                        .errorCode(1)
                        .errorMessage("Notification message")
                        .errorType("TYPE")
                        .action("ACTION")
                        .reason("REASON")
                        .details(List.of("DETAILS_1"))
                        .build()))
                    .build();

            });

        Mockito.when(requestSpec.retrieveFlux(InventoryValidationResponseDTO.class)).thenReturn(responseFlux);

        var target = new InventoryRsocketClient(rsocketRequesterMock, commandMapper, inventoryMapper);


        Flux<ValidateInventoryCommand> requestFlux = Flux.range(0, batchSize)
            .map(i -> {
                String unitNumber = String.format("W0368250%05d", i);
                return new ValidateInventoryCommand(unitNumber, "E0701V00", "123456789");
            });

        var response = target.validateInventoryBatch(requestFlux);

        StepVerifier.create(response)
            .recordWith(ArrayList::new)
            .expectNextCount(batchSize)
            .consumeRecordedWith(responses -> {
                assertThat(responses).hasSize(batchSize);
                responses.forEach(detail -> {
                    Assertions.assertNotNull(detail.getInventory());
                });
            })
            .verifyComplete();

    }

    @Test
    public void shouldNotValidateInventoryInBatch() {

        var rsocketRequesterMock = Mockito.mock(RSocketRequester.class);

        var requestSpec = Mockito.mock(RSocketRequester.RequestSpec.class);

        Mockito.when(rsocketRequesterMock.route("validateInventoryBatch")).thenReturn(requestSpec);
        Mockito.when(requestSpec.data(Mockito.any())).thenReturn(requestSpec) ;
        Mockito.when(requestSpec.retrieveFlux(InventoryValidationResponseDTO.class)).thenReturn(Flux.error(new RuntimeException("Any error")));

        var target = new InventoryRsocketClient(rsocketRequesterMock,commandMapper,inventoryMapper);

        var response = target.validateInventoryBatch(Flux.just(new ValidateInventoryCommand("W036898786799","E0701V00","123456789")));

        StepVerifier.create(response)
            .expectError(InventoryServiceNotAvailableException.class)
            .verify();

    }

}
