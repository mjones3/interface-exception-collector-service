package com.arcone.biopro.distribution.irradiation.infrastructure.listener;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.ProductModifiedProducer;
import com.arcone.biopro.distribution.irradiation.infrastructure.event.ProductModifiedApplicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductModifiedKafkaListener {

    private final ProductModifiedProducer productModifiedProducer;

    @EventListener
    public void handleProductModified(ProductModifiedApplicationEvent event) {
        var productModified = event.getProductModified();

        productModifiedProducer.publishProductModified(productModified)
            .doOnSuccess(result -> log.info("Published product modified event for unit: {}", productModified.unitNumber()))
            .doOnError(error -> log.error("Error publishing product modified event for unit: {}", productModified.unitNumber(), error))
            .subscribe();
    }
}
