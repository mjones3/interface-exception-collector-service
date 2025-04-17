package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CreateCartonCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO));
    }

    @Test
    void shouldThrowExceptionWhenShipmentIdIsNull() {

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L,"number",null,1,"employee-id","close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO));
        assertEquals("Shipment Id is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateEmployeeIdIsNull() {

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L,"number",1L,1,null,"close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO));
        assertEquals("Create Employee Id is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonNumberIsNull() {

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () ->  Carton.fromRepository(1L,null,1L,1,"employee-id","close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO));
        assertEquals("Carton Number is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonSequenceIsNull() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L,"number",1L,null,"employee-id","close-employee-id"
                , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"OPEN", BigDecimal.ZERO,BigDecimal.ZERO));
        assertEquals("Carton Sequence is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonSequenceIsZero() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Carton.fromRepository(1L, "number", 1L, 0, "employee-id", "close-employee-id"
                , ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), "OPEN", BigDecimal.ZERO,BigDecimal.ZERO));
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
        assertEquals("Carton generation error",
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
}
