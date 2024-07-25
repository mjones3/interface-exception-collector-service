package com.arcone.biopro.distribution.orderservice.domain.model;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.OrderItemOrderId;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.ProductFamily;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

import static java.util.Optional.ofNullable;

@Getter
@EqualsAndHashCode
@ToString
public class OrderItem implements Validatable {

    private Long id;
    private OrderItemOrderId orderId;
    private ProductFamily productFamily;
    private BloodType bloodType;
    private Integer quantity;
    private String comments;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;

    public OrderItem(Long id, Long orderId, String productFamily, String bloodType, Integer quantity, String comments, ZonedDateTime createDate, ZonedDateTime modificationDate) {
        this.id = id;
        this.orderId = ofNullable(orderId).map(OrderItemOrderId::new).orElse(null);
        // TODO Fix with the new parameters
        //this.productFamily = new ProductFamily(productFamily);
        //this.bloodType = ofNullable(bloodType).map(BloodType::new,null,null).orElse(null);
        this.quantity = quantity;
        this.comments = comments;
        this.createDate = createDate;
        this.modificationDate = modificationDate;

        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.productFamily == null) {
            throw new IllegalArgumentException("productFamily cannot be null");
        }
        if (this.bloodType == null) {
            throw new IllegalArgumentException("bloodType cannot be null or blank");
        }
        if (this.quantity == null) {
            throw new IllegalArgumentException("quantity cannot be null");
        }
    }

}
