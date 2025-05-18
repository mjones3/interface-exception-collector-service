package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipTo;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryCartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipFrom;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipmentDetail;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ShippingSummaryReport implements Validatable {

    private static final String REPORT_TITLE = "Plasma Shipment Summary Report";
    private String reportTitle;
    private String employeeName;
    private String employeeId;
    private String shipDate;
    private String closeDate;
    private ShippingSummaryShipmentDetail shipmentDetail;
    private ShipTo shipTo;
    private ShippingSummaryShipFrom shipFrom;
    private String testingStatement;
    private List<ShippingSummaryCartonItem> cartonList;
    private boolean displayHeader;
    private String headerStatement;

    private static final String RPS_SYSTEM_PROCESS_TYPE = "RPS_SHIPPING_SUMMARY_REPORT";


    public static ShippingSummaryReport generateReport(RecoveredPlasmaShipment recoveredPlasmaShipment, CartonRepository cartonRepository
        , SystemProcessPropertyRepository systemProcessPropertyRepository, RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository, LocationRepository locationRepository) {

        var location = getLocation(recoveredPlasmaShipment.getLocationCode(), locationRepository);
        var cartonList = buildCartonSummaryList(recoveredPlasmaShipment, cartonRepository);
        var systemProperties = getSystemProperties(systemProcessPropertyRepository);

        var report = ShippingSummaryReport
            .builder()
            .reportTitle(REPORT_TITLE)
            .cartonList(cartonList)
            .employeeName(recoveredPlasmaShipment.getCloseEmployeeId())
            .employeeId(recoveredPlasmaShipment.getCloseEmployeeId())
            .testingStatement(buildTestingStatement(systemProperties))
            .closeDate(formatDateTime(recoveredPlasmaShipment.getCloseDate(), systemProperties, location))
            .shipDate(formatDate(recoveredPlasmaShipment.getShipmentDate(), systemProperties))
            .shipmentDetail(new ShippingSummaryShipmentDetail(recoveredPlasmaShipment, cartonList, getSystemPropertyByKey(systemProperties, "USE_TRANSPORTATION_NUMBER"), recoveredPlasmaShipmentCriteriaRepository))
            .shipTo(buildShipTo(recoveredPlasmaShipment, systemProperties))
            .shipFrom(buildShipFrom(systemProperties, location))
            .headerStatement(buildHeaderStatement(systemProperties))
            .displayHeader("Y".equals(getSystemPropertyByKey(systemProperties, "USE_HEADER_SECTION")) ? Boolean.TRUE : Boolean.FALSE)
            .build();

        report.checkValid();

        return report;
    }


    @Override
    public void checkValid() {
        if (reportTitle == null || reportTitle.isBlank()) {
            throw new IllegalArgumentException("Report Title is required");
        }

        if (employeeName == null || employeeName.isBlank()) {
            throw new IllegalArgumentException("Employee Name is required");
        }

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee Id is required");
        }

        if (shipDate == null || shipDate.isBlank()) {
            throw new IllegalArgumentException("Ship Date is required");
        }

        if (closeDate == null || closeDate.isBlank()) {
            throw new IllegalArgumentException("Close Date is required");
        }

        if (shipmentDetail == null) {
            throw new IllegalArgumentException("Shipment Detail is required");
        }

        if (shipTo == null) {
            throw new IllegalArgumentException("Ship To is required");
        }

        if (shipFrom == null) {
            throw new IllegalArgumentException("Ship From is required");
        }

        if (testingStatement == null || testingStatement.isBlank()) {
            throw new IllegalArgumentException("Testing Statement is required");
        }

        if (displayHeader && (headerStatement == null || headerStatement.isBlank())) {
            throw new IllegalArgumentException("Header Statement is required");
        }

    }

    private static List<ShippingSummaryCartonItem> buildCartonSummaryList(RecoveredPlasmaShipment recoveredPlasmaShipment, CartonRepository cartonRepository) {
        if (recoveredPlasmaShipment == null) {
            throw new IllegalArgumentException("Recovered Plasma Shipment is required");
        }

        if (cartonRepository == null) {
            throw new IllegalArgumentException("Carton Repository is required");
        }

        return cartonRepository.findAllByShipment(recoveredPlasmaShipment.getId())
            .map(ShippingSummaryCartonItem::new)
            .collectSortedList(Comparator.comparing(ShippingSummaryCartonItem::getCartonNumber
                , String.CASE_INSENSITIVE_ORDER))
            .block();
    }

    private static String getSystemPropertyByKey(List<SystemProcessProperty> systemProcessProperties, String key) {
        return systemProcessProperties.stream()
            .filter(systemProcessProperty -> key.equals(systemProcessProperty.getPropertyKey()))
            .map(SystemProcessProperty::getPropertyValue)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("System Property value is required for the Key : " + key));
    }

    private static List<SystemProcessProperty> getSystemProperties(SystemProcessPropertyRepository systemProcessPropertyRepository) {
        return systemProcessPropertyRepository.findAllByType(RPS_SYSTEM_PROCESS_TYPE)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("System Property is required: " + RPS_SYSTEM_PROCESS_TYPE)))
            .collectList()
            .block();
    }

    private static String formatDateTime(ZonedDateTime zonedDateTime, List<SystemProcessProperty> systemProcessProperties, Location location) {

        if (zonedDateTime == null) {
            throw new IllegalArgumentException("Date Time is required");
        }

        if (systemProcessProperties == null || systemProcessProperties.isEmpty()) {
            throw new IllegalArgumentException("System Property is required");
        }

        if (location == null) {
            throw new IllegalArgumentException("Location is required");
        }

        try {
            return DateTimeFormatter.ofPattern(getSystemPropertyByKey(systemProcessProperties, "DATE_TIME_FORMAT")).withZone(ZoneId.of(location.getTimeZone())).format(zonedDateTime);
        } catch (Exception e) {
            log.error("Not able to format date time {} {}", zonedDateTime, e.getMessage());
            throw new IllegalArgumentException("Date Time is required");
        }
    }

    private static String formatDate(LocalDate localDate, List<SystemProcessProperty> systemProcessProperties) {

        if (systemProcessProperties == null || systemProcessProperties.isEmpty()) {
            throw new IllegalArgumentException("System Property is required");
        }
        try {
            return DateTimeFormatter.ofPattern(getSystemPropertyByKey(systemProcessProperties, "DATE_FORMAT")).format(localDate);
        } catch (Exception e) {
            log.error("Not able to format date {} {}", localDate, e.getMessage());
            throw new IllegalArgumentException("Date is required");
        }
    }

    private static String buildTestingStatement(List<SystemProcessProperty> systemProcessProperties) {
        if (systemProcessProperties == null || systemProcessProperties.isEmpty()) {
            throw new IllegalArgumentException("System Property is required");
        }

        if ("Y".equals(getSystemPropertyByKey(systemProcessProperties, "USE_TESTING_STATEMENT"))) {
            return getSystemPropertyByKey(systemProcessProperties, "TESTING_STATEMENT_TXT");
        } else {
            return null;
        }
    }

    private static Location getLocation(String locationCode, LocationRepository locationRepository) {
        if (locationRepository == null) {
            throw new IllegalArgumentException("LocationRepository is required");
        }

        return locationRepository.findOneByCode(locationCode)
            .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Location is required")))
            .block();
    }

    private static ShipTo buildShipTo(RecoveredPlasmaShipment shipment, List<SystemProcessProperty> systemProcessProperties) {
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment is required");
        }

        return new ShipTo(shipment.getShipmentCustomer(), getSystemPropertyByKey(systemProcessProperties, "ADDRESS_FORMAT"));
    }

    private static ShippingSummaryShipFrom buildShipFrom(List<SystemProcessProperty> systemProcessProperties, Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location is required");
        }

        if (systemProcessProperties == null || systemProcessProperties.isEmpty()) {
            throw new IllegalArgumentException("System Property is required");
        }

        return new ShippingSummaryShipFrom(getSystemPropertyByKey(systemProcessProperties, "BLOOD_CENTER_NAME"), location, getSystemPropertyByKey(systemProcessProperties, "ADDRESS_FORMAT"));
    }

    private static String buildHeaderStatement(List<SystemProcessProperty> systemProcessProperties) {
        if (systemProcessProperties == null || systemProcessProperties.isEmpty()) {
            throw new IllegalArgumentException("System Property is required");
        }

        if ("Y".equals(getSystemPropertyByKey(systemProcessProperties, "USE_HEADER_SECTION"))) {
            return getSystemPropertyByKey(systemProcessProperties, "HEADER_SECTION_TXT");
        } else {
            return null;
        }
    }
}
