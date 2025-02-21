package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.vo.Product;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ExternalTransferItem implements Validatable {


    private final Long id;
    private final Long externalTransferId;
    private final Product product;
    private final String createdByEmployeeId;

    public ExternalTransferItem(Long id, Long externalTransferId, String unitNumber , String productCode , String productFamily, String createdByEmployeeId) {
        this.id = id;
        this.externalTransferId = externalTransferId;
        this.product = new Product(unitNumber, productCode,productFamily);
        this.createdByEmployeeId = createdByEmployeeId;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.externalTransferId == null) {
            throw new IllegalArgumentException("External Transfer ID cannot be null");
        }
        if(this.product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (this.createdByEmployeeId == null || this.createdByEmployeeId.isBlank()) {
            throw new IllegalArgumentException("Create Employee ID cannot be null or blank");
        }

    }
}
