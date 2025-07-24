package com.arcone.biopro.distribution.irradiation.infrastructure.event;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductQuarantinedApplicationEvent extends ApplicationEvent {
    
    private final QuarantineProduct quarantineProduct;
    
    public ProductQuarantinedApplicationEvent(QuarantineProduct source) {
        super(source);
        this.quarantineProduct = source;
    }
}