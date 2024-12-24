package com.arcone.biopro.distribution.inventory.infrastructure.listener;

import com.arcone.biopro.distribution.inventory.application.dto.ProductConvertedInput;
import com.arcone.biopro.distribution.inventory.application.usecase.ProductConvertedUseCase;
import com.arcone.biopro.distribution.inventory.domain.event.ProductCreatedEvent;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCreatedListener {

    private final ProductConvertedUseCase useCase;

    @EventListener
    public void convertedProduct(ProductCreatedEvent inventoryCreatedEvent) {
        var aggregate = inventoryCreatedEvent.aggregate();
        var parentProductCode = aggregate.getInventory().getInputProducts().getFirst().productCode();
        var input = new ProductConvertedInput(aggregate.getInventory().getUnitNumber(), new ProductCode(parentProductCode));
        useCase.execute(input)
            .retryWhen(Retry
                .fixedDelay(3, Duration.ofSeconds(60))
                .doBeforeRetry(retrySignal ->
                    log.warn("Retrying due to error: {}. Attempt: {}",
                        retrySignal.failure().getMessage(),
                        retrySignal.totalRetries())))
            .subscribe();
    }
}
