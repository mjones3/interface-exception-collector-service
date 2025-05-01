package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
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
public class PackingSlipShipTo implements Validatable {

    private ShipmentCustomer shipmentCustomer;
    private String addressFormat;
    @Getter(AccessLevel.NONE)
    private String formattedAddress;
    @Getter(AccessLevel.NONE)
    private String customerName;

    public PackingSlipShipTo(ShipmentCustomer shipmentCustomer, String addressFormat) {
        this.shipmentCustomer = shipmentCustomer;
        this.addressFormat = addressFormat;
        checkValid();
    }

    @Override
    public void checkValid() {
        if(shipmentCustomer == null ){
            throw new IllegalArgumentException("Ship Customer is required");
        }

        if(addressFormat == null || addressFormat.isBlank()){
            throw new IllegalArgumentException("Address Format is required");
        }
    }

    public String getFormattedAddress(){
        return addressFormat
            .replace("{address}",shipmentCustomer.getCustomerAddressLine1())
            .replace("{city}", shipmentCustomer.getCustomerCity())
            .replace("{state}", shipmentCustomer.getCustomerState())
            .replace("{zipcode}", shipmentCustomer.getCustomerPostalCode())
            .replace("{country}",shipmentCustomer.getCustomerCountry());
    }

    public String getCustomerName(){
        return shipmentCustomer.getCustomerName();
    }
}
