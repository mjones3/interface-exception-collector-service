package com.arcone.biopro.distribution.irradiation.infrastructure.event;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.ProductModified;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductModifiedApplicationEvent extends ApplicationEvent {
    
    private final ProductModified productModified;
    
    public ProductModifiedApplicationEvent(ProductModified source) {
        super(source);
        this.productModified = source;
    }
}