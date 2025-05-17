package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
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
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RecoveredPlasmaShipment implements Validatable {

    public static final int SHIPMENT_DATE_RANGE_YEARS_LIMIT = 2;

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
    @Getter(AccessLevel.NONE)
    private int totalCartons;
    @Getter(AccessLevel.NONE)
    private int totalProducts;
    @Setter(AccessLevel.PRIVATE)
    private boolean canAddCartons;
    private List<Carton> cartonList;
    private ZonedDateTime lastUnsuitableReportRunDate;

    private static final String SHIPMENT_LOCATION_CODE_KEY = "RPS_LOCATION_SHIPMENT_CODE";
    private static final String SHIPMENT_USE_PREFIX_KEY = "RPS_USE_PARTNER_PREFIX";
    private static final String SHIPMENT_PARTNER_PREFIX_KEY = "RPS_PARTNER_PREFIX";
    private static final String YES = "Y";
    private static final String OPEN_STATUS = "OPEN";
    private static final String IN_PROGRESS_STATUS = "IN_PROGRESS";
    private static final String CLOSED_STATUS = "CLOSED";
    private static final String PROCESSING_STATUS = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_COMPLETED_FAILED = "COMPLETED_FAILED";
    private static final String ERROR_PROCESSING = "ERROR_PROCESSING";


    public static RecoveredPlasmaShipment createNewShipment(CreateShipmentCommand command, CustomerService customerService
        , RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository
        , LocationRepository locationRepository, RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {

        var location = getLocation(command.getLocationCode(), locationRepository);

        var customer = ShipmentCustomer.fromCustomerCode(command.getCustomerCode(), customerService);

        var productCriteria = getProductTypeCriteria(command.getCustomerCode(), command.getProductType(), recoveredPlasmaShipmentCriteriaRepository);

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

    public static RecoveredPlasmaShipment fromFindByCommand(FindShipmentCommand findShipmentCommand, RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {
        return recoveredPlasmaShippingRepository.findOneById(findShipmentCommand.getShipmentId())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", findShipmentCommand.getShipmentId()))))
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
                                                         ZonedDateTime modificationDate, ZonedDateTime lastUnsuitableReportRunDate, List<Carton> cartonList) {
        var shipment = RecoveredPlasmaShipment
            .builder()
            .id(id)
            .shipmentCustomer(ShipmentCustomer.fromShipmentDetails(customerCode, customerName, customerState, customerPostalCode, customerCountry, customerCountryCode
                , customerCity, customerDistrict, customerAddressLine1, customerAddressLine2, customerAddressContactName, customerAddressPhoneNumber, customerAddressDepartmentName))
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
            .cartonList(cartonList)
            .lastUnsuitableReportRunDate(lastUnsuitableReportRunDate)
            .build();

        shipment.checkValid();

        return shipment;
    }

    private static Long getNextShipmentId(RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository) {

        if (recoveredPlasmaShippingRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShippingRepository is required");
        }

        return recoveredPlasmaShippingRepository.getNextShipmentId()
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Shipment Number is required")))
            .block();
    }

    private static Location getLocation(String locationCode, LocationRepository locationRepository) {
        return locationRepository.findOneByCode(locationCode)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Location is required")))
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

        shipmentNumber = String.format("%s%s%s", shipmentNumber, shipmentLocationCode.get().getPropertyValue(), shipmentId);

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

        if (this.status == null || this.status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        if (this.cartonTareWeight == null) {
            throw new IllegalArgumentException("Carton tare weight is required");
        }

        if (this.unsuitableUnitReportDocumentStatus != null && lastUnsuitableReportRunDate == null) {
            throw new IllegalArgumentException("Last run date is required");
        }
    }


    private static RecoveredPlasmaShipmentCriteria getProductTypeCriteria(String customerCode, String productType, RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository) {
        if (recoveredPlasmaShipmentCriteriaRepository == null) {
            throw new IllegalArgumentException("RecoveredPlasmaShipmentCriteriaRepository is required");
        }

        return recoveredPlasmaShipmentCriteriaRepository.findProductCriteriaByCustomerCode(productType, customerCode)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Product type is required")))
            .block();
    }


    public int getTotalCartons() {
        if (this.cartonList == null) {
            return 0;
        }

        return this.cartonList.size();
    }

    public int getTotalProducts() {
        if (this.cartonList == null) {
            return 0;
        }

        return this.cartonList.stream()
            .reduce(0, (partialTotalProducts, carton) -> partialTotalProducts + carton.getTotalProducts(), Integer::sum);
    }

    public RecoveredPlasmaShipment markAsInProgress() {
        if (CLOSED_STATUS.equals(this.status)) {
            throw new IllegalArgumentException("Shipment is closed and cannot be reopen");
        }

        if (this.status.equals(OPEN_STATUS)) {
            this.status = "IN_PROGRESS";
        }

        return this;
    }

    public RecoveredPlasmaShipment markAsProcessing(final CloseShipmentCommand closeShipmentCommand) {

        if (!canClose()) {
            throw new IllegalArgumentException("Shipment cannot be closed");
        }

        this.status = PROCESSING_STATUS;
        this.shipmentDate = closeShipmentCommand.getShipDate();
        this.closeEmployeeId = closeShipmentCommand.getEmployeeId();
        this.unsuitableUnitReportDocumentStatus = null;
        this.lastUnsuitableReportRunDate = null;

        return this;
    }

    public boolean canClose() {
        if (this.cartonList == null || this.cartonList.isEmpty()) {
            return false;
        }
        var allCartonsAreClosed = this.cartonList.stream().allMatch(carton -> CLOSED_STATUS.equals(carton.getStatus()));

        return !PROCESSING_STATUS.equals(this.status) && !CLOSED_STATUS.equals(this.status) && allCartonsAreClosed;
    }

    public boolean canModify() {
        return !PROCESSING_STATUS.equals(this.status) && !CLOSED_STATUS.equals(this.status);
    }

    public UnacceptableUnitReport printUnacceptableUnitReport(final PrintUnacceptableUnitReportCommand printUnacceptableUnitReportCommand, UnacceptableUnitReportRepository unacceptableUnitReportRepository
        , LocationRepository locationRepository, SystemProcessPropertyRepository systemProcessPropertyRepository) {

        if (this.unsuitableUnitReportDocumentStatus == null || this.unsuitableUnitReportDocumentStatus.isBlank()) {
            throw new IllegalArgumentException("Unacceptable units report not available");
        }

        if (PROCESSING_STATUS.equals(this.status)) {
            throw new IllegalArgumentException("Unacceptable units report still running");
        }
        return UnacceptableUnitReport.createReport(this, unacceptableUnitReportRepository, locationRepository, systemProcessPropertyRepository, printUnacceptableUnitReportCommand.getLocationCode());
    }

    public RecoveredPlasmaShipment completeProcessing(final List<UnacceptableUnitReportItem> unacceptableUnitReportItemList) {
        if (unacceptableUnitReportItemList == null || unacceptableUnitReportItemList.isEmpty()) {
            this.status = CLOSED_STATUS;
            this.closeDate = ZonedDateTime.now();
            this.unsuitableUnitReportDocumentStatus = STATUS_COMPLETED;
        } else {
            this.status = IN_PROGRESS_STATUS;
            this.closeDate = null;
            this.closeEmployeeId = null;
            this.unsuitableUnitReportDocumentStatus = STATUS_COMPLETED_FAILED;
        }
        this.lastUnsuitableReportRunDate = ZonedDateTime.now();

        return this;
    }

    public RecoveredPlasmaShipment markAsProcessingError() {
        this.status = IN_PROGRESS_STATUS;
        this.closeDate = null;
        this.closeEmployeeId = null;
        this.unsuitableUnitReportDocumentStatus = ERROR_PROCESSING;
        this.lastUnsuitableReportRunDate = ZonedDateTime.now();
        return this;
    }

    public ShippingSummaryReport printShippingSummaryReport(final PrintShippingSummaryCommand command ,  final CartonRepository cartonRepository ,  final SystemProcessPropertyRepository systemProcessPropertyRepository, final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository
        ,final LocationRepository locationRepository) {

        if (!CLOSED_STATUS.equals(this.status)) {
            throw new IllegalArgumentException("Shipment is not closed and cannot be printed");
        }

        return ShippingSummaryReport.generateReport(this, cartonRepository,systemProcessPropertyRepository,recoveredPlasmaShipmentCriteriaRepository,locationRepository);
    }
}
