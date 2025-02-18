package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@EqualsAndHashCode
@ToString
public class ShipmentLineItem {

    private final String productFamily;
    private final Integer quantityOrdered;
    private List<ShipmentLineItemProduct> products;

    public ShipmentLineItem(String productFamily, Integer quantityOrdered) {

        Assert.notNull(productFamily, "Product Family must not be null");
        Assert.notNull(quantityOrdered, "Quantity Ordered must not be null");
        Assert.isTrue(quantityOrdered > 0, "Quantity Ordered must be greater than zero");

        this.productFamily = productFamily;
        this.quantityOrdered = quantityOrdered;
    }

    public void addProduct(final String unitNumber , final String productCode, final String bloodType, final LocalDateTime expirationDate, final ZonedDateTime collectionDate) {
        if (products == null) {
            products = new ArrayList<>();
        }
        products.add(new ShipmentLineItemProduct(unitNumber,productCode, bloodType, expirationDate, collectionDate));
    }

    public Integer getQuantityFilled() {
        if(products != null){
            return products.size();
        }
        return 0;
    }

}
