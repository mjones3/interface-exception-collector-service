package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderItemOrderId;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductFamily;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;

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
    private Integer quantityAvailable;

    public OrderItem(Long id, Long orderId, String productFamily, String bloodType, Integer quantity, String comments
        , ZonedDateTime createDate, ZonedDateTime modificationDate, String productCategory , OrderConfigService orderConfigService) {
        this.id = id;
        this.orderId = new OrderItemOrderId(orderId);
        this.productFamily = new ProductFamily(productFamily,productCategory,orderConfigService);
        this.bloodType = new BloodType(bloodType,productFamily,orderConfigService);
        this.quantity = quantity;
        this.comments = comments;
        this.createDate = createDate;
        this.modificationDate = modificationDate;
        this.quantityAvailable = 0;

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
        if(this.quantity < 1){
            throw new IllegalArgumentException("quantity cannot be less than 1");
        }
    }

    public void defineAvailableQuantity(Integer quantity){
        Assert.notNull(quantity, "Quantity must not be null");
        Assert.isTrue(quantity >=0 , "Quantity must not be negative");
        this.quantityAvailable = quantity;
    }

}
