package com.arcone.biopro.distribution.irradiation.domain.event;

public interface IrradiationEventPublisher {
    void publish(IrradiationEvent event);
}