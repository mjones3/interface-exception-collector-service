package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class AvailableInventory implements Validatable {

    private String productFamily;
    private String aboRh;
    private Integer quantityAvailable;
    private List<ShortDateProduct> shortDateProducts;

    public AvailableInventory(String productFamily, String aboRh, Integer quantityAvailable, List<ShortDateProduct> shortDateProducts) {
        this.productFamily = productFamily;
        this.aboRh = aboRh;
        this.quantityAvailable = quantityAvailable;
        this.shortDateProducts = shortDateProducts;
    }

    @Override
    public void checkValid() {
        if (this.productFamily == null || this.productFamily.isBlank()) {
            throw new IllegalArgumentException("productFamily cannot be null or blank");
        }

        if (this.aboRh == null || this.aboRh.isEmpty()) {
            throw new IllegalArgumentException("aboRh cannot be null or empty");
        }

        if (this.quantityAvailable == null || this.quantityAvailable < 0) {
            throw new IllegalArgumentException("quantityAvailable cannot be null or less than 0");
        }
    }
}
