package com.arcone.biopro.distribution.irradiation.adapter.irradiation;

import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class IrradiationResource {
    private final ValidateDeviceUseCase validateDeviceUseCase;
    private final ValidateUnitNumberUseCase validateUnitNumberUseCase;

    @QueryMapping
    public Mono<Boolean> validateDevice(@Argument String deviceId, @Argument String location) {
        return validateDeviceUseCase.execute(deviceId, location)
                .onErrorReturn(false);
    }

    @QueryMapping
    public Flux<Inventory> validateUnit(@Argument String unitNumber, @Argument String location) {
        return validateUnitNumberUseCase.execute(unitNumber, location);
    }
}
