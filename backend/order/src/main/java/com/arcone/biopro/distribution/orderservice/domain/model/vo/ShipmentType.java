package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
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

        isValidShipmentType(shipmentType,lookupService);
    }

    private static void isValidShipmentType(String shipmentType , LookupService lookupService) {

        log.info("Checking if shipment type {} is valid", shipmentType);

        var types = lookupService.findAllByType(SHIPMENT_TYPE_CODE).collectList().block();

        if(types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Shipment type " + shipmentType + " is not valid");
        }

        if (types.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(shipmentType))) {
            throw new IllegalArgumentException("Shipment type " + shipmentType + " is not valid");
        }

    }

}
