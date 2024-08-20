package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.ValidateInventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ErrorMessage;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ValidateInventoryUseCaseTest {

    private static final String UNIT_NUMBER = "W123456789012";
    private static final String PRODUCT_CODE = "E0980V99";
    @Mock
    private InventoryAggregateRepository inventoryAggregateRepository;

    String LOCATION_1 = "LOCATION_1";


    @Spy
    private InventoryOutputMapper mapper = Mappers.getMapper(InventoryOutputMapper.class);

    @InjectMocks
    private ValidateInventoryUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldValidate_inventory_is_valid() {
        InventoryInput input = new InventoryInput(UNIT_NUMBER, PRODUCT_CODE, LOCATION_1, null, null, null, null , null);

        when(inventoryAggregateRepository.findExistentByUnitNumberAndProductCodeAndLocation(any(), any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().plusDays(1))));


        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.errorMessage()).isNull();
            })
            .verifyComplete();
    }

    @Test
    void execute_shouldValidate_inventory_is_expired() {
        InventoryInput input = new InventoryInput(UNIT_NUMBER, PRODUCT_CODE, LOCATION_1, null, null, null, null , null);

        when(inventoryAggregateRepository.findExistentByUnitNumberAndProductCodeAndLocation(any(), any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.AVAILABLE, LocalDateTime.now().minusDays(1))));


        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.errorMessage()).isEqualTo(ErrorMessage.DATE_EXPIRED);
            })
            .verifyComplete();
    }

    @Test
    void execute_shouldValidate_inventory_is_quarantined() {
        InventoryInput input = new InventoryInput(UNIT_NUMBER, PRODUCT_CODE, LOCATION_1, null, null, null, null , null);

        when(inventoryAggregateRepository.findExistentByUnitNumberAndProductCodeAndLocation(any(), any(), any()))
            .thenReturn(Mono.just(createInventoryAggregate(InventoryStatus.QUARANTINED, LocalDateTime.now().plusDays(1))));


        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.errorMessage()).isEqualTo(ErrorMessage.STATUS_IN_QUARANTINE);
            })
            .verifyComplete();
    }

    @Test
    void execute_shouldValidate_inventory_is_not_found() {
        InventoryInput input = new InventoryInput(UNIT_NUMBER, PRODUCT_CODE, LOCATION_1, null, null, null, null , null);

        when(inventoryAggregateRepository.findExistentByUnitNumberAndProductCodeAndLocation(any(), any(), any()))
            .thenReturn(Mono.empty());


        Mono<ValidateInventoryOutput> result = useCase.execute(input);

        StepVerifier.create(result)
            .consumeNextWith(output -> {
                assertThat(output).isNotNull();
                assertThat(output.errorMessage()).isEqualTo(ErrorMessage.INVENTORY_NOT_FOUND);
            })
            .verifyComplete();
    }

    private InventoryAggregate createInventoryAggregate(InventoryStatus status, LocalDateTime expirationDate) {
        return InventoryAggregate.builder().inventory(new Inventory(UUID.randomUUID(), new UnitNumber(UNIT_NUMBER), new ProductCode(PRODUCT_CODE), "shortDescription", status, expirationDate, "", LOCATION_1, ProductFamily.PLASMA_TRANSFUSABLE, AboRhType.OP, null, null)).build();
    }


}
