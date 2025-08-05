package com.arcone.biopro.distribution.partnerorderprovider.domain.model;

public class PartnerOrderPickUpType {

    private boolean willCallPickUp;
    private String phoneNumber;

    public PartnerOrderPickUpType(boolean willCallPickUp, String phoneNumber) {
        this.willCallPickUp = willCallPickUp;
        this.phoneNumber = phoneNumber;

        if(willCallPickUp && (phoneNumber == null || phoneNumber.isBlank())){
            throw new IllegalArgumentException("Phone Number cannot be null or empty");

        }
    }

    public boolean isWillCallPickUp() {
        return willCallPickUp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
