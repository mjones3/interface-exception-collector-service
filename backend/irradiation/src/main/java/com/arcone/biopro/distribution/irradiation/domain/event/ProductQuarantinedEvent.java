package com.arcone.biopro.distribution.irradiation.domain.event;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;

public class ProductQuarantinedEvent extends IrradiationEvent {
    
    public ProductQuarantinedEvent(QuarantineProduct quarantineProduct) {
        super(quarantineProduct);
    }
    
    public QuarantineProduct getQuarantineProduct() {
        return (QuarantineProduct) getSource();
    }
}