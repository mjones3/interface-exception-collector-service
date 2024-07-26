package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

@Getter
@EqualsAndHashCode
@ToString
public class OrderPriority implements Validatable {

    private String orderPriority;
    private static final String PRIORITY_TYPE_CODE = "ORDER_PRIORITY";
    private final LookupService lookupService;

    public OrderPriority(String orderPriority , LookupService lookupService) {
        this.orderPriority = orderPriority;
        this.lookupService = lookupService;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (orderPriority == null || orderPriority.isBlank()) {
            throw new IllegalArgumentException("orderPriority cannot be null or blank");
        }
        isValidOrderPriority(orderPriority, lookupService).subscribe();
    }


    private static Mono<Void> isValidOrderPriority(String orderPriority , LookupService lookupService) {
        return lookupService.findAllByType(PRIORITY_TYPE_CODE).collectList()
            .switchIfEmpty(Mono.error(new IllegalArgumentException("orderPriority is not a valid order priority")))
            .flatMap(lookups -> {
                if (lookups.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(orderPriority))) {
                    return Mono.error(new IllegalArgumentException("orderPriority is not a valid order priority"));
                }
                return Mono.empty();
            });
    }
}
