package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.Product;
import com.arcone.biopro.distribution.inventory.application.dto.UpdateQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.inventory.domain.service.TextConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class UpdateQuarantinedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Mock
    TextConfigService textConfigService;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private UpdateQuarantinedUseCase updateQuarantinedUseCase;

    private UpdateQuarantineInput updateQuarantineInput;
    private InventoryAggregate inventoryAggregate;

    @BeforeEach
    void setUp() {
        openMocks(this);

        updateQuarantineInput = new UpdateQuarantineInput(
            Product.builder().unitNumber("W777724111111").productCode("E1624V00").build(),
            1L, "OTHER", "Other comment", false
        );

        Inventory inventory = Inventory.builder()
            .unitNumber(new UnitNumber("W777724111111"))
            .productCode(new ProductCode("E1624V00"))
            .histories(new ArrayList<>(List.of(new History(InventoryStatus.AVAILABLE, null, null))))
            .expirationDate(LocalDateTime.now().plusDays(1))
            .quarantines(new ArrayList<>(List.of(
                new Quarantine(1L, "Contamination", "Suspected contamination", false)
            )))
            .build();

        inventoryAggregate = InventoryAggregate.builder().inventory(inventory).build();

        mapper.setTextConfigService(textConfigService);

        when(textConfigService.getText(anyString(), anyString())).thenReturn("");
    }

    @Test
    @DisplayName("Should Return An InventoryOutput When Quarantines Is Updated")
    void execute_ShouldReturnInventoryOutput_WhenInventoryIsFoundAndQuarantineIsUpdated() {
        // Arrange
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString()))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryAggregateRepository.saveInventory(any(InventoryAggregate.class)))
            .thenReturn(Mono.just(inventoryAggregate));

        // Act
        Mono<InventoryOutput> result = updateQuarantinedUseCase.execute(updateQuarantineInput);

        // Assert
        StepVerifier.create(result)
            .expectNextMatches(Objects::nonNull)
            .verifyComplete();

        assertThat(inventoryAggregate.getInventory().getQuarantines()).isNotEmpty();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode("W777724111111", "E1624V00");
        verify(inventoryAggregateRepository, times(1)).saveInventory(inventoryAggregate);
    }

    @Test
    void execute_ShouldThrowInventoryNotFoundException_WhenInventoryIsNotFound() {
        // Arrange
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString()))
            .thenReturn(Mono.empty());

        // Act
        Mono<InventoryOutput> result = updateQuarantinedUseCase.execute(updateQuarantineInput);

        // Assert
        StepVerifier.create(result)
            .expectError(InventoryNotFoundException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode("W777724111111", "E1624V00");
        verify(inventoryAggregateRepository, never()).saveInventory(any());
        verify(mapper, never()).toOutput(any(Inventory.class));
    }
}
