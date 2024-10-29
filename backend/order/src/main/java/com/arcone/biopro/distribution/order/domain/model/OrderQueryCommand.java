package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class OrderQueryCommand implements Validatable {

    private String locationCode;
    private String orderNumber;
    private QuerySort querySort;
    private Integer limit;

    private static final String DEFAULT_SORT_DIRECTION = "ASC";
    private static final String DEFAULT_FIRST_SORT_BY = "priority";
    private static final String DEFAULT_SECOND_SORT_BY = "status";
    private static final Integer DEFAULT_LIMIT = 20;

    public OrderQueryCommand(String locationCode , String orderNumber, QuerySort querySort ,   Integer limit) {
        this.orderNumber = orderNumber;
        this.locationCode = locationCode;
        this.querySort = querySort;
        this.limit = limit;
        if(this.limit == null){
            this.limit = DEFAULT_LIMIT;
        }
        if(this.querySort == null){
            this.querySort = new QuerySort(List.of(new QueryOrderBy(DEFAULT_FIRST_SORT_BY,DEFAULT_SORT_DIRECTION),new QueryOrderBy(DEFAULT_SECOND_SORT_BY,DEFAULT_SORT_DIRECTION)));
        }

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.locationCode == null || this.locationCode.isEmpty()) {
            throw new IllegalArgumentException("locationCode cannot be null or empty");
        }

        Assert.notNull(this.querySort, "Sort must not be null");

        if (this.limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
    }
}
