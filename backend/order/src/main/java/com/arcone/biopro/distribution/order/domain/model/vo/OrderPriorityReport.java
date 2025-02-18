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

    private String priority;
    private String priorityColor;

    public OrderPriorityReport(String status, String priorityColor) {
        this.priority = status;
        this.priorityColor = priorityColor;
    }

    @Override
    public void checkValid() {
        Assert.notNull(priority, "Status must not be null");
        Assert.notNull(priorityColor, "PriorityColor must not be null");
    }
}
