package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.SystemProcessPropertyRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UnacceptableUnitReport implements Validatable {

    private String shipmentNumber;
    private static final String REPORT_TITLE = "Unacceptable Products Report";
    private String dateTimeExported;
    private List<UnacceptableUnitReportItem> failedProducts;
    private static final String NO_PRODUCTS_FLAGGED_MESSAGE = "The shipment contains no unacceptable products";
    private String noProductsFlaggedMessage;
    private String reportTitle;
    private static final String UNACCEPTABLE_UNIT_REPORT_PROCESS_ID = "RPS_UNACCEPTABLE_UNITS_REPORT";


    public static UnacceptableUnitReport createReport(final RecoveredPlasmaShipment recoveredPlasmaShipment
        , UnacceptableUnitReportRepository unacceptableUnitReportRepository , LocationRepository locationRepository
        , SystemProcessPropertyRepository systemProcessPropertyRepository , String locationCode){

        var location = getLocation(locationCode, locationRepository);

        var systemProcessProperties = getSystemProperties(systemProcessPropertyRepository);

        var report = UnacceptableUnitReport
            .builder()
            .shipmentNumber(recoveredPlasmaShipment.getShipmentNumber())
            .dateTimeExported(getDateTimeExported(systemProcessProperties,location))
            .failedProducts(buildReportItems(recoveredPlasmaShipment.getId(),unacceptableUnitReportRepository))
            .reportTitle(REPORT_TITLE)
            .noProductsFlaggedMessage(NO_PRODUCTS_FLAGGED_MESSAGE)
            .build();

        report.checkValid();

        return report;
    }

    @Override
    public void checkValid() {
        if(shipmentNumber == null || shipmentNumber.isBlank()){
            throw new IllegalArgumentException("Shipment Number is required");
        }

        if(dateTimeExported == null || dateTimeExported.isBlank()){
            throw new IllegalArgumentException("Date Time Exported is required");
        }
    }


    private static String getDateTimeExported(List<SystemProcessProperty> systemProcessProperties, Location location) {
        try{
            return DateTimeFormatter.ofPattern(getSystemPropertyByKey(systemProcessProperties,"DATE_TIME_FORMAT")).withZone(ZoneId.of(location.getTimeZone())).format(ZonedDateTime.now());
        }catch (Exception e){
            log.error("Not able to format collection date {} {}", ZonedDateTime.now() , e.getMessage());
            throw  new IllegalArgumentException("Date Time Exported is required");
        }
    }

    private static List<SystemProcessProperty> getSystemProperties(SystemProcessPropertyRepository systemProcessPropertyRepository) {
        return systemProcessPropertyRepository.findAllByType(UNACCEPTABLE_UNIT_REPORT_PROCESS_ID)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("System Property is required: "+UNACCEPTABLE_UNIT_REPORT_PROCESS_ID)))
            .collectList()
            .block();
    }

    private static String getSystemPropertyByKey(List<SystemProcessProperty> systemProcessProperties , String key) {
        return systemProcessProperties.stream()
            .filter(systemProcessProperty -> key.equals(systemProcessProperty.getPropertyKey()))
            .map(SystemProcessProperty::getPropertyValue)
            .findFirst()
            .orElseThrow(()-> new IllegalArgumentException("System Property value is required for the Key : "+key));
    }

    private static Location getLocation(String locationCode, LocationRepository locationRepository) {
        if (locationRepository == null) {
            throw new IllegalArgumentException("LocationRepository is required");
        }

        return locationRepository.findOneByCode(locationCode)
            .switchIfEmpty(Mono.error( ()-> new IllegalArgumentException("Location is required")))
            .block();
    }

    private static List<UnacceptableUnitReportItem> buildReportItems(final Long shipmentId, UnacceptableUnitReportRepository unacceptableUnitReportRepository){

        if (unacceptableUnitReportRepository == null) {
            throw new IllegalArgumentException("UnacceptableUnitReportRepository is required");
        }

        return unacceptableUnitReportRepository.findAllByShipmentId(shipmentId)
            .collectList()
            .block();

    }

}
