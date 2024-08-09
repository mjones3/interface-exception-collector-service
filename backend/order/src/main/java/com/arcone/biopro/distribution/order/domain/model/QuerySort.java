package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class QuerySort implements Validatable {
    private List<QueryOrderBy> queryOrderByList;

    public QuerySort(List<QueryOrderBy> queryOrderByList) {
        this.queryOrderByList = queryOrderByList;
        checkValid();
    }

    @Override
    public void checkValid() {
        Assert.notNull(queryOrderByList, "QueryOrderByList must not be null");
        Assert.notEmpty(queryOrderByList, "QueryOrderByList must not be empty");
    }
}
