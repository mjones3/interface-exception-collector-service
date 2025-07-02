package com.arcone.biopro.distribution.irradiation.integration;

import com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto.InventoryValidationRequest;
import com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto.InventoryValidationResponseDTO;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.ValidateInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.usecase.UseCase;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.rsocket.server.port=7002"
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations="classpath:application-test.properties")
public class ValidateInventorySocketServerIT {

    @Autowired
    private RSocketRequester.Builder requesterBuilder;

    @MockBean
    private UseCase<Mono<ValidateInventoryOutput>, InventoryInput> validateInventoryUseCase;

    private RSocketRequester requester;

    private static final String UNIT_NUMBER = "W777725015001";
    private static final String PRODUCT_CODE = "E0869V02";
    private static final String LOCATION_CODE = "LOCATION_1";

    @BeforeEach
    void setUp() {
        requester = requesterBuilder
            .tcp("localhost", 7002);
    }

    @Test
    @DisplayName("Should validate irradiation successfully with no notifications")
    public void testValidateInventorySuccess() {
        // Arrange
        InventoryOutput inventoryOutput = buildInventory(UNIT_NUMBER, LOCATION_CODE);

        ValidateInventoryOutput output = new ValidateInventoryOutput(
            inventoryOutput,
            Collections.emptyList()
        );

        when(validateInventoryUseCase.execute(any(InventoryInput.class)))
            .thenReturn(Mono.just(output));

        // Act
        Mono<InventoryValidationResponseDTO> result = requester
            .route("validateInventory")
            .data(new InventoryValidationRequest(UNIT_NUMBER, PRODUCT_CODE, LOCATION_CODE))
            .retrieveMono(InventoryValidationResponseDTO.class);

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertThat(response).isNotNull();
                assertThat(response.inventoryResponseDTO()).isNotNull();
                assertThat(response.inventoryResponseDTO().unitNumber()).isEqualTo(UNIT_NUMBER);
                assertThat(response.inventoryResponseDTO().productCode()).isEqualTo(PRODUCT_CODE);
                assertThat(response.inventoryResponseDTO().locationCode()).isEqualTo(LOCATION_CODE);
                assertThat(response.inventoryNotificationsDTO()).isEmpty();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate irradiation and return expired notification")
    public void testValidateInventoryExpired() {
        // Arrange
        InventoryOutput inventoryOutput = buildInventory(UNIT_NUMBER, LOCATION_CODE);

        NotificationMessage notification = NotificationMessage.builder()
            .name(MessageType.INVENTORY_IS_EXPIRED.name())
            .code(MessageType.INVENTORY_IS_EXPIRED.getCode())
            .message("Product is expired")
            .type(MessageType.INVENTORY_IS_EXPIRED.name())
            .build();

        ValidateInventoryOutput output = new ValidateInventoryOutput(
            inventoryOutput,
            List.of(notification)
        );

        when(validateInventoryUseCase.execute(any(InventoryInput.class)))
            .thenReturn(Mono.just(output));

        // Act
        Mono<InventoryValidationResponseDTO> result = requester
            .route("validateInventory")
            .data(new InventoryValidationRequest(UNIT_NUMBER, PRODUCT_CODE, LOCATION_CODE))
            .retrieveMono(InventoryValidationResponseDTO.class);

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertThat(response).isNotNull();
                assertThat(response.inventoryResponseDTO()).isNotNull();
                assertThat(response.inventoryResponseDTO().unitNumber()).isEqualTo(UNIT_NUMBER);
                assertThat(response.inventoryResponseDTO().productCode()).isEqualTo(PRODUCT_CODE);
                assertThat(response.inventoryResponseDTO().locationCode()).isEqualTo(LOCATION_CODE);
                assertThat(response.inventoryNotificationsDTO()).hasSize(1);
                assertThat(response.inventoryNotificationsDTO().get(0).errorName())
                    .isEqualTo(MessageType.INVENTORY_IS_EXPIRED.name());
                assertThat(response.inventoryNotificationsDTO().get(0).errorCode())
                    .isEqualTo(MessageType.INVENTORY_IS_EXPIRED.getCode());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate irradiation and return quarantined notification")
    public void testValidateInventoryQuarantined() {
        // Arrange
        InventoryOutput inventoryOutput = buildInventory(UNIT_NUMBER, LOCATION_CODE);

        NotificationMessage notification = NotificationMessage.builder()
            .name(MessageType.INVENTORY_IS_QUARANTINED.name())
            .code(MessageType.INVENTORY_IS_QUARANTINED.getCode())
            .message("Product is quarantined")
            .type(MessageType.INVENTORY_IS_QUARANTINED.name())
            .details(List.of("Quarantine reason 1", "Quarantine reason 2"))
            .build();

        ValidateInventoryOutput output = new ValidateInventoryOutput(
            inventoryOutput,
            List.of(notification)
        );

        when(validateInventoryUseCase.execute(any(InventoryInput.class)))
            .thenReturn(Mono.just(output));

        // Act
        Mono<InventoryValidationResponseDTO> result = requester
            .route("validateInventory")
            .data(new InventoryValidationRequest(UNIT_NUMBER, PRODUCT_CODE, LOCATION_CODE))
            .retrieveMono(InventoryValidationResponseDTO.class);

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertThat(response).isNotNull();
                assertThat(response.inventoryResponseDTO()).isNotNull();
                assertThat(response.inventoryResponseDTO().unitNumber()).isEqualTo(UNIT_NUMBER);
                assertThat(response.inventoryResponseDTO().productCode()).isEqualTo(PRODUCT_CODE);
                assertThat(response.inventoryResponseDTO().locationCode()).isEqualTo(LOCATION_CODE);
                assertThat(response.inventoryNotificationsDTO()).hasSize(1);
                assertThat(response.inventoryNotificationsDTO().get(0).errorName())
                    .isEqualTo(MessageType.INVENTORY_IS_QUARANTINED.name());
                assertThat(response.inventoryNotificationsDTO().get(0).errorCode())
                    .isEqualTo(MessageType.INVENTORY_IS_QUARANTINED.getCode());
                assertThat(response.inventoryNotificationsDTO().get(0).details())
                    .containsExactly("Quarantine reason 1", "Quarantine reason 2");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate irradiation and return not found notification")
    public void testValidateInventoryNotFound() {
        // Arrange
        NotificationMessage notification = NotificationMessage.builder()
            .name(MessageType.INVENTORY_NOT_EXIST.name())
            .code(MessageType.INVENTORY_NOT_EXIST.getCode())
            .message("Inventory not found")
            .type(MessageType.INVENTORY_NOT_EXIST.name())
            .build();

        ValidateInventoryOutput output = new ValidateInventoryOutput(
            null,
            List.of(notification)
        );

        when(validateInventoryUseCase.execute(any(InventoryInput.class)))
            .thenReturn(Mono.just(output));

        // Act
        Mono<InventoryValidationResponseDTO> result = requester
            .route("validateInventory")
            .data(new InventoryValidationRequest(UNIT_NUMBER, PRODUCT_CODE, LOCATION_CODE))
            .retrieveMono(InventoryValidationResponseDTO.class);

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertThat(response).isNotNull();
                assertThat(response.inventoryResponseDTO()).isNull();
                assertThat(response.inventoryNotificationsDTO()).hasSize(1);
                assertThat(response.inventoryNotificationsDTO().get(0).errorName())
                    .isEqualTo(MessageType.INVENTORY_NOT_EXIST.name());
                assertThat(response.inventoryNotificationsDTO().get(0).errorCode())
                    .isEqualTo(MessageType.INVENTORY_NOT_EXIST.getCode());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate irradiation and return not found in location notification")
    public void testValidateInventoryNotFoundInLocation() {
        // Arrange
        InventoryOutput inventoryOutput = buildInventory(UNIT_NUMBER, "DIFFERENT_LOCATION");

        NotificationMessage notification = NotificationMessage.builder()
            .name(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.name())
            .code(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.getCode())
            .message("Inventory not found in location")
            .type(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.name())
            .build();

        ValidateInventoryOutput output = new ValidateInventoryOutput(
            inventoryOutput,
            List.of(notification)
        );

        when(validateInventoryUseCase.execute(any(InventoryInput.class)))
            .thenReturn(Mono.just(output));

        // Act
        Mono<InventoryValidationResponseDTO> result = requester
            .route("validateInventory")
            .data(new InventoryValidationRequest(UNIT_NUMBER, PRODUCT_CODE, LOCATION_CODE))
            .retrieveMono(InventoryValidationResponseDTO.class);

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertThat(response).isNotNull();
                assertThat(response.inventoryResponseDTO()).isNotNull();
                assertThat(response.inventoryResponseDTO().unitNumber()).isEqualTo(UNIT_NUMBER);
                assertThat(response.inventoryResponseDTO().productCode()).isEqualTo(PRODUCT_CODE);
                assertThat(response.inventoryResponseDTO().locationCode()).isEqualTo("DIFFERENT_LOCATION");
                assertThat(response.inventoryNotificationsDTO()).hasSize(1);
                assertThat(response.inventoryNotificationsDTO().get(0).errorName())
                    .isEqualTo(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.name());
                assertThat(response.inventoryNotificationsDTO().get(0).errorCode())
                    .isEqualTo(MessageType.INVENTORY_NOT_FOUND_IN_LOCATION.getCode());
            })
            .verifyComplete();
    }

    /**
     * Test method for validateInventoryBatch that creates 10,000 irradiation items.
     *
     * This test verifies that the validateInventoryBatch method can handle a large batch
     * of irradiation validation requests (10,000 items) and correctly process them.
     *
     * The test includes different scenarios:
     * - 9700 successful validations (no notifications)
     * - 100 expired items (with INVENTORY_IS_EXPIRED notification)
     * - 100 quarantined items (with INVENTORY_IS_QUARANTINED notification)
     * - 100 not found items (with INVENTORY_NOT_EXIST notification)
     *
     * The test verifies that all 10,000 requests are processed and that the correct
     * number of each type of response is received.
     */
    @Test
    @DisplayName("Should validate 10,000 irradiation items in batch")
    public void testValidateInventoryBatchWith10000Items() {
        // Arrange
        final int BATCH_SIZE = 10000;
        final AtomicInteger counter = new AtomicInteger(0);

        // Create a list to hold all the mock responses
        List<ValidateInventoryOutput> mockResponses = new ArrayList<>(BATCH_SIZE);

        // Generate 10,000 mock responses with different scenarios:
        // - 9700 successful validations
        // - 100 expired items
        // - 100 quarantined items
        // - 100 not found items
        for (int i = 0; i < BATCH_SIZE; i++) {
            String unitNumber = String.format("W7777250%05d", i); // Generate unique unit numbers

            if (i < 9700) {
                // Successful validation
                InventoryOutput inventoryOutput = buildInventory(unitNumber, LOCATION_CODE);

                ValidateInventoryOutput output = new ValidateInventoryOutput(
                    inventoryOutput,
                    Collections.emptyList() // No notifications for successful validations
                );

                mockResponses.add(output);
            } else if (i < 9800) {
                // Expired items
                InventoryOutput inventoryOutput = buildInventory(unitNumber, LOCATION_CODE);

                NotificationMessage notification = NotificationMessage.builder()
                    .name(MessageType.INVENTORY_IS_EXPIRED.name())
                    .code(MessageType.INVENTORY_IS_EXPIRED.getCode())
                    .message("Product is expired")
                    .type(MessageType.INVENTORY_IS_EXPIRED.name())
                    .build();

                ValidateInventoryOutput output = new ValidateInventoryOutput(
                    inventoryOutput,
                    List.of(notification)
                );

                mockResponses.add(output);
            } else if (i < 9900) {
                // Quarantined items
                InventoryOutput inventoryOutput = buildInventory(unitNumber, LOCATION_CODE);

                NotificationMessage notification = NotificationMessage.builder()
                    .name(MessageType.INVENTORY_IS_QUARANTINED.name())
                    .code(MessageType.INVENTORY_IS_QUARANTINED.getCode())
                    .message("Product is quarantined")
                    .type(MessageType.INVENTORY_IS_QUARANTINED.name())
                    .details(List.of("Quarantine reason"))
                    .build();

                ValidateInventoryOutput output = new ValidateInventoryOutput(
                    inventoryOutput,
                    List.of(notification)
                );

                mockResponses.add(output);
            } else {
                // Not found items
                NotificationMessage notification = NotificationMessage.builder()
                    .name(MessageType.INVENTORY_NOT_EXIST.name())
                    .code(MessageType.INVENTORY_NOT_EXIST.getCode())
                    .message("Inventory not found")
                    .type(MessageType.INVENTORY_NOT_EXIST.name())
                    .build();

                ValidateInventoryOutput output = new ValidateInventoryOutput(
                    null,
                    List.of(notification)
                );

                mockResponses.add(output);
            }
        }

        // Setup mock to return the appropriate response based on the input
        when(validateInventoryUseCase.execute(any(InventoryInput.class)))
            .thenAnswer(invocation -> {
                InventoryInput input = invocation.getArgument(0);
                // Find the matching response based on the unit number
                String unitNumber = input.unitNumber();
                int index = Integer.parseInt(unitNumber.substring(9)); // Extract the index from the unit number
                return Mono.just(mockResponses.get(index));
            });

        // Create a flux of 10,000 irradiation validation requests
        Flux<InventoryValidationRequest> requestFlux = Flux.range(0, BATCH_SIZE)
            .map(i -> {
                String unitNumber = String.format("W7777250%05d", i);
                return InventoryValidationRequest.builder()
                    .unitNumber(unitNumber)
                    .productCode(PRODUCT_CODE)
                    .locationCode(LOCATION_CODE)
                    .build();
            });

        // Act
        Flux<InventoryValidationResponseDTO> resultFlux = requester
            .route("validateInventoryBatch")
            .data(requestFlux)
            .retrieveFlux(InventoryValidationResponseDTO.class);

        // Assert
        // Count the different types of responses
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger expiredCount = new AtomicInteger(0);
        AtomicInteger quarantinedCount = new AtomicInteger(0);
        AtomicInteger notFoundCount = new AtomicInteger(0);

        StepVerifier.create(resultFlux)
            .recordWith(ArrayList::new)
            .expectNextCount(BATCH_SIZE) // Expect 10,000 responses
            .consumeRecordedWith(responses -> {
                assertThat(responses).hasSize(BATCH_SIZE);

                // Count the different types of responses
                for (InventoryValidationResponseDTO response : responses) {
                    if (response.inventoryNotificationsDTO().isEmpty()) {
                        successCount.incrementAndGet();
                    } else {
                        String errorName = response.inventoryNotificationsDTO().getFirst().errorName();
                        if (MessageType.INVENTORY_IS_EXPIRED.name().equals(errorName)) {
                            expiredCount.incrementAndGet();
                        } else if (MessageType.INVENTORY_IS_QUARANTINED.name().equals(errorName)) {
                            quarantinedCount.incrementAndGet();
                        } else if (MessageType.INVENTORY_NOT_EXIST.name().equals(errorName)) {
                            notFoundCount.incrementAndGet();
                        }
                    }
                }

                // Verify the counts
                assertThat(successCount.get()).isEqualTo(9700);
                assertThat(expiredCount.get()).isEqualTo(100);
                assertThat(quarantinedCount.get()).isEqualTo(100);
                assertThat(notFoundCount.get()).isEqualTo(100);
            })
            .verifyComplete();
    }

    private static InventoryOutput buildInventory(String unitNumber, String locationCode) {
        return InventoryOutput.builder()
            .unitNumber(unitNumber)
            .productCode(PRODUCT_CODE)
            .location(locationCode)
            .build();
    }
}




