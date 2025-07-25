package com.arcone.biopro.distribution.irradiation.domain.event;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified;

public class ProductModifiedEvent extends IrradiationEvent {
    
    public ProductModifiedEvent(ProductModified productModified) {
        super(productModified);
    }
    
    public ProductModified getProductModified() {
        return (ProductModified) getSource();
    }
}