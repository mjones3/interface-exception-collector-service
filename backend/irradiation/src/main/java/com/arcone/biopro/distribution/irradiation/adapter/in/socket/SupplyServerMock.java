package com.arcone.biopro.distribution.irradiation.adapter.in.socket;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.ValidateSupplyRequestDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@Profile("rsocket-server")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupplyServerMock {


    @MessageMapping("validateSupply")
    public Mono<Boolean> validateSupply(ValidateSupplyRequestDTO request) {
        log.info("Mock supply service validating: {} {}", request.type(), request.lotNumber());

        if(request.lotNumber().equalsIgnoreCase("Lot1234")) {
            return Mono.just(true);
        }

        if(request.lotNumber().equalsIgnoreCase("Lot1234Lot5678")) {
            return Mono.just(true);
        }

        // Mock logic: return true only for "LII1" with "IRRADIATION_INDICATOR"
        boolean isValid = ("LII1".equals(request.lotNumber()) || "Lot1234".equals(request.lotNumber())) && "IRRADIATION_INDICATOR".equals(request.type());

        log.info("Validation result: {}", isValid);
        return Mono.just(isValid);
    }
}
