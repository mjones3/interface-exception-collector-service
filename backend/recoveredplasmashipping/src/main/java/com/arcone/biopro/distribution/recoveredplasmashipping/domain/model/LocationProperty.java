package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class LocationProperty implements Validatable {

    private final Long locationId;
    private final String propertyKey;
    private final String propertyValue;

    public LocationProperty(Long locationId, String propertyKey, String propertyValue) {
        this.locationId = locationId;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;

        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.locationId == null) {
            throw new IllegalArgumentException("Location ID cannot be null");
        }

        if (this.propertyKey == null || this.propertyKey.isBlank()) {
            throw new IllegalArgumentException("Property Key cannot be null or empty");
        }

        if (this.propertyValue == null || this.propertyValue.isBlank()) {
            throw new IllegalArgumentException("Property Value cannot be null or empty");
        }
    }
}
