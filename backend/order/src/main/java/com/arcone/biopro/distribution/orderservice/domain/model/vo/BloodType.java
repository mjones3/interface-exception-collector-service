package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class BloodType implements Validatable {

    private String bloodType;

    public BloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    @Override
    public void checkValid() {
        if (bloodType == null || bloodType.isBlank()) {
            throw new IllegalArgumentException("bloodType cannot be null or blank");
        }
    }

}
