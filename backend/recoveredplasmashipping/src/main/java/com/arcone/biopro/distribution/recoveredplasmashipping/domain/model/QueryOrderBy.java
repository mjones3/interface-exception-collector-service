package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSortedSet;

@Getter
@EqualsAndHashCode
@ToString
public class QueryOrderBy implements Validatable {

    private final static SortedSet<String> SORT_DIRECTIONS = unmodifiableSortedSet(new TreeSet<>(Set.of("ASC", "DESC")));

    private final String property;
    private final String direction;

    public QueryOrderBy(String property, String direction) {
        this.property = property;
        this.direction = direction;
        checkValid();
    }

    @Override
    public void checkValid() {
        Assert.notNull(property, "Property must not be null");
        Assert.notNull(direction, "Direction must not be null");
        Assert.isTrue(SORT_DIRECTIONS.contains(direction), "Direction is invalid");
    }

}
