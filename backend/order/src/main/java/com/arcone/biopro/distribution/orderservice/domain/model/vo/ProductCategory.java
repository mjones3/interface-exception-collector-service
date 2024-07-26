package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

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

        isValidCategory(productCategory, lookupService).subscribe();
    }

    private static Mono<Void> isValidCategory(String category , LookupService lookupService) {
        return lookupService.findAllByType(PRODUCT_CATEGORY_TYPE_CODE).collectList()
            .switchIfEmpty(Mono.error(new IllegalArgumentException("productCategory is not a valid category")))
            .flatMap(lookups -> {
                if (lookups.stream().noneMatch(lookup -> lookup.getId().getOptionValue().equals(category))) {
                    return Mono.error(new IllegalArgumentException("productCategory is not a valid category"));
                }
                return Mono.empty();
            });
    }
}
