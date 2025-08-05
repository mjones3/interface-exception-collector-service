package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RecoveredPlasmaShipmentClosedCartonItemOutbound implements Validatable {

    private static final String TIME_FORMAT = "HH:mm";
    private static final String DATE_TIME_FORMAT = "yyyy-mm-dd HH:mm";

    private String unitNumber;
    private String productCode;
    private String collectionFacility;
    private ZonedDateTime collectionDate;
    private BigDecimal productVolume;
    private String bloodType;
    private String drawBeginTime;
    private String collectionTimeZone;
    private String collectionDateFormatted;

    public RecoveredPlasmaShipmentClosedCartonItemOutbound(String unitNumber, String productCode, String collectionFacility
        , ZonedDateTime collectionDate, Integer productVolume, String bloodType , String collectionTimeZone) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.collectionFacility = collectionFacility;
        this.collectionDate = collectionDate;
        this.productVolume = getVolume(productVolume);
        this.bloodType = bloodType;
        this.collectionTimeZone = collectionTimeZone;
        this.drawBeginTime = getDateTimeFormat(TIME_FORMAT);
        this.collectionDateFormatted = getDateTimeFormat(DATE_TIME_FORMAT);

        checkValid();

    }

    @Override
    public void checkValid() {

        if (unitNumber == null || unitNumber.isBlank()) {
            throw new IllegalStateException("Unit number is null");
        }

        if (productCode == null || productCode.isBlank()) {
            throw new IllegalStateException("Product code is null");
        }

        if (collectionFacility == null || collectionFacility.isBlank()) {
            throw new IllegalStateException("Collection Facility is null");
        }

        if (collectionDate == null) {
            throw new IllegalStateException("Collection Date is null");
        }

        if(drawBeginTime == null || drawBeginTime.isBlank()){
            throw new IllegalStateException("Draw Begin Time is null");
        }

        if(collectionDateFormatted == null || collectionDateFormatted.isBlank()){
            throw new IllegalStateException("Collection Date Formatted is null");
        }
    }

    private String getDateTimeFormat(String format){
        if(this.collectionTimeZone == null || this.collectionTimeZone.isBlank()){
            throw new IllegalStateException("Collection Timezone is null");
        }

        if(this.collectionDate == null){
            throw new IllegalStateException("Collection Date is null");
        }

        var collectionDateTimeZone = this.collectionDate.withZoneSameInstant(java.time.ZoneId.of(this.collectionTimeZone));

        return collectionDateTimeZone.format(java.time.format.DateTimeFormatter.ofPattern(format));

    }

    private BigDecimal getVolume(Integer volume){
        if(volume == null || volume == 0){
            return BigDecimal.ZERO;
        }
       return new BigDecimal(volume).divide(new BigDecimal(1000)).setScale(3,RoundingMode.HALF_UP);
    }
}
