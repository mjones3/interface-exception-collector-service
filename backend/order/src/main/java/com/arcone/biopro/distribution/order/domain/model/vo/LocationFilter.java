package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class LocationFilter implements Validatable {

    private final String code;
    private final String name;

    public LocationFilter(String code, String name) {
        this.code = code;
        this.name = name;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("code cannot be null or blank");
        }

        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
    }
}
