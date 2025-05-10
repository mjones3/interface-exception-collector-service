package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class Location implements Validatable {

    private final Long id;
    private final String name;
    private final String code;
    private final String externalId;
    private final String addressLine1;
    private final String addressLine2;
    private final String postalCode;
    private final String city;
    private final String state;
    private List<LocationProperty> locationProperties;

    public Location(Long id, String name, String code, String externalId, String addressLine1, String addressLine2, String postalCode, String city, String state) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.externalId = externalId;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.postalCode = postalCode;
        this.city = city;
        this.state = state;

        checkValid();
    }

    public void addProperty(String propertyKey, String propertyValue) {
        if (locationProperties == null) {
            locationProperties = new ArrayList<>();
        }

        this.locationProperties.add(new LocationProperty(this.id, propertyKey, propertyValue));

    }

    public Optional<LocationProperty> findProperty(String propertyKey) {
        return ofNullable(this.locationProperties)
            .filter(list -> !list.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(property -> property.getPropertyKey().equals(propertyKey))
            .findFirst();
    }

    @Override
    public void checkValid() {

        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or blank");
        }

        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }

        if (this.addressLine1 == null || this.addressLine1.isBlank()) {
            throw new IllegalArgumentException("Address Line1 cannot be null or blank");
        }

        if (this.postalCode == null || this.postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal Code cannot be null or blank");
        }

        if (this.city == null || this.city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }

        if (this.state == null || this.state.isBlank()) {
            throw new IllegalArgumentException("State cannot be null or blank");
        }
    }

    public String getTimeZone(){
        var timeZone = findProperty("TZ");
        if(timeZone.isEmpty()){
            log.error("Location Timezone is missing {}", this.name);
            throw new IllegalArgumentException("Timezone is required");
        }

        return timeZone.get().getPropertyValue();
    }
}
