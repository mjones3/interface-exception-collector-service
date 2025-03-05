package com.arcone.biopro.distribution.shipping.application.usecase;

import com.arcone.biopro.distribution.shipping.application.dto.NotificationDTO;
import com.arcone.biopro.distribution.shipping.application.dto.NotificationType;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.shipping.application.exception.DomainException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class AbstractUseCase {

    public Mono<RuleResponseDTO> buildErrorResponse(Throwable error) {
        if(error instanceof DuplicateKeyException){
            return Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.BAD_REQUEST)
                .notifications(List.of(NotificationDTO
                    .builder()
                    .code(UseCaseMessageType.EXTERNAL_TRANSFER_DUPLICATED_PRODUCT.getCode())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(UseCaseMessageType.EXTERNAL_TRANSFER_DUPLICATED_PRODUCT.getMessage())
                    .notificationType(UseCaseMessageType.EXTERNAL_TRANSFER_DUPLICATED_PRODUCT.getType().name())
                    .build()))
                .build());
        }else if(error instanceof DomainException exception){
            return Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.BAD_REQUEST)
                .notifications(List.of(NotificationDTO
                    .builder()
                    .code(exception.getUseCaseMessageType().getCode())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(exception.getUseCaseMessageType().getMessage())
                    .notificationType(exception.getUseCaseMessageType().getType().name())
                    .build()))
                .build());
        }else{
            return Mono.just(RuleResponseDTO.builder()
                .ruleCode(HttpStatus.BAD_REQUEST)
                .notifications(List.of(NotificationDTO
                    .builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(error.getMessage())
                    .notificationType(NotificationType.WARN.name())
                    .build()))
                .build());
        }
    }
}
