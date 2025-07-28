package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ValidateTransitTimeCommand implements Validatable {
    private final String temperatureCategory;
    private final LocalDateTime startDateTime;
    private final String startTimeZone;
    private final LocalDateTime endDateTime;
    private final String endTimeZone;
    private ZoneId startZoneId;
    private ZoneId endZoneId;

    public ValidateTransitTimeCommand(String temperatureCategory,LocalDateTime startDateTime, String startTimeZone, LocalDateTime endDateTime, String endTimeZone) {
        this.temperatureCategory = temperatureCategory;
        this.startDateTime = startDateTime;
        this.startTimeZone = startTimeZone;
        this.endDateTime = endDateTime;
        this.endTimeZone = endTimeZone;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (temperatureCategory == null || temperatureCategory.isBlank()) {
            throw new IllegalArgumentException("Temperature category cannot be null or blank");
        }

        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date time cannot be null");
        }
        if (startTimeZone == null || startTimeZone.isBlank()) {
            throw new IllegalArgumentException("Start time zone cannot be null or blank");
        }
        try{
            startZoneId = ZoneId.of(startTimeZone);
        }catch (Exception e){
            throw new IllegalArgumentException("Invalid start time zone");
        }

        if (endDateTime == null) {
            throw new IllegalArgumentException("End date time cannot be null");
        }
        if (endTimeZone == null || endTimeZone.isBlank()) {
            throw new IllegalArgumentException("End time zone cannot be null or blank");
        }
        try{
            endZoneId = ZoneId.of(endTimeZone);
        } catch (Exception e){
            throw new IllegalArgumentException("Invalid end time zone");
        }

        if(startDateTime.isAfter(endDateTime)){
            throw new IllegalArgumentException("Start date date cannot be after end date date");
        }

        if(endDateTime.isBefore(startDateTime)){
            throw new IllegalArgumentException("End date date cannot be before start date");
        }

        if(endDateTime.isAfter(LocalDateTime.now())){
            throw new IllegalArgumentException("End date date cannot be in the future");
        }
    }
}
