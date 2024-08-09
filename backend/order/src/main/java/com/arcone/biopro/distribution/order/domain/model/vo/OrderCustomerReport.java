package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class OrderCustomerReport implements Validatable {

    private String code;
    private String name;

    public OrderCustomerReport(String code, String name) {
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
