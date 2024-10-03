package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.UnitNumberWithCheckDigitMapper;
import com.arcone.biopro.distribution.shipping.domain.model.UnitNumberWithCheckDigit;
import com.arcone.biopro.distribution.shipping.domain.service.UnitNumberWithCheckDigitService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnitNumberWithCheckDigitUseCase implements UnitNumberWithCheckDigitService {

    private final UnitNumberWithCheckDigitMapper unitNumberWithCheckDigitMapper;

    @Override
    @WithSpan("verifyCheckDigit")
    public Mono<RuleResponseDTO> verifyCheckDigit(String unitNumber, String checkDigit) {
        return Mono.fromSupplier(() -> new UnitNumberWithCheckDigit(unitNumber, checkDigit))
            .map(unitNumberWithCheckDigitMapper::mapToDTO)
            .map(unitNumberWithCheckDigit ->
                RuleResponseDTO.builder()
                    .ruleCode(HttpStatus.OK)
                    .results(Map.of("data", List.of(unitNumberWithCheckDigit)))
                    .build()
            )
            .onErrorResume(throwable ->
                Mono.just(
                    RuleResponseDTO.builder()
                        .ruleCode(HttpStatus.BAD_REQUEST)
                        .notifications(
                            List.of(NotificationDTO.builder()
                                .code(HttpStatus.BAD_REQUEST.value())
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .name("INVALID_CHECK_DIGIT")
                                .message(throwable.getMessage())
                                .notificationType(NotificationType.WARN.name())
                                .build())
                        )
                        .build()
                )
            );
    }

}
