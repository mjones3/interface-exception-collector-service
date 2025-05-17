package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ShippingSummaryCartonItem implements Validatable {

    private String cartonNumber;
    private String productCode;
    private String productDescription;
    private int totalProducts;

    public ShippingSummaryCartonItem(final Carton carton) {
        this.cartonNumber = carton.getCartonNumber();
        this.productCode = carton.getProducts().getFirst().getProductCode();
        this.productDescription = carton.getProducts().getFirst().getProductDescription();
        this.totalProducts = carton.getTotalProducts();
        checkValid();
    }

    @Override
    public void checkValid() {
        if(cartonNumber == null || cartonNumber.isBlank()){
            throw new IllegalArgumentException("Carton Number is required");
        }

        if(productCode == null || productCode.isBlank()){
            throw new IllegalArgumentException("Product Code is required");
        }

        if(productDescription == null || productDescription.isBlank()){
            throw new IllegalArgumentException("Product Description is required");
        }
    }
}
