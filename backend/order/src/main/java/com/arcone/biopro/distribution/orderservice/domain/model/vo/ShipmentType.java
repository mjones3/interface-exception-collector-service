package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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

        isValidShipmentType(shipmentType,lookupService).subscribe();
    }

    private static Mono<Void> isValidShipmentType(String shipmentType , LookupService lookupService) {

        log.info("Checking if shipment type {} is valid", shipmentType);

        return lookupService.findAllByType(SHIPMENT_TYPE_CODE).collectList()
            .switchIfEmpty(Mono.error(new IllegalArgumentException("shipmentType is not a valid order shipment type")))
            .flatMap(lookups -> {
                if (lookups.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(shipmentType))) {
                    return Mono.error(new IllegalArgumentException("shipmentType is not a valid order shipment type"));
                }
                return Mono.empty();
            });
    }

}
