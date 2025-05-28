package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ShippingSummaryShipFrom implements Validatable {

    private final String bloodCenterName;
    private final String addressFormat;
    private final String locationAddress;
    private final String phoneNumber;


    public ShippingSummaryShipFrom(String bloodCenterName, Location location, String addressFormat) {
        this.bloodCenterName = bloodCenterName;
        this.addressFormat = addressFormat;
        this.locationAddress = getLocationAddressFormatted(location);
        this.phoneNumber = getLocationPhoneNumber(location);
        checkValid();
    }

    @Override
    public void checkValid() {

        if(bloodCenterName == null || bloodCenterName.isBlank()){
            throw new IllegalArgumentException("Blood Center Name is required");
        }

        if(locationAddress == null || locationAddress.isBlank()){
            throw new IllegalArgumentException("Location Address is required");
        }

    }

    private String getLocationAddressFormatted(Location location){
        if(location == null){
            throw new IllegalArgumentException("Location is required");
        }
        return addressFormat
            .replace("{address}",location.getAddressLine1())
            .replace("{city}", location.getCity())
            .replace("{state}", location.getState())
            .replace("{zipCode}", location.getPostalCode())
            .replace("{country}","USA");
    }

    private String getLocationPhoneNumber(Location location){
        if(location == null){
            throw new IllegalArgumentException("Location is required");
        }

        return location.findProperty("PHONE_NUMBER")
            .map(LocationProperty::getPropertyValue)
            .orElse(null);
    }
}
