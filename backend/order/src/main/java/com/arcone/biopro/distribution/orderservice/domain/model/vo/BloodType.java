package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
        if(!isValidBloodType(bloodType,productFamily,orderConfigService)) {
            throw new IllegalArgumentException("bloodType is not a valid blood type for this product family "+productFamily);
        }
    }

    private static boolean isValidBloodType(String bloodType , String productFamily , OrderConfigService orderConfigService) {

        var bloodTypeResponse = orderConfigService.findBloodTypeByFamilyAndType(productFamily, bloodType).block();
        if(bloodTypeResponse == null || bloodTypeResponse.isBlank()) {
            return false;
        }

        return true;

    }

}
