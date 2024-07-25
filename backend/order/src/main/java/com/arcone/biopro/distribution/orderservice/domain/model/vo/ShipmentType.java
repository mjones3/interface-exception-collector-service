package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ShipmentType implements Validatable {

    private String shipmentType;
    private static final String SHIPMENT_TYPE_CODE = "ORDER_SHIPMENT_TYPE";
    private final LookupService lookupService;

    public ShipmentType(String shipmentType , LookupService lookupService) {
        this.shipmentType = shipmentType;
        this.lookupService = lookupService;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (shipmentType == null || shipmentType.isBlank()) {
            throw new IllegalArgumentException("shipmentType cannot be null or blank");
        }
        if(!isValidShipmentType(shipmentType,lookupService)){
            throw new IllegalArgumentException("shipmentType is not a valid order shipment type");
        }
    }

    private static boolean isValidShipmentType(String shipmentType , LookupService lookupService) {

        var list = lookupService.findAllByType(SHIPMENT_TYPE_CODE).collectList().block();
        if(list == null || list.isEmpty()) {
            return false;
        }

        return list.stream().anyMatch(lookup -> lookup.getId().getOptionValue().equals(shipmentType));
    }

}
