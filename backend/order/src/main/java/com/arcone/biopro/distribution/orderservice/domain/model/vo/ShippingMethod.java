package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ShippingMethod implements Validatable {

    private String shippingMethod;
    private static final String SHIPPING_METHOD_TYPE_CODE = "ORDER_SHIPPING_METHOD";
    private final LookupService lookupService;

    public ShippingMethod(String shippingMethod , LookupService lookupService) {
        this.shippingMethod = shippingMethod;
        this.lookupService = lookupService;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (shippingMethod == null || shippingMethod.isBlank()) {
            throw new IllegalArgumentException("shippingMethod cannot be null or blank");
        }
        if(!isValidShipmentType(shippingMethod,lookupService)){
            throw new IllegalArgumentException("shippingMethod is not a valid order shipping method");
        }
    }

    private static boolean isValidShipmentType(String shippingMethod , LookupService lookupService) {

        var list = lookupService.findAllByType(SHIPPING_METHOD_TYPE_CODE).collectList().block();
        if(list == null || list.isEmpty()) {
            return false;
        }

        return list.stream().anyMatch(lookup -> lookup.getId().getOptionValue().equals(shippingMethod));
    }

}
