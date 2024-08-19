package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
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

        isValidCategory(productCategory, lookupService);
    }

    private static void isValidCategory(String category , LookupService lookupService) {

        var types = lookupService.findAllByType(PRODUCT_CATEGORY_TYPE_CODE).collectList().block();

        if(types == null || types.isEmpty()) {
            throw new IllegalArgumentException("Product Category " + category + " is not valid");
        }

        if (types.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(category))) {
            throw new IllegalArgumentException("Product Category " + category + " is not valid");
        }
    }
}
