package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.dto.GetInventoryByUnitNumberAndProductInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.usecase.GetInventoryByUnitNumberAndProductCodeUseCase;
import com.arcone.biopro.distribution.inventory.application.usecase.GetInventoryByUnitNumberUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.rsocket.server.port=7002"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations="classpath:application-test.properties")
class GetInventoryByUnitNumberSocketServerIT {

    @Autowired
    private RSocketRequester.Builder requesterBuilder;

    @MockitoBean
    private GetInventoryByUnitNumberUseCase getByUnitNumberUseCase;

    @MockitoBean
    private GetInventoryByUnitNumberAndProductCodeUseCase getByUnitNumberAndProductCodeUseCase;

    private RSocketRequester requester;

    @BeforeEach
    void setUp() {
        requester = requesterBuilder.tcp("localhost", 7002);
    }

    private static final String UNIT_NUMBER = "W036800000001";
    private static final String PRODUCT_CODE = "PROD001";

    @Test
    void getInventoryByUnitNumber_ShouldReturnInventory_WhenUnitNumberExists() {
        // Given
        InventoryOutput mockInventory = InventoryOutput.builder()
            .unitNumber(UNIT_NUMBER)
            .build();

        when(getByUnitNumberUseCase.execute(anyString()))
            .thenReturn(Flux.just(mockInventory));

        // When
        Flux<InventoryOutput> result = requester
            .route("getInventoryByUnitNumber")
            .data(UNIT_NUMBER)
            .retrieveFlux(InventoryOutput.class);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(output -> output.unitNumber().equals(UNIT_NUMBER))
            .verifyComplete();
    }

    @Test
    void getInventoryByUnitNumber_ShouldReturnEmpty_WhenUnitNumberNotFound() {
        // Given
        when(getByUnitNumberUseCase.execute(anyString()))
            .thenReturn(Flux.empty());

        // When
        Flux<InventoryOutput> result = requester
            .route("getInventoryByUnitNumber")
            .data(UNIT_NUMBER)
            .retrieveFlux(InventoryOutput.class);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void getInventoryByUnitNumberAndProductCode_ShouldReturnInventory_WhenExists() {
        // Given
        InventoryOutput mockInventory = InventoryOutput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .build();

        when(getByUnitNumberAndProductCodeUseCase.execute(any()))
            .thenReturn(Mono.just(mockInventory));

        GetInventoryByUnitNumberAndProductInput input =
            new GetInventoryByUnitNumberAndProductInput(UNIT_NUMBER, PRODUCT_CODE);

        // When
        Mono<InventoryOutput> result = requester
            .route("getInventoryByUnitNumberAndProductCode")
            .data(input)
            .retrieveMono(InventoryOutput.class);

        // Then
        StepVerifier.create(result)
            .expectNextMatches(output ->
                output.unitNumber().equals(UNIT_NUMBER) &&
                output.productCode().equals(PRODUCT_CODE))
            .verifyComplete();
    }

    @Test
    void getInventoryByUnitNumberAndProductCode_ShouldReturnEmpty_WhenNotFound() {
        // Given
        when(getByUnitNumberAndProductCodeUseCase.execute(any()))
            .thenReturn(Mono.empty());

        GetInventoryByUnitNumberAndProductInput input =
            new GetInventoryByUnitNumberAndProductInput(UNIT_NUMBER, PRODUCT_CODE);

        // When
        Mono<InventoryOutput> result = requester
            .route("getInventoryByUnitNumberAndProductCode")
            .data(input)
            .retrieveMono(InventoryOutput.class);

        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }
}
