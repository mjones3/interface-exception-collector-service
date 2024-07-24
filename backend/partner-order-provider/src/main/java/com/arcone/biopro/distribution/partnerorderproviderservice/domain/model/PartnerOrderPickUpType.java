package com.arcone.biopro.distribution.partnerorderproviderservice.domain.model;

import java.util.Objects;

public class PartnerOrderPickUpType {

    private boolean willCallPickUp;
    private String phoneNumber;

    public PartnerOrderPickUpType(boolean willCallPickUp, String phoneNumber) {
        this.willCallPickUp = willCallPickUp;
        this.phoneNumber = Objects.requireNonNull(phoneNumber,"Phone Number cannot be null");;
    }

    public boolean isWillCallPickUp() {
        return willCallPickUp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
