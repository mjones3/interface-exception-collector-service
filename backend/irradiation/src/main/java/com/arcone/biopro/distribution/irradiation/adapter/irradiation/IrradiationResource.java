package com.arcone.biopro.distribution.irradiation.adapter.irradiation;

import com.arcone.biopro.distribution.irradiation.application.usecase.CheckBatchStatusUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class IrradiationResource {
    private final ValidateDeviceUseCase validateDeviceUseCase;
    private final ValidateUnitNumberUseCase validateUnitNumberUseCase;
    private final CheckBatchStatusUseCase checkBatchStatusUseCase;

    public IrradiationResource(ValidateDeviceUseCase validateDeviceUseCase,
                              ValidateUnitNumberUseCase validateUnitNumberUseCase,
                              CheckBatchStatusUseCase checkBatchStatusUseCase) {
        this.validateDeviceUseCase = validateDeviceUseCase;
        this.validateUnitNumberUseCase = validateUnitNumberUseCase;
        this.checkBatchStatusUseCase = checkBatchStatusUseCase;
    }

    @QueryMapping
    public Mono<Boolean> validateDevice(@Argument String deviceId, @Argument String location) {
        return validateDeviceUseCase.execute(deviceId, location);
    }

    @QueryMapping
    public Flux<Inventory> validateUnit(@Argument String unitNumber, @Argument String location) {
        return validateUnitNumberUseCase.execute(unitNumber, location);
    }

    @QueryMapping
    public Mono<Boolean> isBatchActive(@Argument String batchId) {
        return checkBatchStatusUseCase.execute(batchId);
    }
}
