package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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

        if(!isValidOrderPriority(orderPriority,lookupService)){
            throw new IllegalArgumentException("orderPriority is not a valid order priority");
        }


    }

    private static boolean isValidOrderPriority(String orderPriority , LookupService lookupService) {

        var list = lookupService.findAllByType(PRIORITY_TYPE_CODE).collectList().block();
        if(list == null || list.isEmpty()) {
            return false;
        }

        return list.stream().anyMatch(lookup -> lookup.getId().getOptionValue().equals(orderPriority));
    }
}
