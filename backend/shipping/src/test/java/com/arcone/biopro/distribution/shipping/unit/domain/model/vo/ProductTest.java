package com.arcone.biopro.distribution.shipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProductTest {


    @Test
    void shouldCreate() {

        var product = new Product("UNIT", "CODE","FAMILY");
        Assertions.assertEquals("UNIT", product.getUnitNumber());
        Assertions.assertEquals("CODE", product.getProductCode());
        Assertions.assertEquals("FAMILY", product.getProductFamily());
    }

    @Test
    void shouldNotCreate() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product(null, "CODE","FAMILY"), "Unit Number cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("", "CODE","FAMILY"), "Unit Number cannot be empty");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("UNIT", null,"FAMILY"), "Product code  cannot be null");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("UNIT", "","FAMILY"), "Product code cannot be empty");
    }

}
