package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductDiscardedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.common.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductDiscardedUseCaseTest {

    private static final String UNIT_NUMBER = "W123456789012";
    private static final String PRODUCT_CODE = "E0980V99";
    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    static String LOCATION_1 = "LOCATION_1";
    static String ADDITIVE_SOLUTION_ISSUES = "ADDITIVE_SOLUTION_ISSUES";
    static String COMMENTS = "Some comments here";

    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private ProductDiscardedUseCase useCase;

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldDiscard_inventory_is_valid() {
        ProductDiscardedInput input = createInput();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));

        when(inventoryAggregateRepository.saveInventory(any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.DISCARDED, LocalDateTime.now().plusDays(1))));

        Mono<InventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.inventoryStatus()).isEqualTo(InventoryStatus.DISCARDED);
            })
            .verifyComplete();

        ArgumentCaptor<InventoryAggregate> laCaptor = ArgumentCaptor.forClass(InventoryAggregate.class);
        verify(inventoryAggregateRepository).saveInventory(laCaptor.capture());
        InventoryAggregate inventoryAggregateUpdated = laCaptor.getValue();

        assertEquals(InventoryStatus.DISCARDED, inventoryAggregateUpdated.getInventory().getInventoryStatus());
        assertEquals(ADDITIVE_SOLUTION_ISSUES, inventoryAggregateUpdated.getInventory().getStatusReason());
        assertEquals(COMMENTS, inventoryAggregateUpdated.getInventory().getComments());
    }

    private static ProductDiscardedInput createInput() {
        return ProductDiscardedInput.builder()
            .unitNumber(UNIT_NUMBER)
            .productCode(PRODUCT_CODE)
            .reason(ADDITIVE_SOLUTION_ISSUES)
            .comments(COMMENTS)
            .build();
    }

    @Test
    void execute_shouldValidate_inventory_is_not_found() {
        ProductDiscardedInput input = createInput();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(any(), any()))
            .thenReturn(Mono.empty());


        Mono<InventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .expectError(InventoryNotFoundException.class)
            .verify();
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
                    .quarantines(TestUtil.createQuarantines())
                    .build())
            .build();
    }




}
