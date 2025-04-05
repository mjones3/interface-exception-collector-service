package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RecoveredPlasmaShipment implements Validatable {

    private Long id;
    private ShipmentCustomer shipmentCustomer;
    private String locationCode;
    private String productType;
    private String shipmentNumber;
    private String status;
    private String createEmployeeId;
    private String closeEmployeeId;
    private ZonedDateTime closeDate;
    private String transportationReferenceNumber;
    private LocalDate shipmentDate;
    private BigDecimal cartonTareWeight;
    private String unsuitableUnitReportDocumentStatus;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;
    private int totalCartons;
    private int totalProducts;
    @Setter(AccessLevel.PRIVATE)
    private boolean canAddCartons;

    private static final String SHIPMENT_LOCATION_CODE_KEY = "RPS_LOCATION_SHIPMENT_CODE";
    private static final String SHIPMENT_USE_PREFIX_KEY = "RPS_USE_PARTNER_PREFIX";
    private static final String SHIPMENT_PARTNER_PREFIX_KEY = "RPS_PARTNER_PREFIX";
    private static final String YES = "Y";
    private static final String OPEN_STATUS = "OPEN";

    public static RecoveredPlasmaShipment createNewShipment(CreateShipmentCommand command, CustomerService customerService
        , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository
        , LocationRepository locationRepository , RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {

        var location = getLocation(command.getLocationCode(), locationRepository);

        var customer = ShipmentCustomer.fromCustomerCode(command.getCustomerCode(), customerService);

        var productCriteria = getProductTypeCriteria(command.getCustomerCode(), command.getProductType(),recoveredPlasmaShipmentCriteriaRepository );

        var shipmentId = getNextShipmentId(recoveredPlasmaShippingRepository);

        var shipment = RecoveredPlasmaShipment
            .builder()
            .id(null)
            .shipmentNumber(generateShipmentNumber(shipmentId, location))
            .shipmentCustomer(customer)
            .productType(productCriteria.getProductType())
            .status(OPEN_STATUS)
            .locationCode(location.getCode())
            .createDate(ZonedDateTime.now())
            .shipmentDate(command.getShipmentDate())
            .cartonTareWeight(command.getCartonTareWeight())
            .createEmployeeId(command.getCreateEmployeeId())
            .transportationReferenceNumber(command.getTransportationReferenceNumber())
            .build();

        shipment.checkValid();

        return shipment;

    }

    public static RecoveredPlasmaShipment fromFindByCommand(FindShipmentCommand findShipmentCommand , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository){
        return recoveredPlasmaShippingRepository.findOneById(findShipmentCommand.getShipmentId())
            .switchIfEmpty(Mono.error( ()-> new DomainNotFoundForKeyException(String.format("%s", findShipmentCommand.getShipmentId()))))
            .flatMap(recoveredPlasmaShipment -> {
                recoveredPlasmaShipment.setCanAddCartons(recoveredPlasmaShipment.getLocationCode().equals(findShipmentCommand.getLocationCode()));
                return Mono.just(recoveredPlasmaShipment);
            })
            .block();
    }

    public static RecoveredPlasmaShipment fromRepository(Long id, String locationCode,
                                                         String productType,
                                                         String shipmentNumber,
                                                         String status,
                                                         String createEmployeeId,
                                                         String closeEmployeeId,
                                                         ZonedDateTime closeDate,
                                                         String transportationReferenceNumber,
                                                         LocalDate shipmentDate,
                                                         BigDecimal cartonTareWeight,
                                                         String unsuitableUnitReportDocumentStatus,
                                                         String customerCode, String customerName, String customerState, String customerPostalCode, String customerCountry
        , String customerCountryCode, String customerCity, String customerDistrict, String customerAddressLine1, String customerAddressLine2, String customerAddressContactName
        , String customerAddressPhoneNumber, String customerAddressDepartmentName,

                                                         ZonedDateTime createDate,
                                                         ZonedDateTime modificationDate) {
        var shipment = RecoveredPlasmaShipment
            .builder()
            .id(id)
            .shipmentCustomer(ShipmentCustomer.fromShipmentDetails(customerCode,customerName,customerState,customerPostalCode,customerCountry,customerCountryCode
                ,customerCity,customerDistrict,customerAddressLine1,customerAddressLine2,customerAddressContactName,customerAddressPhoneNumber,customerAddressDepartmentName))
            .locationCode(locationCode)
            .productType(productType)
            .shipmentNumber(shipmentNumber)
            .status(status)
            .createEmployeeId(createEmployeeId)
            .closeEmployeeId(closeEmployeeId)
            .closeDate(closeDate)
            .transportationReferenceNumber(transportationReferenceNumber)
            .shipmentDate(shipmentDate)
            .cartonTareWeight(cartonTareWeight)
            .unsuitableUnitReportDocumentStatus(unsuitableUnitReportDocumentStatus)
            .createDate(createDate)
            .modificationDate(modificationDate)
            .build();

        shipment.checkValid();

        return shipment;
    }

    private static Long getNextShipmentId(RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {

        if (recoveredPlasmaShippingRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShippingRepository is required");
        }

        return recoveredPlasmaShippingRepository.getNextShipmentId()
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Shipment Number is required")))
            .block();
    }

    private static Location getLocation(String locationCode, LocationRepository locationRepository) {
            return locationRepository.findOneByCode(locationCode)
                .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Location is required")))
                .block();
    }

    private static String generateShipmentNumber(Long shipmentId, Location location) {
        var shipmentNumber = "";
        var useShipmentPrefix = location.findProperty(SHIPMENT_USE_PREFIX_KEY);
        if (useShipmentPrefix.isEmpty()) {
            log.error("Location property is missed {}", SHIPMENT_USE_PREFIX_KEY);
            throw new IllegalArgumentException("Location configuration is missing the setup for  " + SHIPMENT_USE_PREFIX_KEY + " property");
        }
        if (useShipmentPrefix.get().getPropertyValue().equals(YES)) {
            var prefix = location.findProperty(SHIPMENT_PARTNER_PREFIX_KEY);
            if (prefix.isEmpty()) {
                log.error("Location property is missed {}", SHIPMENT_PARTNER_PREFIX_KEY);
                throw new IllegalArgumentException("Location configuration is missing the setup for  " + SHIPMENT_PARTNER_PREFIX_KEY + " property");
            }
            shipmentNumber = prefix.get().getPropertyValue();

        }

        var shipmentLocationCode = location.findProperty(SHIPMENT_LOCATION_CODE_KEY);
        if (shipmentLocationCode.isEmpty()) {
            log.error("Location property is missed {}", SHIPMENT_LOCATION_CODE_KEY);
            throw new IllegalArgumentException("Location configuration is missing the setup for  " + SHIPMENT_LOCATION_CODE_KEY + " property");
        }

        shipmentNumber = String.format("%s%s%s",shipmentNumber,shipmentLocationCode.get().getPropertyValue(),shipmentId);

        return shipmentNumber;

    }

    @Override
    public void checkValid() {

        if (this.shipmentNumber == null || this.shipmentNumber.isBlank()) {
            throw new IllegalArgumentException("Shipment Number is required");
        }
        if (this.shipmentCustomer == null) {
            throw new IllegalArgumentException("Shipment Customer is required");
        }

        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("Location code is required");
        }

        if (this.productType == null || this.productType.isBlank()) {
            throw new IllegalArgumentException("Product type is required");
        }

        if (this.createEmployeeId == null || this.createEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Create employee ID is required");
        }

        if (this.shipmentDate == null) {
            throw new IllegalArgumentException("Shipment date is required");
        }

        if (this.status == null || this.status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        if (this.cartonTareWeight == null) {
            throw new IllegalArgumentException("Carton tare weight is required");
        }
    }


    private static RecoveredPlasmaShipmentCriteria getProductTypeCriteria(String customerCode , String productType ,RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {
        if (recoveredPlasmaShipmentCriteriaRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShipmentCriteriaRepository is required");
        }

        return recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(productType, customerCode)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Product type is required")))
            .block();
    }
}
