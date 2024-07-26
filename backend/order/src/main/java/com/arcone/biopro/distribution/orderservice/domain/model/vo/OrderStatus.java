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
public class OrderStatus implements Validatable {

    private String orderStatus;
    private static final String ORDER_STATUS_TYPE_CODE = "ORDER_STATUS";
    private final LookupService lookupService;

    public OrderStatus(String orderStatus, LookupService lookupService) {
        this.orderStatus = orderStatus;
        this.lookupService = lookupService;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (orderStatus == null || orderStatus.isBlank()) {
            throw new IllegalArgumentException("orderStatus cannot be null or blank");
        }
        isValidStatus(orderStatus,lookupService).subscribe();
    }
    private static Mono<Void> isValidStatus(String orderStatus , LookupService lookupService) {
        return lookupService.findAllByType(ORDER_STATUS_TYPE_CODE).collectList()
            .switchIfEmpty(Mono.error(new IllegalArgumentException("orderStatus is not a valid order status")))
            .flatMap(lookups -> {
                if (lookups.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(orderStatus))) {
                    return Mono.error(new IllegalArgumentException("orderStatus is not a valid order status"));
                }
                return Mono.empty();
            });
    }

}
