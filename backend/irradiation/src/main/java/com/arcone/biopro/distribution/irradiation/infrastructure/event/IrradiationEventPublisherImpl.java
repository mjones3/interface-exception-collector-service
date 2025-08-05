package com.arcone.biopro.distribution.irradiation.infrastructure.event;

import com.arcone.biopro.distribution.irradiation.domain.event.IrradiationEvent;
import com.arcone.biopro.distribution.irradiation.domain.event.IrradiationEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.event.ProductModifiedEvent;
import com.arcone.biopro.distribution.irradiation.domain.event.ProductQuarantinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IrradiationEventPublisherImpl implements IrradiationEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(IrradiationEvent event) {
        applicationEventPublisher.publishEvent(build(event));
    }

    private ApplicationEvent build(IrradiationEvent event) {
        if (event instanceof ProductModifiedEvent productModifiedEvent) {
            return new ProductModifiedApplicationEvent(productModifiedEvent.getProductModified());
        }

        if (event instanceof ProductQuarantinedEvent productQuarantinedEvent) {
            return new ProductQuarantinedApplicationEvent(productQuarantinedEvent.getQuarantineProduct());
        }

        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
}
