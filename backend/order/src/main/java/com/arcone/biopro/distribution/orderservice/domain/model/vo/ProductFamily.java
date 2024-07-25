package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.OrderConfigService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
        if(!isValidFamily(productFamily,productCategory,orderConfigService)){
            throw new IllegalArgumentException("Invalid product family for the specified product category:"+productCategory);
        }
    }

    private static boolean isValidFamily(String productFamily , String productCategory , OrderConfigService orderConfigService) {

        var bloodTypeResponse = orderConfigService.findProductFamilyByCategory(productCategory, productFamily).block();
        if(bloodTypeResponse == null || bloodTypeResponse.isBlank()) {
            return false;
        }

        return true;

    }

}
