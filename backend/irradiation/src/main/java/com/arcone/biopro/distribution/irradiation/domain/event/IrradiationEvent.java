package com.arcone.biopro.distribution.irradiation.domain.event;

import lombok.Getter;

@Getter
public abstract class IrradiationEvent {
    private final Object source;
    
    protected IrradiationEvent(Object source) {
        this.source = source;
    }
}