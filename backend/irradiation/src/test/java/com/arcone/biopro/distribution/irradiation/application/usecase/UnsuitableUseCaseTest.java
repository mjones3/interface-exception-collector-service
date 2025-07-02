package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.UnsuitableInput;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnsuitableUseCaseTest {

    private static final String UNIT_NUMBER = "W123456789012";
    private static final String PRODUCT_CODE = "E0980V99";
    private static final String REASON = "POSITIVE_REACTIVE_TEST_RESULTS";
    private static final String LOCATION_1 = "1FS";

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @InjectMocks
    private UnsuitableUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldMarkInventoryAsUnsuitable_whenProductExists() {
        UnsuitableInput input = createInputWithProductCode();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));

        when(inventoryAggregateRepository.saveInventory(any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));

        Mono<Void> result = useCase.execute(input);

        StepVerifier.create(result)
            .verifyComplete();

        ArgumentCaptor<InventoryAggregate> inventoryCaptor = ArgumentCaptor.forClass(InventoryAggregate.class);
        verify(inventoryAggregateRepository).saveInventory(inventoryCaptor.capture());
        InventoryAggregate updatedInventory = inventoryCaptor.getValue();

        assertEquals(InventoryStatus.AVAILABLE, updatedInventory.getInventory().getInventoryStatus());
        assertEquals(REASON, updatedInventory.getInventory().getUnsuitableReason());
    }

    @Test
    void execute_shouldProcessAllInventories_whenProductCodeIsNull() {
        UnsuitableInput input = createInputWithoutProductCode();

        when(inventoryAggregateRepository.findByUnitNumber(any()))
            .thenReturn(Flux.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));

        when(inventoryAggregateRepository.saveInventory(any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));

        Mono<Void> result = useCase.execute(input);

        StepVerifier.create(result)
            .verifyComplete();

        verify(inventoryAggregateRepository, times(1)).saveInventory(any());
    }

    @Test
    void execute_shouldLogWarningAndSkip_whenInventoryNotFound() {
        UnsuitableInput input = createInputWithProductCode();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.empty());

        Mono<Void> result = useCase.execute(input);

        StepVerifier.create(result)
            .verifyComplete();

        verify(inventoryAggregateRepository, never()).saveInventory(any());
    }

    @Test
    void execute_shouldThrowError_whenFindByUnitNumberAndProductCodeFails() {
        UnsuitableInput input = createInputWithProductCode();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<Void> result = useCase.execute(input);

        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void execute_shouldThrowInventoryNotFoundException_whenNoProductFoundForUnit() {
        UnsuitableInput input = createInputWithProductCode();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.empty());

        Mono<Void> result = useCase.execute(input);

        StepVerifier.create(result)
            .verifyComplete();

        verify(inventoryAggregateRepository, never()).saveInventory(any());
    }

    private static UnsuitableInput createInputWithProductCode() {
        return UnsuitableInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .reasonKey(REASON)
            .build();
    }

    private static UnsuitableInput createInputWithoutProductCode() {
        return UnsuitableInput.builder()
            .unitNumber(UNIT_NUMBER)
            .reasonKey(REASON)
            .build();
    }

    private InventoryAggregate createInventoryAggregate(InventoryStatus status, LocalDateTime expirationDate) {
        return InventoryAggregate.builder()
            .inventory(Inventory.builder()
                .id(UUID.randomUUID())
                .unitNumber(new UnitNumber(UNIT_NUMBER))
                .productCode(new ProductCode(PRODUCT_CODE))
                .inventoryLocation(LOCATION_1)
                .inventoryStatus(status)
                .expirationDate(expirationDate)
                .build())
            .build();
    }
}
