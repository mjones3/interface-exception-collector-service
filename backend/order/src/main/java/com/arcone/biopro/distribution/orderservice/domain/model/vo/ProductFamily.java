package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

@Getter
@EqualsAndHashCode
@ToString
public class ProductFamily implements Validatable {

    private String productFamily;
    private String productCategory;
    private final OrderConfigService orderConfigService;


    public ProductFamily(String productFamily , String productCategory, OrderConfigService orderConfigService) {
        this.productFamily = productFamily;
        this.orderConfigService = orderConfigService;
        this.productCategory = productCategory;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (productFamily == null || productFamily.isBlank()) {
            throw new IllegalArgumentException("productFamily cannot be null or blank");
        }
        isValidFamily(productFamily,productCategory,orderConfigService);
    }

    private static void isValidFamily(String productFamily , String productCategory , OrderConfigService orderConfigService) {

        var family = orderConfigService.findProductFamilyByCategory(productCategory, productFamily).block();
        if(family == null) {
            throw new IllegalArgumentException("Invalid product family for the specified product category:"+productCategory);
        }
    }

}
