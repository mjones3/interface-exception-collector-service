package com.arcone.biopro.distribution.irradiation.infrastructure.listener;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.producer.QuarantineProductProducer;
import com.arcone.biopro.distribution.irradiation.infrastructure.event.ProductQuarantinedApplicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductQuarantinedKafkaListener {

    private final QuarantineProductProducer quarantineProductProducer;

    @EventListener
    public void handleProductQuarantined(ProductQuarantinedApplicationEvent event) {
        var quarantineProduct = event.getQuarantineProduct();

        quarantineProductProducer.publishQuarantineProduct(quarantineProduct)
            .doOnSuccess(result -> log.info("Published quarantine event for {} products", quarantineProduct.products().size()))
            .doOnError(error -> log.error("Error publishing quarantine event for {} products", quarantineProduct.products().size(), error))
            .subscribe();
    }
}
