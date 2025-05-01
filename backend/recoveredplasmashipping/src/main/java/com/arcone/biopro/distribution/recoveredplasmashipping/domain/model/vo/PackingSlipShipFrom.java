package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Location;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.LocationProperty;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class PackingSlipShipFrom implements Validatable {

    private String bloodCenterName;
    private Location location;
    private String addressFormat;
    private String licenseNumber;
    @Getter(AccessLevel.NONE)
    private String locationAddressFormatted;

    public PackingSlipShipFrom(String bloodCenterName, Location location , String addressFormat) {
        this.bloodCenterName = bloodCenterName;
        this.location = location;
        this.addressFormat = addressFormat;
        checkValid();

        this.licenseNumber = location.findProperty("LICENSE_NUMBER").map(LocationProperty::getPropertyValue).orElse("");
    }

    @Override
    public void checkValid() {

        if(bloodCenterName == null || bloodCenterName.isBlank()){
            throw new IllegalArgumentException("Blood Center Name is required");
        }

        if(location == null ){
            throw new IllegalArgumentException("Location is required");
        }

        if(addressFormat == null || addressFormat.isBlank()){
            throw new IllegalArgumentException("Address Format is required");
        }
    }

    public String getLocationAddressFormatted(){
        return addressFormat
            .replace("{address}",location.getAddressLine1())
            .replace("{city}", location.getCity())
            .replace("{state}", location.getState())
            .replace("{zipcode}", location.getPostalCode())
            .replace("{country}","USA");
    }
}
