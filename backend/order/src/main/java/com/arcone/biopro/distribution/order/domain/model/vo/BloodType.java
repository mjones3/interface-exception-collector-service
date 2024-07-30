package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
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

        isValidBloodType(bloodType,productFamily, orderConfigService);
    }

    private static void isValidBloodType(String bloodType , String productFamily , OrderConfigService orderConfigService) {


        var bloodTypeResponse = orderConfigService.findBloodTypeByFamilyAndType(productFamily, bloodType).block();
        if(bloodTypeResponse == null) {
            throw new IllegalArgumentException("Invalid blood type "+bloodType+" for the specified product family:"+productFamily);
        }
    }

}
