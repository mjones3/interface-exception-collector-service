package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.AddQuarantineInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.Product;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.irradiation.domain.service.TextConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AddQuarantinedUseCaseTest {

    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @Mock
    TextConfigService textConfigService;

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private AddQuarantinedUseCase addQuarantinedUseCase;

    private AddQuarantineInput addQuarantineInput;
    private InventoryAggregate inventoryAggregate;

    @BeforeEach
    void setUp() {
        openMocks(this);

        addQuarantineInput = new AddQuarantineInput(
            Product.builder().unitNumber("W777724111111").productCode("E1624V00").build(),
            1L,
            "Contamination",
            "Suspected contamination"
        );

        Inventory inventory = Inventory.builder()
            .unitNumber(new UnitNumber("W777724111111"))
            .productCode(new ProductCode("E1624V00"))
            .expirationDate(LocalDateTime.now().plusDays(1))
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .build();

        inventoryAggregate = InventoryAggregate.builder().inventory(inventory).build();

        mapper.setTextConfigService(textConfigService);

        when(textConfigService.getText(anyString(), anyString())).thenReturn("");
    }

    @Test
    void execute_ShouldReturnInventoryOutput_WhenInventoryIsFoundAndQuarantineIsAdded() {
        // Arrange
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString()))
            .thenReturn(Mono.just(inventoryAggregate));

        when(inventoryAggregateRepository.saveInventory(any(InventoryAggregate.class)))
            .thenReturn(Mono.just(inventoryAggregate));

        // Act
        Mono<InventoryOutput> result = addQuarantinedUseCase.execute(addQuarantineInput);

        // Assert
        StepVerifier.create(result)
            .expectNextMatches(Objects::nonNull)
            .verifyComplete();

        assertThat(inventoryAggregate.getInventory().getQuarantines()).hasSize(1);
        assertThat(inventoryAggregate.getInventory().getQuarantines().get(0).reason()).isEqualTo("Contamination");

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode("W777724111111", "E1624V00");
        verify(inventoryAggregateRepository).saveInventory(inventoryAggregate);
    }

    @Test
    void execute_ShouldThrowInventoryNotFoundException_WhenInventoryIsNotFound() {
        // Arrange
        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString()))
            .thenReturn(Mono.empty());

        // Act
        Mono<InventoryOutput> result = addQuarantinedUseCase.execute(addQuarantineInput);

        // Assert
        StepVerifier.create(result)
            .expectError(InventoryNotFoundException.class)
            .verify();

        verify(inventoryAggregateRepository).findByUnitNumberAndProductCode("W777724111111", "E1624V00");
        verify(inventoryAggregateRepository, never()).saveInventory(any());
        verify(mapper, never()).toOutput(any(Inventory.class));
    }
}
