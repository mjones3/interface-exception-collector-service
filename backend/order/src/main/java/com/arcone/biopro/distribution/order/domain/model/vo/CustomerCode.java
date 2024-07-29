package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
@ToString
public class CustomerCode implements Validatable {

    static final Pattern CUSTOMER_CODE_ALPHANUMERIC_REGEX = Pattern.compile("^\\p{Alnum}+$");

    private final String value;

    public CustomerCode(String value) {
        this.value = value;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.value == null || this.value.isBlank()) {
            throw new IllegalArgumentException("value cannot be null or blank");
        }
        if (!CUSTOMER_CODE_ALPHANUMERIC_REGEX.matcher(this.value).matches()) {
            throw new IllegalArgumentException(String.format("value \"%s\" for customer code is invalid: allowed characters are alphanumeric only", this.value));
        }
    }

}
