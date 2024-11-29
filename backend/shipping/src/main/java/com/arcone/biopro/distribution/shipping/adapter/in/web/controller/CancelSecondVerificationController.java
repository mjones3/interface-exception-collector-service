package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.application.dto.CancelSecondVerificationRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.domain.service.CancelSecondVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CancelSecondVerificationController {

    private final CancelSecondVerificationService cancelSecondVerificationService;

    @MutationMapping("cancelSecondVerification")
    public Mono<RuleResponseDTO> cancelSecondVerification(@Argument("cancelSecondVerificationRequest") CancelSecondVerificationRequest cancelSecondVerificationRequest) {
        log.info("Request to cancel second verification {}", cancelSecondVerificationRequest);
        return cancelSecondVerificationService.cancelSecondVerification(cancelSecondVerificationRequest);
    }

    @MutationMapping("confirmCancelSecondVerification")
    public Mono<RuleResponseDTO> confirmCancelSecondVerification(@Argument("confirmCancelSecondVerificationRequest") CancelSecondVerificationRequest cancelSecondVerificationRequest) {
        log.info("Request to confirm cancel second verification {}", cancelSecondVerificationRequest);
        return cancelSecondVerificationService.confirmCancelSecondVerification(cancelSecondVerificationRequest);
    }

}
