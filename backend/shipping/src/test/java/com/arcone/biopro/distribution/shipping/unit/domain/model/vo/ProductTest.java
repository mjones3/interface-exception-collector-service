package com.arcone.biopro.distribution.shipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProductTest {


    @Test
    void shouldCreate() {

        var product = new Product("UNIT", "CODE");
        Assertions.assertEquals("UNIT", product.getUnitNumber());
    }

    @Test
    void shouldNotCreate() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product(null, "CODE"), "Unit Number cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("", "CODE"), "Unit Number cannot be empty");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("UNIT", null), "Product code  cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("UNIT", ""), "Product code cannot be empty");

    }

}
