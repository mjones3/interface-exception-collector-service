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
        isValidShippingMethod(shippingMethod,lookupService).subscribe();
    }

    private static Mono<Void> isValidShippingMethod(String shippingMethod , LookupService lookupService) {
        log.info("Checking if shippingMethod type {} is valid", shippingMethod);
        return lookupService.findAllByType(SHIPPING_METHOD_TYPE_CODE).collectList()
            .switchIfEmpty(Mono.error(new IllegalArgumentException("shippingMethod is not a valid order shipping method")))
            .flatMap(lookups -> {
                if (lookups.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(shippingMethod))) {
                    return Mono.error(new IllegalArgumentException("shippingMethod is not a valid order shipping method"));
                }
                return Mono.empty();
            });
    }

}
