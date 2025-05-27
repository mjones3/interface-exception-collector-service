package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CloseCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RemoveCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RepackCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.SystemProcessProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.VerifyItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.InventoryService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.LabelTemplateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartonTest {

    @Mock
    private CartonRepository cartonRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;

    @Test
    void shouldCreateNewCarton() {
        // Given

        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(recoveredPlasmaShipment.getId()).thenReturn(1L);
        Mockito.when(recoveredPlasmaShipment.getLocationCode()).thenReturn("locationCode");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(recoveredPlasmaShipment));

        Mockito.when(cartonRepository.countByShipment(Mockito.anyLong())).thenReturn(Mono.just(0));


        Location location = mock(Location.class);

        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just( 123L));

        when(location.findProperty("RPS_CARTON_PARTNER_PREFIX")).thenReturn(Optional.of(new LocationProperty(1L,"RPS_CARTON_PARTNER_PREFIX","BPM")));
        when(location.findProperty("RPS_LOCATION_CARTON_CODE")).thenReturn(Optional.of(new LocationProperty(1L,"RPS_LOCATION_CARTON_CODE","MH1")));
        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just(1L));

        // When
        Carton carton = Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), recoveredPlasmaShippingRepository, cartonRepository, locationRepository);

        // Then
        assertNotNull(carton);

        assertEquals(1,carton.getCartonSequence());
        assertEquals("BPMMH11",carton.getCartonNumber());
        assertNull(carton.getId());
        assertEquals(1L,carton.getShipmentId());
        assertEquals("create-employee-id",carton.getCreateEmployeeId());
        assertEquals("OPEN", carton.getStatus());
        assertNull(carton.getCloseEmployeeId());
        assertNull(carton.getCloseDate());
        assertEquals(0,carton.getTotalProducts());
        assertEquals(BigDecimal.ZERO,carton.getTotalWeight());
        assertEquals(BigDecimal.ZERO,carton.getTotalVolume());

        verify(cartonRepository).getNextCartonId();
        verify(locationRepository).findOneByCode(anyString());
        verify(cartonRepository).countByShipment(anyLong());
        verify(recoveredPlasmaShippingRepository).findOneById(anyLong());
    }

    @Test
    void shouldValidateCompleteCarton() {

        // When/Then
        assertDoesNotThrow(() -> Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO,Collections.emptyList(),0,0));

    }

    @Test
    void shouldThrowExceptionWhenShipmentIdIsNull() {

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L,"number",null,1,"employee-id","close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO,Collections.emptyList(),0,0));
        assertEquals("Shipment Id is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateEmployeeIdIsNull() {

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L,"number",1L,1,null,"close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO,Collections.emptyList(),0,0));
        assertEquals("Create Employee Id is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonNumberIsNull() {

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () ->  Carton.fromRepository(1L,null,1L,1,"employee-id","close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO,Collections.emptyList(),0,0));
        assertEquals("Carton Number is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonSequenceIsNull() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L,"number",1L,null,"employee-id","close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO,Collections.emptyList(),0,0));
        assertEquals("Carton Sequence is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonSequenceIsZero() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L, "number", 1L, 0, "employee-id", "close-employee-id"
                , ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "OPEN", BigDecimal.ZERO,BigDecimal.ZERO,Collections.emptyList(),0,0));
        assertEquals("Carton Sequence must be greater than 0", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLocationNotFound() {
        // Given
        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(recoveredPlasmaShipment.getLocationCode()).thenReturn("locationCode");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(recoveredPlasmaShipment));

        Mockito.when(cartonRepository.countByShipment(Mockito.anyLong())).thenReturn(Mono.just(0));


        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just( 123L));

        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.empty());

        // When/Then
        assertThrows(DomainNotFoundForKeyException.class,
            () -> Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), recoveredPlasmaShippingRepository, cartonRepository, locationRepository));
    }

    @Test
    void shouldThrowExceptionWhenRpsLocationCartonCodeKeyMissing() {
        // Given
        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(recoveredPlasmaShipment.getLocationCode()).thenReturn("locationCode");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(recoveredPlasmaShipment));

        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just( 123L));

        Location location = mock(Location.class);
        LocationProperty prefixProperty = mock(LocationProperty.class);
        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just( 123L));
        when(location.findProperty("RPS_CARTON_PARTNER_PREFIX")).thenReturn(Optional.of(prefixProperty));
        when(location.findProperty("RPS_LOCATION_CARTON_CODE")).thenReturn(Optional.empty());

        Mockito.when(cartonRepository.countByShipment(Mockito.anyLong())).thenReturn(Mono.just(0));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), recoveredPlasmaShippingRepository, cartonRepository, locationRepository));
        assertEquals("Location configuration is missing the setup for  RPS_LOCATION_CARTON_CODE property",
            exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonRepositoryIsNull() {
        // Given
        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(recoveredPlasmaShipment));

        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), recoveredPlasmaShippingRepository, null, locationRepository));
    }

    @Test
    void shouldThrowExceptionWhenRecoveredPlasmaShippingRepositoryIsNull() {
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), null, cartonRepository, locationRepository));
    }

    @Test
    void shouldThrowExceptionWhenRpsShipmentIsNotFound() {
        // Given
        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), recoveredPlasmaShippingRepository, cartonRepository, locationRepository));
        assertEquals("Shipment is required",
            exception.getMessage());
    }

    @Test
    void shouldIncrementCartonSequence() {
        // Given

        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(recoveredPlasmaShipment.getId()).thenReturn(1L);
        Mockito.when(recoveredPlasmaShipment.getLocationCode()).thenReturn("locationCode");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(recoveredPlasmaShipment));

        Mockito.when(cartonRepository.countByShipment(Mockito.anyLong())).thenReturn(Mono.just(10));


        Location location = mock(Location.class);

        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just( 123L));

        when(location.findProperty("RPS_CARTON_PARTNER_PREFIX")).thenReturn(Optional.of(new LocationProperty(1L,"RPS_CARTON_PARTNER_PREFIX","BPM")));
        when(location.findProperty("RPS_LOCATION_CARTON_CODE")).thenReturn(Optional.of(new LocationProperty(1L,"RPS_LOCATION_CARTON_CODE","MH1")));
        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just(1L));


        // When
        Carton carton = Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), recoveredPlasmaShippingRepository, cartonRepository, locationRepository);

        // Then
        assertNotNull(carton);

        assertEquals(11,carton.getCartonSequence());
        assertEquals("BPMMH11",carton.getCartonNumber());
        assertNull(carton.getId());
        assertEquals(1L,carton.getShipmentId());
        assertEquals("create-employee-id",carton.getCreateEmployeeId());
        assertEquals("OPEN", carton.getStatus());
        assertNull(carton.getCloseEmployeeId());
        assertNull(carton.getCloseDate());
        assertEquals(0,carton.getTotalProducts());
        assertEquals(BigDecimal.ZERO,carton.getTotalWeight());
        assertEquals(BigDecimal.ZERO,carton.getTotalVolume());

        verify(cartonRepository).getNextCartonId();
        verify(locationRepository).findOneByCode(anyString());
    }


    @Test
    void shouldNotCreateNewCartonWhenCartonPartnerPrefixIsNotConfigured() {
        // Given

        RecoveredPlasmaShipment recoveredPlasmaShipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(recoveredPlasmaShipment.getId()).thenReturn(1L);
        Mockito.when(recoveredPlasmaShipment.getLocationCode()).thenReturn("locationCode");

        Mockito.when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(recoveredPlasmaShipment));

        Mockito.when(cartonRepository.countByShipment(Mockito.anyLong())).thenReturn(Mono.just(0));


        Location location = mock(Location.class);

        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just( 123L));

        when(location.findProperty("RPS_CARTON_PARTNER_PREFIX")).thenReturn(Optional.empty());
        when(locationRepository.findOneByCode(anyString())).thenReturn(Mono.just(location));
        when(cartonRepository.getNextCartonId()).thenReturn(Mono.just(1L));

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.createNewCarton(new CreateCartonCommand(1L,"create-employee-id"), recoveredPlasmaShippingRepository, cartonRepository, locationRepository));
        assertEquals("Location configuration is missing the setup for  RPS_CARTON_PARTNER_PREFIX property",
            exception.getMessage());
    }

    @Test
    public void shouldReturnVerifiedProduct(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getStatus()).thenReturn("VERIFIED");
        Mockito.when(cartonItem.getProductCode()).thenReturn("CODE1");

        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem2.getStatus()).thenReturn("PACKED");


        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),0 ,0 );

        assertEquals(1,carton.getVerifiedProducts().size());
        assertEquals("CODE1",carton.getVerifiedProducts().getFirst().getProductCode());
        assertEquals("VERIFIED",carton.getVerifiedProducts().getFirst().getStatus());

    }

    @Test
    public void shouldAllowVerification(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),1 ,3 );

        assertTrue(carton.canVerify());
    }

    @Test
    public void shouldNotAllowVerification(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);

        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);


        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),5 ,10 );

        assertFalse(carton.canVerify());
    }

    @Test
    public void shouldNotAllowVerificationWhenStatusIsNotOpen(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);

        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);


        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"CLOSED", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),2 ,10 );

        assertFalse(carton.canVerify());
    }

    @Test
    public void shouldAllowClose(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getStatus()).thenReturn("VERIFIED");
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem2.getStatus()).thenReturn("VERIFIED");

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),2 ,2 );

        assertTrue(carton.canClose());
    }

    @Test
    public void shouldNotAllowCloseWhenAllProductsAreNotVerified(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getStatus()).thenReturn("VERIFIED");
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem2.getStatus()).thenReturn("PACKED");

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),2 ,2 );

        assertFalse(carton.canClose());
    }

    @Test
    public void shouldNotVerifyWhenMinimumProductsAreNotPacked(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),5 ,10 );


        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.verifyItem(Mockito.mock(VerifyItemCommand.class),Mockito.mock(InventoryService.class), Mockito.mock(CartonItemRepository.class)
                , Mockito.mock(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.mock(RecoveredPlasmaShippingRepository.class)));
        assertEquals("Carton cannot be verified",
            exception.getMessage());
    }

    @Test
    public void shouldNotVerifyWhenStatusIsNotOpen(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"CLOSED", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),2 ,10 );


        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.verifyItem(Mockito.mock(VerifyItemCommand.class),Mockito.mock(InventoryService.class), Mockito.mock(CartonItemRepository.class)
                , Mockito.mock(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.mock(RecoveredPlasmaShippingRepository.class)));
        assertEquals("Carton cannot be verified",
            exception.getMessage());
    }

    @Test
    public void shouldVerifyItem(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),2 ,10 );


       var cartonItem3 = Mockito.mock(CartonItem.class);

        try (MockedStatic<CartonItem> utilities = Mockito.mockStatic(CartonItem.class)) {
            utilities.when(() -> CartonItem.verifyCartonItem(Mockito.any(VerifyItemCommand.class),Mockito.any(Carton.class),Mockito.any(InventoryService.class), Mockito.any(CartonItemRepository.class)
                , Mockito.any(RecoveredPlasmaShippingRepository.class), Mockito.any(RecoveredPlasmaShipmentCriteriaRepository.class)))
                .thenReturn(cartonItem3);

            // When/Then
           var response = carton.verifyItem(Mockito.mock(VerifyItemCommand.class),Mockito.mock(InventoryService.class), Mockito.mock(CartonItemRepository.class), Mockito.mock(RecoveredPlasmaShipmentCriteriaRepository.class), Mockito.mock(RecoveredPlasmaShippingRepository.class));

            Assertions.assertNotNull(response);

        }
    }

    @Test
    public void shouldNotCloseWhenAllProductsAreNotVerified(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getStatus()).thenReturn("VERIFIED");
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem2.getStatus()).thenReturn("PACKED");

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),2 ,2 );

        var closeCartonCommand = Mockito.mock(CloseCartonCommand.class);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.close(closeCartonCommand));
        assertEquals("Carton cannot be closed",
            exception.getMessage());
    }

    @Test
    public void shouldNotCloseWhenStatusIsNotOpen(){

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"CLOSED", BigDecimal.ZERO,BigDecimal.ZERO, null,2 ,10 );

        var closeCartonCommand = Mockito.mock(CloseCartonCommand.class);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.close(closeCartonCommand));
        assertEquals("Carton cannot be closed",
            exception.getMessage());
    }


    @Test
    public void shouldCloseCarton(){

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getStatus()).thenReturn("VERIFIED");
        CartonItem cartonItem2 = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem2.getStatus()).thenReturn("VERIFIED");

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem2,cartonItem),2 ,2 );

        var closeCartonCommand = Mockito.mock(CloseCartonCommand.class);
        Mockito.when(closeCartonCommand.getEmployeeId()).thenReturn("close-employee-id");


        // When/Then
        var response = carton.close(closeCartonCommand);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("CLOSED", response.getStatus());
        Assertions.assertNotNull(response.getCloseDate());
        Assertions.assertEquals("close-employee-id", response.getCloseEmployeeId());

    }

    @Test
    public void shouldNotMarkAsReopenWenStatusNotPack(){

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        var repackCommand = Mockito.mock(RepackCartonCommand.class);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.markAsReopen(repackCommand));
        assertEquals("Carton cannot be repacked",
            exception.getMessage());

    }

    @Test
    public void shouldNotMarkAsReopenWenInvalidCommand(){

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"REPACK", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.markAsReopen(null));
        assertEquals("RepackCartonCommand is required",
            exception.getMessage());

    }

    @Test
    public void shouldMarkAsReopen(){

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"REPACK", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        var repackCommand = Mockito.mock(RepackCartonCommand.class);
        Mockito.when(repackCommand.getEmployeeId()).thenReturn("employee-id");
        Mockito.when(repackCommand.getReasonComments()).thenReturn("comments");

        var response = carton.markAsReopen(repackCommand);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("OPEN", response.getStatus());
        Assertions.assertNull(response.getCloseDate());
        Assertions.assertNull(response.getCloseEmployeeId());
        Assertions.assertNotNull(response.getRepackDate());
        Assertions.assertEquals("employee-id", response.getRepackEmployeeId());
        Assertions.assertEquals("comments", response.getRepackComments());
    }

    @Test
    public void shouldNotRemoveWhenStatusIsClosed(){

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"CLOSED", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        var removeCommand = Mockito.mock(RemoveCartonCommand.class);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.removeCarton(removeCommand,Mockito.mock(RecoveredPlasmaShippingRepository.class)));
        assertEquals("Carton is closed and cannot be removed",
            exception.getMessage());

    }

    @Test
    public void shouldNotRemoveWhenIsNotLastCarton(){

        var shipRepository = Mockito.mock(RecoveredPlasmaShippingRepository.class);

        var shipMock = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipMock.getTotalCartons()).thenReturn(2);
        Mockito.when(shipMock.canModify()).thenReturn(true);

        Mockito.when(shipRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipMock));


        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        var removeCommand = Mockito.mock(RemoveCartonCommand.class);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.removeCarton(removeCommand,shipRepository));
        assertEquals("Carton is not the last one and cannot be removed",
            exception.getMessage());

    }

    @Test
    public void shouldNotRemoveWhenShipmentIsClosed(){

        var shipRepository = Mockito.mock(RecoveredPlasmaShippingRepository.class);

        var shipMock = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipMock.canModify()).thenReturn(false);

        Mockito.when(shipRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipMock));


        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        var removeCommand = Mockito.mock(RemoveCartonCommand.class);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.removeCarton(removeCommand,shipRepository));
        assertEquals("Shipment is closed and cannot be modified",
            exception.getMessage());

    }

    @Test
    public void shouldNotRemoveWhenShipmentNotFound(){

        var shipRepository = Mockito.mock(RecoveredPlasmaShippingRepository.class);

        Mockito.when(shipRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.empty());

        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        var removeCommand = Mockito.mock(RemoveCartonCommand.class);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.removeCarton(removeCommand,shipRepository));
        assertEquals("Shipment is required",
            exception.getMessage());

    }


    @Test
    public void shouldNotRemoveLastCarton(){

        var shipRepository = Mockito.mock(RecoveredPlasmaShippingRepository.class);

        var shipMock = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipMock.getTotalCartons()).thenReturn(1);
        Mockito.when(shipMock.canModify()).thenReturn(true);

        Mockito.when(shipRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipMock));


        Carton carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        var removeCommand = Mockito.mock(RemoveCartonCommand.class);
        Mockito.when(removeCommand.getEmployeeId()).thenReturn("employee-id");

        // When/Then
        var response =  carton.removeCarton(removeCommand,shipRepository);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("REMOVED", response.getStatus());
        Assertions.assertNotNull(response.getDeleteDate());
        Assertions.assertEquals("employee-id", response.getDeleteEmployeeId());

    }


    @Test
    public void shouldGenerateCartonLabel(){

        var labelTemplateService = Mockito.mock(LabelTemplateService.class);
        Mockito.when(labelTemplateService.processTemplate(Mockito.anyString(), Mockito.anyMap())).thenReturn(Mono.just("LABEL"));

        var systemProcessPropertyRepository = Mockito.mock(SystemProcessPropertyRepository.class);
        var recoveredPlasmaShippingRepository = Mockito.mock(RecoveredPlasmaShippingRepository.class);

        var shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipment.getShipmentNumber()).thenReturn("NUMBER");
        Mockito.when(shipment.getLocationCode()).thenReturn("location-code");

        var shipmentCustomer = Mockito.mock(ShipmentCustomer.class);
        Mockito.when(shipment.getShipmentCustomer()).thenReturn(shipmentCustomer);

        when(recoveredPlasmaShippingRepository.findOneById(Mockito.anyLong())).thenReturn(Mono.just(shipment));


        var locationRepository = Mockito.mock(LocationRepository.class);
        var location = Mockito.mock(Location.class);
        when(locationRepository.findOneByCode(Mockito.anyString())).thenReturn(Mono.just(location));

        List<SystemProcessProperty> properties = new ArrayList<>();
        properties.add(new SystemProcessProperty(4L, "RPS_CARTON_LABEL", "BLOOD_CENTER_NAME", "Test Blood Center"));
        properties.add(new SystemProcessProperty(4L, "RPS_CARTON_LABEL", "USE_TRANSPORTATION_NUMBER", "Y"));
        properties.add(new SystemProcessProperty(4L, "RPS_CARTON_LABEL", "USE_TOTAL_CARTONS", "Y"));




        Mockito.when(systemProcessPropertyRepository.findAllByType(Mockito.anyString())).thenReturn(Flux.fromIterable(properties));

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getProductCode()).thenReturn("PRODUCT_CODE");

        var carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"CLOSED", BigDecimal.ZERO,BigDecimal.ZERO, List.of(cartonItem),2 ,2 );

        var response = carton.generateCartonLabel(labelTemplateService,locationRepository, recoveredPlasmaShippingRepository, systemProcessPropertyRepository);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("LABEL", response);

    }

    @Test
    public void shouldNotGenerateCartonLabelWhenStatusIsNotClosed(){

        var labelTemplateService = Mockito.mock(LabelTemplateService.class);
        var systemProcessPropertyRepository = Mockito.mock(SystemProcessPropertyRepository.class);
        var recoveredPlasmaShippingRepository = Mockito.mock(RecoveredPlasmaShippingRepository.class);
        var locationRepository = Mockito.mock(LocationRepository.class);

        var carton = Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO, Collections.emptyList(),2 ,2 );

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> carton.generateCartonLabel(labelTemplateService, locationRepository, recoveredPlasmaShippingRepository, systemProcessPropertyRepository));
        assertEquals("Carton is not closed",
            exception.getMessage());

    }


}
