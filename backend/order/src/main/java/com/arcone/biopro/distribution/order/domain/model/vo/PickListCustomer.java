package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

@Getter
@EqualsAndHashCode
@ToString
public class PickListCustomer implements Validatable {

    private String code;
    private String name;

    public PickListCustomer(String code, String name) {
        this.code = code;
        this.name = name;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        Assert.notNull(code, "Code must not be null");
        Assert.notNull(name, "Name must not be null");
    }

}
