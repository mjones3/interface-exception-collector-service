package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class LookupId implements Validatable {

    private final String type;
    private final String optionValue;

    public LookupId(
        String type,
        String optionValue
    ) {
        this.type = type;
        this.optionValue = optionValue;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type cannot be null or blank");
        }
        if (optionValue == null || optionValue.isBlank()) {
            throw new IllegalArgumentException("optionValue cannot be null or blank");
        }
    }

}
