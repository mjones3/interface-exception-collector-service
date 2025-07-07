package com.arcone.biopro.distribution.irradiation.adapter.irradiation;

import com.arcone.biopro.distribution.irradiation.application.irradiation.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.irradiation.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/irradiation")
public class IrradiationResource {
    private final ValidateDeviceUseCase validateDeviceUseCase;
    private final ValidateUnitNumberUseCase validateUnitNumberUseCase;

    public IrradiationResource(ValidateDeviceUseCase validateDeviceUseCase, 
                              ValidateUnitNumberUseCase validateUnitNumberUseCase) {
        this.validateDeviceUseCase = validateDeviceUseCase;
        this.validateUnitNumberUseCase = validateUnitNumberUseCase;
    }

    @PostMapping("/validate-device")
    public Mono<Boolean> validateDevice(@RequestBody ValidateDeviceRequest request) {
        return validateDeviceUseCase.execute(request.getDeviceId(), request.getLocation());
    }

    @PostMapping("/validate-unit")
    public Flux<Inventory> validateUnit(@RequestBody ValidateUnitRequest request) {
        return validateUnitNumberUseCase.execute(request.getUnitNumber(), request.getLocation());
    }

    public static class ValidateDeviceRequest {
        private String deviceId;
        private String location;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class ValidateUnitRequest {
        private String unitNumber;
        private String location;

        public String getUnitNumber() { return unitNumber; }
        public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}