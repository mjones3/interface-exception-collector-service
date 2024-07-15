package com.arcone.biopro.distribution.orderservice.domain.model;

public record LookupId(
    String type,
    String optionValue
) implements Validatable {

    @Override
    public boolean isValid() {
        return type != null
            && !type.isBlank()
            && optionValue != null
            && !optionValue.isBlank();
    }

}
