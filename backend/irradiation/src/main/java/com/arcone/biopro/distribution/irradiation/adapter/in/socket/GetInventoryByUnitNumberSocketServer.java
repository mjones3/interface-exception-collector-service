package com.arcone.biopro.distribution.irradiation.adapter.in.socket;

import com.arcone.biopro.distribution.irradiation.application.dto.GetInventoryByUnitNumberAndProductInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.usecase.UseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller for getting Inventory by Unit Number using RSocket.
 */
@Slf4j
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GetInventoryByUnitNumberSocketServer {

    UseCase<Flux<InventoryOutput>, String> getByUnitNumberUseCase;
    UseCase<Mono<InventoryOutput>, GetInventoryByUnitNumberAndProductInput> getByUnitNumberAndProductCodeUseCase;

    @MessageMapping("getInventoryByUnitNumber")
    public Flux<InventoryOutput> getInventoryByUnitNumber(String unitNumber) {
        log.info("Getting irradiation for unit number: {}", unitNumber);
        return getByUnitNumberUseCase.execute(unitNumber)
            .doOnNext(response -> log.debug("Found irradiation: {}", response.toString()));
    }

    @MessageMapping("getInventoryByUnitNumberAndProductCode")
    public Mono<InventoryOutput> getInventoryByUnitNumberAndProductCode(GetInventoryByUnitNumberAndProductInput input) {
        log.info("Getting irradiation for unit number: {} and product code: {}", input.unitNumber(), input.productCode());
        return getByUnitNumberAndProductCodeUseCase.execute(input)
            .doOnNext(response -> log.debug("Found irradiation: {}", response.toString()));
    }
}
