package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ProductCategory implements Validatable {

    private String productCategory;
    private static final String PRODUCT_CATEGORY_TYPE_CODE = "PRODUCT_CATEGORY";
    private final LookupService lookupService;

    public ProductCategory(String productCategory , LookupService lookupService) {
        this.productCategory = productCategory;
        this.lookupService = lookupService;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (productCategory == null || productCategory.isBlank()) {
            throw new IllegalArgumentException("productCategory cannot be null or blank");
        }

        if(!isValidCategory(productCategory,lookupService)){
            throw new IllegalArgumentException("productCategory is not a valid category");
        }
    }

    private static boolean isValidCategory(String category , LookupService lookupService) {

        var list = lookupService.findAllByType(PRODUCT_CATEGORY_TYPE_CODE).collectList().block();
        if(list == null || list.isEmpty()) {
            return false;
        }

        return list.stream().anyMatch(lookup -> lookup.getId().getOptionValue().equals(category));
    }
}
