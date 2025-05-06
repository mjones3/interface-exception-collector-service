package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class SystemProcessProperty implements Validatable {

    private final Long Id;
    private final String propertyType;
    private final String propertyKey;
    private final String propertyValue;

    public SystemProcessProperty(Long id, String propertyType, String propertyKey, String propertyValue) {
        Id = id;
        this.propertyType = propertyType;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;

        checkValid();
    }

    @Override
    public void checkValid() {
        if (this.propertyType == null || this.propertyType.isBlank()) {
            throw new IllegalArgumentException("Property Type cannot be null or empty");
        }

        if (this.propertyKey == null || this.propertyKey.isBlank()) {
            throw new IllegalArgumentException("Property Key cannot be null or empty");
        }

        if (this.propertyValue == null || this.propertyValue.isBlank()) {
            throw new IllegalArgumentException("Property Value cannot be null or empty");
        }
    }
}
