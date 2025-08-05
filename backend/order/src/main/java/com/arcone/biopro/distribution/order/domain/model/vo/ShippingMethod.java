package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
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
        isValidShippingMethod(shippingMethod,lookupService);
    }

    private static void isValidShippingMethod(String shippingMethod , LookupService lookupService) {
        log.info("Checking if shippingMethod type {} is valid", shippingMethod);

        var types = lookupService.findAllByType(SHIPPING_METHOD_TYPE_CODE).collectList().block();

        if(types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Shipment Method " + shippingMethod + " is not valid");
        }

        if (types.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(shippingMethod))) {
            throw new IllegalArgumentException("Shipment Method " + shippingMethod + " is not valid");
        }
    }

}
