package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderCustomer implements Validatable {

    private String code;
    private String name;

    public OrderCustomer(String code, String name) {
        this.code = code;
        this.name = name;
        this.checkValid();
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
