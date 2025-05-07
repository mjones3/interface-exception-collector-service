package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class PackingSlipProduct implements Validatable {

    private final String unitNumber;
    @Getter(AccessLevel.NONE)
    private final ZonedDateTime collectionDate;
    private final Integer volume;
    private final String dateFormat;
    private final String timeZone;
    @Getter(AccessLevel.NONE)
    private String collectionDateFormatted;

    public PackingSlipProduct(String unitNumber, ZonedDateTime collectionDate, Integer volume , String dateFormat , String timeZone) {
        this.unitNumber = unitNumber;
        this.collectionDate = collectionDate;
        this.volume = volume;
        this.dateFormat = dateFormat;
        this.timeZone = timeZone;

        checkValid();
    }


    @Override
    public void checkValid() {

        if(unitNumber == null || unitNumber.isBlank()){
            throw new IllegalArgumentException("Unit Number is required");
        }

        if(collectionDate == null){
            throw new IllegalArgumentException("Collection Date is required");
        }

        if(dateFormat == null || dateFormat.isBlank()){
            throw new IllegalArgumentException("Date Format is required");
        }

        if(timeZone == null || timeZone.isBlank()){
            throw new IllegalArgumentException("Timezone is required");
        }

        if(volume == null || volume <= 0){
            throw new IllegalArgumentException("Volume is required");
        }
    }

    public String getCollectionDateFormatted() {
        try{
            return DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of(this.timeZone)).format(this.collectionDate);
        }catch (Exception e){
            log.error("Not able to format collection date {} {}", this.collectionDate , e.getMessage());
            throw  new IllegalArgumentException("Collection date is required");
        }
    }

}
