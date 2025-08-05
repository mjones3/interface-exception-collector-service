package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderPriority implements Validatable {

    private Integer priority;
    private String deliveryType;
    private static final String PRIORITY_TYPE_CODE = "ORDER_PRIORITY";
    private final LookupService lookupService;

    public OrderPriority(String deliveryType , LookupService lookupService) {
        this.deliveryType = deliveryType;
        this.lookupService = lookupService;
        this.checkValid();
        this.priority = definePriority(deliveryType, lookupService);
    }

    @Override
    public void checkValid() {
        if (deliveryType == null || deliveryType.isBlank()) {
            throw new IllegalArgumentException("orderPriority cannot be null or blank");
        }

        isValidOrderPriority(deliveryType, lookupService);
    }


    private static void isValidOrderPriority(String deliveryType , LookupService lookupService) {

        var types = lookupService.findAllByType(PRIORITY_TYPE_CODE).collectList().block();

        if(types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Order Priority " + deliveryType + " is not valid");
        }

        if (types.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(deliveryType))) {
            throw new IllegalArgumentException("Order Priority " + deliveryType + " is not valid");
        }
    }

    private static Integer definePriority(String deliveryType, LookupService lookupService) {
        var types = lookupService.findAllByType(PRIORITY_TYPE_CODE).collectList().block();

        if(types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Priority Not found for delivery type " + deliveryType);
        }

        return types.stream()
            .filter(lookup -> lookup.getId().getOptionValue().equals(deliveryType))
            .findFirst()
            .map(lookup -> lookup.getOrderNumber())
            .orElse(0);

    }
}
