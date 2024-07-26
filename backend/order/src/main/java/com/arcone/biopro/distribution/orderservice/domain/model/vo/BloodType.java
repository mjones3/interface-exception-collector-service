package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

@Getter
@EqualsAndHashCode
@ToString
public class BloodType implements Validatable {

    private String bloodType;
    private String productFamily;
    private final OrderConfigService orderConfigService;

    public BloodType(String bloodType, String productFamily , OrderConfigService orderConfigService) {
        this.bloodType = bloodType;
        this.orderConfigService = orderConfigService;
        this.productFamily = productFamily;
        checkValid();
    }

    @Override
    public void checkValid() {
        if (bloodType == null || bloodType.isBlank()) {
            throw new IllegalArgumentException("bloodType cannot be null or blank");
        }

        isValidBloodType(bloodType,productFamily, orderConfigService).subscribe();
    }

    private static Mono<String> isValidBloodType(String bloodType , String productFamily , OrderConfigService orderConfigService) {
        return orderConfigService.findBloodTypeByFamilyAndType(productFamily, bloodType)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("bloodType is not a valid blood type for this product family "+productFamily)));
    }

}
