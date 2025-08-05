package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

@Getter
@EqualsAndHashCode
@ToString
public class OrderPriorityReport implements Validatable {

    private final String priority;
    private final String priorityColor;

    public OrderPriorityReport(String priority, String priorityColor) {
        this.priority = priority;
        this.priorityColor = priorityColor;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        Assert.notNull(priority, "Priority must not be null");
        Assert.notNull(priorityColor, "PriorityColor must not be null");
    }

}
