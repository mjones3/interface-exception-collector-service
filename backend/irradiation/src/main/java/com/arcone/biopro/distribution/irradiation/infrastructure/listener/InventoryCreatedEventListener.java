package com.arcone.biopro.distribution.irradiation.infrastructure.listener;

import com.arcone.biopro.distribution.irradiation.application.dto.ProductConvertedInput;
import com.arcone.biopro.distribution.irradiation.application.usecase.ProductConvertedUseCase;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryCreatedEvent;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.ProductCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import java.time.Duration;


@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCreatedEventListener {

    private final ProductConvertedUseCase useCase;

    @EventListener
    public void convertedProduct(InventoryCreatedEvent inventoryCreatedEvent) {
        var aggregate = inventoryCreatedEvent.aggregate();
        if (aggregate.hasParent()) {
            var parentProductCode = aggregate.getInventory().getInputProducts().getFirst().productCode();
            var input = new ProductConvertedInput(aggregate.getInventory().getUnitNumber(), new ProductCode(parentProductCode));
            useCase.execute(input)
                .retryWhen(Retry
                    .backoff(3, Duration.ofSeconds(5))
                    .jitter(0.5)
                    .doBeforeRetry(retrySignal ->
                        log.warn("Retrying due to error: {}. Attempt: {}",
                            retrySignal.failure().getMessage(),
                            retrySignal.totalRetries())))
                .subscribe();
        }
    }
}
