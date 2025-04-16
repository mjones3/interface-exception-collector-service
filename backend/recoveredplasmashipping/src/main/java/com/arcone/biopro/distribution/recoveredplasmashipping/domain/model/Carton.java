package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;


import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Carton implements Validatable {

    private Long id;
    private String cartonNumber;
    private Long shipmentId;
    private Integer cartonSequence;
    private String createEmployeeId;
    private String closeEmployeeId;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;
    private ZonedDateTime closeDate;
    private String status;
    private int totalProducts;
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;

    private static final String STATUS_OPEN = "OPEN";
    private static final String SHIPMENT_PARTNER_PREFIX_KEY = "RPS_PARTNER_PREFIX";
    private static final String RPS_LOCATION_CARTON_CODE_KEY = "RPS_LOCATION_CARTON_CODE";


    public static Carton createNewCarton(CreateCartonCommand createCartonCommand , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository, CartonRepository cartonRepository, LocationRepository locationRepository) {

        var shipment = getShipment(createCartonCommand.getRecoveredPlasmaShipmentId() , recoveredPlasmaShippingRepository);

        var totalCartons = getTotalCartons(shipment.getId(),cartonRepository);

        var cartonId = getNextCartonId(cartonRepository);

        var carton = Carton.builder()
            .id(null)
            .cartonNumber(generateCartonNumber(cartonId,shipment.getLocationCode(),locationRepository))
            .shipmentId(shipment.getId())
            .cartonSequence( totalCartons + 1 )
            .createEmployeeId(createCartonCommand.getCreateEmployeeId())
            .closeEmployeeId(null)
            .closeDate(null)
            .createDate(ZonedDateTime.now())
            .modificationDate(null)
            .status(STATUS_OPEN)
            .totalProducts(0)
            .totalWeight(BigDecimal.ZERO)
            .totalVolume(BigDecimal.ZERO)
            .build();

        carton.checkValid();

        return carton;
    }

    private static RecoveredPlasmaShipment getShipment(Long shipmentId , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {
        if (recoveredPlasmaShippingRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShippingRepository is required");
        }

        return recoveredPlasmaShippingRepository.findOneById(shipmentId)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Carton generation error")))
            .block();
    }

    public static Carton fromRepository(Long id, String cartonNumber, Long shipmentId, Integer cartonSequence, String createEmployeeId, String closeEmployeeId
        , ZonedDateTime createDate, ZonedDateTime modificationDate, ZonedDateTime closeDate, String status , BigDecimal totalVolume , BigDecimal totalWeight) {
        var carton = Carton.builder()
            .id(id)
            .cartonNumber(cartonNumber)
            .shipmentId(shipmentId)
            .cartonSequence(cartonSequence)
            .createEmployeeId(createEmployeeId)
            .closeEmployeeId(closeEmployeeId)
            .createDate(createDate)
            .modificationDate(modificationDate)
            .closeDate(closeDate)
            .status(status)
            .totalWeight(totalWeight)
            .totalVolume(totalVolume)
            .build();

        carton.checkValid();

        return carton;

    }

    @Override
    public void checkValid() {

        if (this.shipmentId == null) {
            throw new IllegalArgumentException("Shipment Id is required");
        }

        if (this.createEmployeeId == null || this.createEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Create Employee Id is required");
        }

        if (this.status == null || this.status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        if(this.cartonNumber == null || this.cartonNumber.isBlank()){
            throw new IllegalArgumentException("Carton Number is required");
        }

        if(this.cartonSequence == null){
            throw new IllegalArgumentException("Carton Sequence is required");
        }

        if(this.cartonSequence <= 0){
            throw new IllegalArgumentException("Carton Sequence must be greater than 0");
        }

    }

    private static String generateCartonNumber(Long cartonId, String locationCode , LocationRepository locationRepository ) {

        var location = locationRepository.findOneByCode(locationCode)
            .switchIfEmpty(Mono.error( ()-> new DomainNotFoundForKeyException(String.format("%s", locationCode))))
            .block();

        var cartonNumber = "";
        var cartonPrefix = "";

        var prefix = location.findProperty(SHIPMENT_PARTNER_PREFIX_KEY);
        if (prefix.isPresent()) {
            log.debug("Location property {}", SHIPMENT_PARTNER_PREFIX_KEY);
            cartonPrefix = prefix.get().getPropertyValue();
        }

        var cartonLocationCode = location.findProperty(RPS_LOCATION_CARTON_CODE_KEY);
        if (cartonLocationCode.isEmpty()) {
            log.error("Location property is missed {}", RPS_LOCATION_CARTON_CODE_KEY);
            throw new IllegalArgumentException("Location configuration is missing the setup for  " + RPS_LOCATION_CARTON_CODE_KEY + " property");
        }
        cartonNumber = String.format("%s%s%s",cartonPrefix,cartonLocationCode.get().getPropertyValue(),cartonId);

        return cartonNumber;

    }

    private static Long getNextCartonId(CartonRepository cartonRepository) {

        if (cartonRepository == null) {
            throw new IllegalArgumentException("CartonRepository is required");
        }

        return cartonRepository.getNextCartonId()
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Carton ID is required")))
            .block();
    }

    private static Integer getTotalCartons(Long shipmentId,CartonRepository cartonRepository) {

        if (cartonRepository == null) {
            throw new IllegalArgumentException("CartonRepository is required");
        }

        return cartonRepository.countByShipment(shipmentId)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Total Cartons is required")))
            .block();
    }
}
