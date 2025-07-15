//package com.arcone.biopro.distribution.irradiation.unit.application.usecase;
//
//import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
//import com.arcone.biopro.distribution.irradiation.application.mapper.IrradiationInventoryMapper;
//import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
//import com.arcone.biopro.distribution.irradiation.domain.exception.NoEligibleProductForIrradiationException;
//import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
//import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.InventoryClient;
//import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
//import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.UnitNumber;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import reactor.core.publisher.Flux;
//import reactor.test.StepVerifier;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ValidateUnitNumberUseCaseTest {
//
//    @Mock
//    private InventoryClient inventoryClient;
//
//    @Mock
//    private IrradiationInventoryOutput validInventory;
//
//    @Mock
//    private IrradiationInventoryMapper invalidInventory;
//
//    @InjectMocks
//    private ValidateUnitNumberUseCase validateUnitNumberUseCase;
//
//    @Test
//    @DisplayName("Should return valid inventories when unit number exists and matches location")
//    void execute_ShouldReturnValidInventories_WhenUnitNumberExistsAndMatchesLocation() {
//        String unitNumber = "W777725001001";
//        String location = "123456789";
//        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
//                .thenReturn(Flux.just(validInventory, invalidInventory));
//        when(validInventory.isAvailable()).thenReturn(true);
//        when(validInventory.isAtLocation(any(Location.class))).thenReturn(true);
//        when(invalidInventory.isAvailable()).thenReturn(false);
//
//        Flux<IrradiationInventoryOutput> result = validateUnitNumberUseCase.execute(unitNumber, location);
//
//        StepVerifier.create(result)
//                .expectNext(validInventory)
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Should throw NoEligibleProductForIrradiationException when no inventory found")
//    void execute_ShouldThrowException_WhenNoInventoryFound() {
//        String unitNumber = "INVALID-UNIT";
//        String location = "123456789";
//        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
//                .thenReturn(Flux.empty());
//
//        Flux<Inventory> result = validateUnitNumberUseCase.execute(unitNumber, location);
//
//        StepVerifier.create(result)
//                .expectError(NoEligibleProductForIrradiationException.class)
//                .verify();
//    }
//
//    @Test
//    @DisplayName("Should throw NoEligibleProductForIrradiationException when inventory not at target location")
//    void execute_ShouldThrowException_WhenInventoryNotAtTargetLocation() {
//        String unitNumber = "W777725001001";
//        String location = "123456789";
//        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
//                .thenReturn(Flux.just(validInventory));
//        when(validInventory.isAvailable()).thenReturn(true);
//        when(validInventory.isAtLocation(any(Location.class))).thenReturn(false);
//
//        Flux<Inventory> result = validateUnitNumberUseCase.execute(unitNumber, location);
//
//        StepVerifier.create(result)
//                .expectError(NoEligibleProductForIrradiationException.class)
//                .verify();
//    }
//
//    @Test
//    @DisplayName("Should propagate error when inventory client fails")
//    void execute_ShouldPropagateError_WhenInventoryClientFails() {
//        String unitNumber = "W777725001001";
//        String location = "123456789";
//        when(inventoryClient.getInventoryByUnitNumber(any(UnitNumber.class)))
//                .thenReturn(Flux.error(new RuntimeException("Client error")));
//
//        Flux<Inventory> result = validateUnitNumberUseCase.execute(unitNumber, location);
//
//        StepVerifier.create(result)
//                .expectError(RuntimeException.class)
//                .verify();
//    }
//}
