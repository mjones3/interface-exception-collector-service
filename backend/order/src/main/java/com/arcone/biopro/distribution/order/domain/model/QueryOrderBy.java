package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.util.Arrays;

@Getter
@EqualsAndHashCode
@ToString
public class QueryOrderBy implements Validatable {
    private String property;
    private String direction;
    private final static String[] SORT_DIRECTIONS = {"ASC", "DESC"};

    public QueryOrderBy(String property, String direction) {
        this.property = property;
        this.direction = direction;
        checkValid();
    }


    @Override
    public void checkValid() {
        Assert.notNull(direction, "Direction must not be null");
        Assert.notNull(property, "Property must not be null");
        Assert.isTrue(Arrays.stream(SORT_DIRECTIONS).anyMatch(s -> s.equals(direction)), "Direction is invalid");

    }
}
