package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.application.dto.Product;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.QuarantineReason;
import com.arcone.biopro.distribution.inventory.domain.model.vo.CreatedDate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"createDate"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductQuarantine implements Serializable {

    UUID Id;

    UUID productId;

    QuarantineReason reason;

    CreatedDate createDate;


    public ProductQuarantine(Product product, QuarantineReason reason) {
        this.Id = UUID.randomUUID();
        this.productId = UUID.fromString(product.productCode());
        this.reason = reason;
    }
}
